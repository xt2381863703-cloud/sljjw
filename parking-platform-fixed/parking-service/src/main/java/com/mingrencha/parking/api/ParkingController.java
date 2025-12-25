package com.mingrencha.parking.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.parking.repo.ParkingRepo;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ParkingController {
  private final ParkingRepo repo;

  @GetMapping("/parking-lots/{lotId}/summary")
  public ApiResponse<?> lotSummary(@PathVariable("lotId") String lotId){
    var lot = repo.getLot(lotId);
    var counts = repo.countByStatus(lotId);
    return ApiResponse.ok(Map.of("lot", lot, "counts", counts));
  }

  @GetMapping("/parking-lots/{lotId}/spaces")
  public ApiResponse<?> listSpaces(
      @PathVariable String lotId,
      @RequestParam(required=false) String status,
      @RequestParam(defaultValue="20") @Min(1) int limit,
      @RequestParam(defaultValue="0") @Min(0) int offset
  ){
    return ApiResponse.ok(repo.listSpaces(lotId, status, limit, offset));
  }
}
