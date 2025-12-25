package com.mingrencha.reservation.client;

import com.mingrencha.common.api.ApiResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="parking-service", url="http://parking-service:8081")
public interface ParkingClient {

  @PostMapping("/api/v1/internal/spaces/allocate")
  ApiResponse<Map<String,Object>> allocate(@RequestHeader("X-Tenant-Id") String tenantId, @RequestBody Map<String,Object> req);

  @PostMapping("/api/v1/internal/spaces/reserve")
  ApiResponse<Map<String,Object>> reserve(@RequestHeader("X-Tenant-Id") String tenantId, @RequestBody Map<String,Object> req);

  @PostMapping("/api/v1/internal/spaces/release")
  ApiResponse<Map<String,Object>> release(@RequestHeader("X-Tenant-Id") String tenantId, @RequestBody Map<String,Object> req);

  @GetMapping("/api/v1/parking-lots/{lotId}/summary")
  ApiResponse<Map<String,Object>> lotSummary(@RequestHeader("X-Tenant-Id") String tenantId, @PathVariable("lotId") String lotId);
}
