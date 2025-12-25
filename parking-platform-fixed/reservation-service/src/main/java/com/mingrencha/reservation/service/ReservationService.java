package com.mingrencha.reservation.service;

import com.mingrencha.common.tenant.TenantContext;
import com.mingrencha.reservation.client.ParkingClient;
import com.mingrencha.reservation.repo.ReservationRepo;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {
  private final ReservationRepo repo;
  private final ParkingClient parkingClient;
  private final RedisLockService lockService;
  private final OpeningHoursService openingHoursService;
  private final RabbitTemplate rabbit;

  public Map<String,Object> create(String userId, String lotId, String spaceId, boolean preferEv, Instant start, Instant end) {
    String tenant = TenantContext.getTenantId();
    if(tenant == null) throw new IllegalArgumentException("Missing tenantId");
    openingHoursService.validate(tenant, lotId, start, end);

    String lockKey = (spaceId != null && !spaceId.isBlank()) ? ("lock:space:" + spaceId) : ("lock:lot:" + lotId);
    String token = lockService.tryLock(lockKey, Duration.ofSeconds(20));
    if(token == null) throw new IllegalStateException("resource busy, try again");

    try {
      Map<String,Object> space;
      if(spaceId != null && !spaceId.isBlank()){
        var resp = parkingClient.reserve(tenant, Map.of("spaceId", spaceId));
        if(resp == null || !resp.success()) throw new IllegalStateException(resp==null?"reserve failed":resp.message());
        space = Map.of("id", spaceId);
      } else {
        var resp = parkingClient.allocate(tenant, Map.of("lotId", lotId, "preferEv", preferEv));
        if(resp == null || !resp.success()) throw new IllegalStateException(resp==null?"allocate failed":resp.message());
        space = (Map<String,Object>) resp.data().get("space");
        spaceId = (String) space.get("id");
      }

      String id = "rsv-" + UUID.randomUUID();
      Instant now = Instant.now();
      Instant lockExpireAt = now.plus(Duration.ofMinutes(15));

      Map<String,Object> r = Map.of(
          "id", id,
          "tenant_id", tenant,
          "user_id", userId,
          "lot_id", lotId,
          "space_id", spaceId,
          "start_time", start.toString(),
          "end_time", end.toString(),
          "status", "ACTIVE",
          "lock_expire_at", lockExpireAt.toString(),
          "created_at", now.toString()
      );
      repo.insert(r);

      rabbit.convertAndSend("parking.events", "reservation.created", Map.of(
          "reservationId", id,
          "lotId", lotId,
          "spaceId", spaceId,
          "userId", userId,
          "lockExpireAt", lockExpireAt.toString()
      ));
      return r;
    } finally {
      lockService.unlock(lockKey, token);
    }
  }

  public boolean cancel(String reservationId) {
    String tenant = TenantContext.getTenantId();
    var rOpt = repo.findById(reservationId);
    if(rOpt.isEmpty()) throw new IllegalArgumentException("reservation not found");
    var r = rOpt.get();
    String spaceId = (String) r.get("space_id");
    int n = repo.cancel(reservationId);
    if(n==1){
      parkingClient.release(tenant, Map.of("spaceId", spaceId));
      rabbit.convertAndSend("parking.events", "reservation.cancelled", Map.of("reservationId", reservationId, "spaceId", spaceId));
      return true;
    }
    return false;
  }
}
