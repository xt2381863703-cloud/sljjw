package com.mingrencha.billing;

import com.mingrencha.common.jwt.JwtAuthFilter;
import com.mingrencha.common.jwt.JwtUtil;
import com.mingrencha.common.jwt.SecurityConfigSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication extends SecurityConfigSupport {
  public static void main(String[] args) {
    SpringApplication.run(BillingServiceApplication.class, args);
  }

  @Bean
  JwtUtil jwtUtil(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.issuer}") String issuer) {
    return new JwtUtil(secret, issuer);
  }

  @Bean
  JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, @Value("${app.tenant-header}") String tenantHeader) {
    return new JwtAuthFilter(jwtUtil, tenantHeader);
  }
}
