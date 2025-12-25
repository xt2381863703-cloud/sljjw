package com.mingrencha.parking.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.common.jwt.JwtUtil;
import com.mingrencha.common.tenant.TenantContext;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final JdbcTemplate jdbc;
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public record RegisterReq(@NotBlank String username, @NotBlank String password) {}
  public record LoginReq(@NotBlank String username, @NotBlank String password) {}

  @PostMapping("/register")
  public ApiResponse<?> register(@RequestBody RegisterReq req){
    System.out.println(">>> HIT /register, tenant=" + TenantContext.getTenantId() + ", username=" + req.username());
    String tenant = TenantContext.getTenantId();
    if (tenant == null) return ApiResponse.fail("Missing tenantId (header or jwt)");
    var rows = jdbc.queryForList("SELECT id FROM users WHERE tenant_id=? AND username=?", tenant, req.username());
    if(!rows.isEmpty()) return ApiResponse.fail("username exists");
    jdbc.update("INSERT INTO users(id, tenant_id, username, password_bcrypt, created_at) VALUES(?,?,?,?,?)",
        "u-"+UUID.randomUUID(), tenant, req.username(), encoder.encode(req.password()), Instant.now().toString());
    return ApiResponse.ok(Map.of("registered", true));
  }

  @PostMapping("/login")
  public ApiResponse<?> login(@RequestBody LoginReq req){
    String tenant = TenantContext.getTenantId();
    if (tenant == null) return ApiResponse.fail("Missing tenantId (header or jwt)");
    var rows = jdbc.queryForList("SELECT * FROM users WHERE tenant_id=? AND username=?", tenant, req.username());
    if(rows.isEmpty()) return ApiResponse.fail("invalid credentials");
    var u = rows.get(0);
    String hash = (String)u.get("password_bcrypt");
    if(!encoder.matches(req.password(), hash)) return ApiResponse.fail("invalid credentials");
    String token = jwtUtil.generate(req.username(), tenant, Map.of("uid", u.get("id")), 3600*12);
    return ApiResponse.ok(Map.of("token", token, "expiresIn", 3600*12));
  }
}
