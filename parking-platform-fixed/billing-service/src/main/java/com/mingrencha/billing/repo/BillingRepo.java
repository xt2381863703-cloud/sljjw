package com.mingrencha.billing.repo;

import com.mingrencha.common.tenant.TenantContext;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BillingRepo {
  private final JdbcTemplate jdbc;

  public void insert(Map<String,Object> s){
    jdbc.update("INSERT INTO billing_session(session_no, tenant_id, user_id, lot_id, space_id, plate_no, enter_time, exit_time, status, amount_cents, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
        s.get("session_no"), s.get("tenant_id"), s.get("user_id"), s.get("lot_id"), s.get("space_id"), s.get("plate_no"),
        s.get("enter_time"), s.get("exit_time"), s.get("status"), s.get("amount_cents"), s.get("created_at"), s.get("updated_at"));
  }

  public Optional<Map<String,Object>> find(String sessionNo){
    String tenant = TenantContext.getTenantId();
    var list = jdbc.queryForList("SELECT * FROM billing_session WHERE tenant_id=? AND session_no=?", tenant, sessionNo);
    return list.isEmpty()?Optional.empty():Optional.of(list.get(0));
  }

  public int closeToWaitPay(String sessionNo, String exitTime, long amountCents){
    String tenant = TenantContext.getTenantId();
    return jdbc.update("UPDATE billing_session SET exit_time=?, status='WAIT_PAY', amount_cents=?, updated_at=NOW() WHERE tenant_id=? AND session_no=? AND status='ACTIVE'",
        exitTime, amountCents, tenant, sessionNo);
  }

  public int markPaid(String sessionNo){
    String tenant = TenantContext.getTenantId();
    return jdbc.update("UPDATE billing_session SET status='PAID', updated_at=NOW() WHERE tenant_id=? AND session_no=? AND status='WAIT_PAY'", tenant, sessionNo);
  }
}
