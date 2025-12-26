package com.mingrencha.reservation.job;

import com.mingrencha.common.tenant.TenantContext;
import com.mingrencha.reservation.client.ParkingClient;
import com.mingrencha.reservation.repo.ReservationRepo;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpireJob {
  private final ReservationRepo repo;
  private final ParkingClient parkingClient;
  private final RabbitTemplate rabbit;

  // demo: only for tenant t1. In production you'd iterate tenants.
  @Scheduled(fixedDelay = 10_000)
  public void run(){
    TenantContext.setTenantId("t1");
    try {
      List<Map<String,Object>> expired = repo.findExpiredActive(50);
      for (var r: expired){
        String id = (String) r.get("id");
        String spaceId = (String) r.get("space_id");
        int n = repo.markExpired(id);
        if(n==1){
          parkingClient.release("t1", Map.of("spaceId", spaceId));
          rabbit.convertAndSend("parking.events", "reservation.expired", Map.of("reservationId", id, "spaceId", spaceId));
          log.info("expired reservation {} -> released {}", id, spaceId);
        }
      }
    } finally {
      TenantContext.clear();
    }
  }
}
