package com.mingrencha.iot.api;

import com.mingrencha.common.api.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class IotController {
  @GetMapping("/healthz")
  public ApiResponse<?> healthz(){
    return ApiResponse.ok(Map.of("status","ok"));
  }
}
