package com.mingrencha.payment.client;

import com.mingrencha.common.api.ApiResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="billing-service", url="http://billing-service:8083")
public interface BillingClient {
  @PostMapping("/api/v1/internal/billing/mark-paid")
  ApiResponse<Map<String,Object>> markPaid(@RequestHeader("X-Tenant-Id") String tenantId, @RequestBody Map<String,Object> req);

  @GetMapping("/api/v1/billing-sessions/{sessionNo}")
  ApiResponse<Map<String,Object>> getSession(@RequestHeader("X-Tenant-Id") String tenantId, @PathVariable("sessionNo") String sessionNo);
}
