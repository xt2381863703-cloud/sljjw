package com.mingrencha.reservation.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.reservation.repo.ReservationRepo;
import com.mingrencha.reservation.service.ReservationService;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReservationController {
  private final ReservationService service;
  private final ReservationRepo repo;

  public record CreateReq(
      @NotBlank String lotId,
      String spaceId,
      boolean preferEvSpace,
      @NotBlank String startTime,
      @NotBlank String endTime
  ) {}

  @PostMapping("/reservations")
  public ApiResponse<?> create(Authentication auth, @RequestBody CreateReq req){
    String userId = auth.getName();
    var r = service.create(userId, req.lotId(), req.spaceId(), req.preferEvSpace(), Instant.parse(req.startTime()), Instant.parse(req.endTime()));
    return ApiResponse.ok(r);
  }

  @PostMapping("/reservations/{id}/cancel")
  public ApiResponse<?> cancel(@PathVariable("id") String id){
    return ApiResponse.ok(Map.of("cancelled", service.cancel(id)));
  }

  @GetMapping("/reservations/{id}")
  public ApiResponse<?> get(@PathVariable("id") String id){
    return ApiResponse.ok(repo.findById(id).orElse(null));
  }
}
