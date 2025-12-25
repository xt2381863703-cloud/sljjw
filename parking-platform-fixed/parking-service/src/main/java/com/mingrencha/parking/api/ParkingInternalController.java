package com.mingrencha.parking.api;

import com.mingrencha.common.api.ApiResponse;
import com.mingrencha.parking.repo.ParkingRepo;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ParkingInternalController {
  private final ParkingRepo repo;
  private final RabbitTemplate rabbit;

  @PostMapping("/iot/parking-status")
  public ApiResponse<?> iotStatus(@RequestBody ParkingStatusReq req){
    // external iot reports occupied/available; for simplicity allow force update
    repo.forceUpdateSpaceStatus(req.spaceId(), req.status());
    rabbit.convertAndSend("parking.events", "parking.status.updated", Map.of(
        "spaceId", req.spaceId(),
        "status", req.status()
    ));
    return ApiResponse.ok(Map.of("updated", true));
  }

  public record ParkingStatusReq(@NotBlank String spaceId, @NotBlank String status){}

  public record AllocateReq(@NotBlank String lotId, boolean preferEv){}
  @PostMapping("/internal/spaces/allocate")
  public ApiResponse<?> allocate(@RequestBody AllocateReq req){
    var opt = repo.allocateSpace(req.lotId(), req.preferEv());
    if(opt.isEmpty()) return ApiResponse.fail("no available space");
    String spaceId = (String) opt.get().get("id");
    boolean ok = repo.updateSpaceStatusIfMatch(spaceId, "AVAILABLE", "RESERVED");
    if(!ok) return ApiResponse.fail("space not available");
    return ApiResponse.ok(Map.of("space", opt.get()));
  }

  public record ReserveSpecificReq(@NotBlank String spaceId){}
  @PostMapping("/internal/spaces/reserve")
  public ApiResponse<?> reserveSpecific(@RequestBody ReserveSpecificReq req){
    boolean ok = repo.updateSpaceStatusIfMatch(req.spaceId(), "AVAILABLE", "RESERVED");
    if(!ok) return ApiResponse.fail("space not available");
    return ApiResponse.ok(Map.of("reserved", true));
  }

  public record ReleaseReq(@NotBlank String spaceId){}
  @PostMapping("/internal/spaces/release")
  public ApiResponse<?> release(@RequestBody ReleaseReq req){
    // idempotent: if already AVAILABLE, treat success
    var space = repo.findSpace(req.spaceId()).orElse(null);
    if(space==null) return ApiResponse.fail("space not found");
    String status = (String) space.get("status");
    if("AVAILABLE".equals(status)) return ApiResponse.ok(Map.of("released", true, "already", true));
    repo.forceUpdateSpaceStatus(req.spaceId(), "AVAILABLE");
    rabbit.convertAndSend("parking.events", "parking.space.released", Map.of("spaceId", req.spaceId()));
    return ApiResponse.ok(Map.of("released", true));
  }
}
