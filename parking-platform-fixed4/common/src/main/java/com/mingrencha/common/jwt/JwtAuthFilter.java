package com.mingrencha.common.jwt;

import com.mingrencha.common.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final String tenantHeader;

  public JwtAuthFilter(JwtUtil jwtUtil, String tenantHeader) {
    this.jwtUtil = jwtUtil;
    this.tenantHeader = tenantHeader;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String headerTenant = request.getHeader(tenantHeader);
      String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7);
        Claims claims = jwtUtil.parseClaims(token);

        String jwtTenant = claims.get("tenantId", String.class);
        if (headerTenant != null && jwtTenant != null && !headerTenant.equals(jwtTenant)) {
          response.setStatus(403);
          response.setContentType("application/json");
          response.getWriter().write("{\"success\":false,\"message\":\"Tenant mismatch\",\"data\":null}");
          return;
        }
        String effectiveTenant = headerTenant != null ? headerTenant : jwtTenant;
        if (effectiveTenant != null) TenantContext.setTenantId(effectiveTenant);

        String sub = claims.getSubject();
        var authToken = new UsernamePasswordAuthenticationToken(sub, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);
      } else if (headerTenant != null) {
        // allow health checks or public endpoints with tenant header only
        TenantContext.setTenantId(headerTenant);
      }

      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }
}
