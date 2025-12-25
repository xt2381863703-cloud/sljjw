package com.mingrencha.billing.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.billing.repo.BillingRepo;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class BillingInternalController {
  private final BillingRepo repo;

  public record PaidReq(@NotBlank String sessionNo){}
  @PostMapping("/billing/mark-paid")
  public ApiResponse<?> markPaid(@RequestBody PaidReq req){
    int n = repo.markPaid(req.sessionNo());
    if(n!=1) return ApiResponse.fail("session not in WAIT_PAY");
    return ApiResponse.ok(Map.of("paid", true));
  }
}
