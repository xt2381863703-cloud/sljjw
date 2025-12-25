package com.mingrencha.reservation.repo;

import com.mingrencha.common.tenant.TenantContext;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationRepo {
  private final JdbcTemplate jdbc;

  public void insert(Map<String,Object> r){
    jdbc.update("INSERT INTO reservation(id, tenant_id, user_id, lot_id, space_id, start_time, end_time, status, lock_expire_at, created_at) VALUES(?,?,?,?,?,?,?,?,?,?)",
        r.get("id"), r.get("tenant_id"), r.get("user_id"), r.get("lot_id"), r.get("space_id"),
        r.get("start_time"), r.get("end_time"), r.get("status"), r.get("lock_expire_at"), r.get("created_at"));
  }

  public Optional<Map<String,Object>> findById(String id){
    String tenant = TenantContext.getTenantId();
    var list = jdbc.queryForList("SELECT * FROM reservation WHERE id=? AND tenant_id=?", id, tenant);
    return list.isEmpty()?Optional.empty():Optional.of(list.get(0));
  }

  public int cancel(String id){
    String tenant = TenantContext.getTenantId();
    return jdbc.update("UPDATE reservation SET status='CANCELLED' WHERE id=? AND tenant_id=? AND status='ACTIVE'", id, tenant);
  }

  public List<Map<String,Object>> findExpiredActive(int limit){
    String tenant = TenantContext.getTenantId();
    return jdbc.queryForList("SELECT * FROM reservation WHERE tenant_id=? AND status='ACTIVE' AND lock_expire_at < NOW() LIMIT ?",
        tenant, limit);
  }

  public int markExpired(String id){
    String tenant = TenantContext.getTenantId();
    return jdbc.update("UPDATE reservation SET status='EXPIRED' WHERE id=? AND tenant_id=? AND status='ACTIVE'", id, tenant);
  }
}
