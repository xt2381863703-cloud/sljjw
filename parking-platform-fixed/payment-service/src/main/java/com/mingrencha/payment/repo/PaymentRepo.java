package com.mingrencha.payment.repo;

import com.mingrencha.common.tenant.TenantContext;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepo {
  private final JdbcTemplate jdbc;

  public void insert(Map<String,Object> o){
    jdbc.update("INSERT INTO pay_order(pay_order_no, tenant_id, session_no, amount_cents, status, channel, third_pay_params_json, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?)",
        o.get("pay_order_no"), o.get("tenant_id"), o.get("session_no"), o.get("amount_cents"), o.get("status"),
        o.get("channel"), o.get("third_pay_params_json"), o.get("created_at"), o.get("updated_at"));
  }

  public Optional<Map<String,Object>> find(String payOrderNo){
    String tenant = TenantContext.getTenantId();
    var list = jdbc.queryForList("SELECT * FROM pay_order WHERE tenant_id=? AND pay_order_no=?", tenant, payOrderNo);
    return list.isEmpty()?Optional.empty():Optional.of(list.get(0));
  }

  public int markSuccess(String payOrderNo){
    String tenant = TenantContext.getTenantId();
    return jdbc.update("UPDATE pay_order SET status='SUCCESS', updated_at=NOW() WHERE tenant_id=? AND pay_order_no=? AND status='CREATED'",
        tenant, payOrderNo);
  }
}
