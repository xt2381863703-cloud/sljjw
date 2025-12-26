package com.mingrencha.billing.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.billing.repo.BillingRepo;
import com.mingrencha.billing.service.BillingService;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BillingController {
  private final BillingService service;
  private final BillingRepo repo;

  public record EnterReq(@NotBlank String lotId, String spaceId, @NotBlank String plateNo, String enterTime) {}
  @PostMapping("/events/vehicle-enter")
  public ApiResponse<?> enter(Authentication auth, @RequestBody EnterReq req){
    String userId = auth!=null?auth.getName():null;
    Instant t = req.enterTime()!=null?Instant.parse(req.enterTime()):Instant.now();
    return ApiResponse.ok(service.vehicleEnter(req.lotId(), req.spaceId(), req.plateNo(), userId, t));
  }

  public record ExitReq(@NotBlank String sessionNo, String exitTime) {}
  @PostMapping("/events/vehicle-exit")
  public ApiResponse<?> exit(@RequestBody ExitReq req){
    Instant t = req.exitTime()!=null?Instant.parse(req.exitTime()):Instant.now();
    return ApiResponse.ok(service.vehicleExit(req.sessionNo(), t));
  }

  @GetMapping("/billing-sessions/{sessionNo}")
  public ApiResponse<?> get(@PathVariable("sessionNo") String sessionNo){
    return ApiResponse.ok(repo.find(sessionNo).orElse(null));
  }
}
