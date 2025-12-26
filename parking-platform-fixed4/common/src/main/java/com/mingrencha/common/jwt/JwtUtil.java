package com.mingrencha.common.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public final class JwtUtil {
  private final SecretKey key;
  private final String issuer;

  public JwtUtil(String secret, String issuer) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
  }

  public String generate(String subject, String tenantId, Map<String, Object> extraClaims, long ttlSeconds) {
    var now = Instant.now();
    var builder = Jwts.builder()
        .issuer(issuer)
        .subject(subject)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .claim("tenantId", tenantId);

    if (extraClaims != null) {
      extraClaims.forEach(builder::claim);
    }
    return builder.signWith(key).compact();
  }

  public io.jsonwebtoken.Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
        .parseSignedClaims(token).getPayload();
  }
}
