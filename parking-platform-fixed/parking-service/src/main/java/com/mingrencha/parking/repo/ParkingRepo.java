
package com.mingrencha.parking.repo;

import com.mingrencha.common.tenant.TenantContext;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ParkingRepo {
  private final JdbcTemplate jdbc;

  public Map<String,Object> getLot(String lotId){
    return jdbc.queryForMap("SELECT * FROM parking_lot WHERE id=? AND tenant_id=?", lotId, TenantContext.getTenantId());
  }

  public List<Map<String,Object>> listSpaces(String lotId, String status, int limit, int offset){
    String tenant = TenantContext.getTenantId();
    String sql = "SELECT * FROM parking_space WHERE tenant_id=? AND lot_id=? ";
    List<Object> args = new ArrayList<>();
    args.add(tenant); args.add(lotId);
    if(status!=null && !status.isBlank()){
      sql += " AND status=? ";
      args.add(status);
    }
    sql += " ORDER BY code LIMIT ? OFFSET ? ";
    args.add(limit); args.add(offset);
    return jdbc.queryForList(sql, args.toArray());
  }

  public Map<String,Long> countByStatus(String lotId){
    String tenant = TenantContext.getTenantId();
    var rows = jdbc.queryForList("SELECT status, COUNT(1) c FROM parking_space WHERE tenant_id=? AND lot_id=? GROUP BY status",
        tenant, lotId);
    Map<String,Long> m = new HashMap<>();
    for (var r: rows){
      m.put((String)r.get("status"), ((Number)r.get("c")).longValue());
    }
    return m;
  }

  public Optional<Map<String,Object>> findSpace(String spaceId){
    String tenant = TenantContext.getTenantId();
    var list = jdbc.queryForList("SELECT * FROM parking_space WHERE id=? AND tenant_id=?", spaceId, tenant);
    return list.isEmpty()?Optional.empty():Optional.of(list.get(0));
  }

  public Optional<Map<String,Object>> allocateSpace(String lotId, boolean preferEv){
    String tenant = TenantContext.getTenantId();
    // pick first AVAILABLE based on preferEv then code
    String sql;
    if(preferEv){
      sql = "SELECT * FROM parking_space WHERE tenant_id=? AND lot_id=? AND status='AVAILABLE' ORDER BY CASE type WHEN 'EV' THEN 0 ELSE 1 END, code LIMIT 1";
    } else {
      sql = "SELECT * FROM parking_space WHERE tenant_id=? AND lot_id=? AND status='AVAILABLE' ORDER BY code LIMIT 1";
    }
    var list = jdbc.queryForList(sql, tenant, lotId);
    return list.isEmpty()?Optional.empty():Optional.of(list.get(0));
  }

  public boolean updateSpaceStatusIfMatch(String spaceId, String fromStatus, String toStatus){
    String tenant = TenantContext.getTenantId();
    int n = jdbc.update("UPDATE parking_space SET status=?, updated_at=NOW() WHERE id=? AND tenant_id=? AND status=?",
        toStatus, spaceId, tenant, fromStatus);
    return n==1;
  }

  public void forceUpdateSpaceStatus(String spaceId, String toStatus){
    String tenant = TenantContext.getTenantId();
    jdbc.update("UPDATE parking_space SET status=?, updated_at=NOW() WHERE id=? AND tenant_id=?",
        toStatus, spaceId, tenant);
  }
}
