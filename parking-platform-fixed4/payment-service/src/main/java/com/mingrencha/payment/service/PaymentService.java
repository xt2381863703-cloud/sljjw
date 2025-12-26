package com.mingrencha.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingrencha.common.tenant.TenantContext;
import com.mingrencha.payment.client.BillingClient;
import com.mingrencha.payment.repo.PaymentRepo;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepo repo;
  private final BillingClient billingClient;
  private final RabbitTemplate rabbit;
  private final ObjectMapper om = new ObjectMapper();

  public Map<String,Object> create(String sessionNo){
    String tenant = TenantContext.getTenantId();
    var s = billingClient.getSession(tenant, sessionNo);
    if(s==null || !s.success()) throw new IllegalArgumentException("billing session not found");
    long amountCents = ((Number)s.data().get("amount_cents")).longValue();

    String payOrderNo = "po-" + UUID.randomUUID();
    String third = toJson(Map.<String,Object>of("payUrl","https://pay.mock/checkout/"+payOrderNo, "nonce", UUID.randomUUID().toString()));
    Map<String,Object> o = new java.util.HashMap<>();
    o.put("pay_order_no", payOrderNo);
    o.put("tenant_id", tenant);
    o.put("session_no", sessionNo);
    o.put("amount_cents", amountCents);
    o.put("status", "CREATED");
    o.put("channel", "mock");
    o.put("third_pay_params_json", third);
    o.put("created_at", Instant.now().toString());
    o.put("updated_at", Instant.now().toString());
    repo.insert(o);
    rabbit.convertAndSend("parking.events","payment.created", Map.<String,Object>of("payOrderNo", payOrderNo, "sessionNo", sessionNo, "amountCents", amountCents));
    return o;
  }

  public Map<String,Object> notifySuccess(String payOrderNo){
    String tenant = TenantContext.getTenantId();
    var opt = repo.find(payOrderNo);
    if(opt.isEmpty()) throw new IllegalArgumentException("pay order not found");
    var o = opt.get();
    int n = repo.markSuccess(payOrderNo);
    if(n!=1) return o; // idempotent

    String sessionNo = (String)o.get("session_no");
    billingClient.markPaid(tenant, Map.<String,Object>of("sessionNo", sessionNo));

    rabbit.convertAndSend("parking.events","payment.succeeded", Map.<String,Object>of("payOrderNo", payOrderNo, "sessionNo", sessionNo));
    rabbit.convertAndSend("parking.events","gate.open", Map.<String,Object>of("sessionNo", sessionNo, "reason", "payment_success"));
    return repo.find(payOrderNo).orElseThrow();
  }

  private String toJson(Object v){
    try { return om.writeValueAsString(v); } catch(Exception e){ return "{}"; }
  }
}
