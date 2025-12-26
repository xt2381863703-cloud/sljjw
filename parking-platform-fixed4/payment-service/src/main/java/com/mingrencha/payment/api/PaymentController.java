package com.mingrencha.payment.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.payment.repo.PaymentRepo;
import com.mingrencha.payment.service.PaymentService;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService service;
  private final PaymentRepo repo;

  public record CreateReq(@NotBlank String sessionNo){}

  @PostMapping("/payments")
  public ApiResponse<?> create(@RequestBody CreateReq req){
    return ApiResponse.ok(service.create(req.sessionNo()));
  }

  @GetMapping("/payments/{payOrderNo}")
  public ApiResponse<?> get(@PathVariable("payOrderNo") String payOrderNo){
    return ApiResponse.ok(repo.find(payOrderNo).orElse(null));
  }

  // mock notify
  @PostMapping("/payments/notify/{channel}")
  public ApiResponse<?> notify(@PathVariable("channel") String channel, @RequestBody Map<String,Object> body){
    if(!"mock".equals(channel)) return ApiResponse.fail("only mock supported in this demo");
    String payOrderNo = (String) body.get("payOrderNo");
    return ApiResponse.ok(service.notifySuccess(payOrderNo));
  }
}
