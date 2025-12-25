package com.mingrencha.billing.service;

import com.mingrencha.common.tenant.TenantContext;
import com.mingrencha.billing.repo.BillingRepo;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillingService {
  private final BillingRepo repo;
  private final RabbitTemplate rabbit;

  public Map<String,Object> vehicleEnter(String lotId, String spaceId, String plateNo, String userId, Instant enterTime){
    String tenant = TenantContext.getTenantId();
    String sessionNo = "bs-" + UUID.randomUUID();
    // NOTE: Map.of() supports up to 10 entries and does not allow null values.
    // We use HashMap to keep this compatible and allow exit_time = null.
    Map<String,Object> s = new HashMap<>();
    s.put("session_no", sessionNo);
    s.put("tenant_id", tenant);
    s.put("user_id", userId);
    s.put("lot_id", lotId);
    s.put("space_id", spaceId);
    s.put("plate_no", plateNo);
    s.put("enter_time", enterTime.toString());
    s.put("exit_time", null);
    s.put("status", "ACTIVE");
    s.put("amount_cents", 0L);
    s.put("created_at", Instant.now().toString());
    s.put("updated_at", Instant.now().toString());
    repo.insert(s);
    rabbit.convertAndSend("parking.events","billing.session.started", Map.of("sessionNo", sessionNo, "spaceId", spaceId, "lotId", lotId));
    return s;
  }

  public Map<String,Object> vehicleExit(String sessionNo, Instant exitTime){
    var opt = repo.find(sessionNo);
    if(opt.isEmpty()) throw new IllegalArgumentException("billing session not found");
    var s = opt.get();
    Instant enter = Instant.parse((String)s.get("enter_time"));
    long amount = calcAmountCents(enter, exitTime);
    int n = repo.closeToWaitPay(sessionNo, exitTime.toString(), amount);
    if(n!=1) throw new IllegalStateException("session not active");
    rabbit.convertAndSend("parking.events","billing.session.closed", Map.of("sessionNo", sessionNo, "amountCents", amount));
    return repo.find(sessionNo).orElseThrow();
  }

  // Rule: 30 minutes per 15 yuan. Round up.
  private long calcAmountCents(Instant enter, Instant exit){
    long mins = Math.max(0, Duration.between(enter, exit).toMinutes());
    long blocks = (mins + 29) / 30;
    long yuan = blocks * 15;
    return yuan * 100;
  }
}
