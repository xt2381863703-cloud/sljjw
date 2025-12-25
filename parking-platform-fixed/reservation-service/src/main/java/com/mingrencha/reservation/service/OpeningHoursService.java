package com.mingrencha.reservation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingrencha.reservation.client.ParkingClient;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpeningHoursService {
  private final ParkingClient parkingClient;
  private final ObjectMapper om = new ObjectMapper();
  private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
  private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");

  public void validate(String tenantId, String lotId, Instant start, Instant end) {
    var resp = parkingClient.lotSummary(tenantId, lotId);
    if(resp == null || !resp.success()) throw new IllegalArgumentException("parking lot not found");
    var lot = (Map<String,Object>) resp.data().get("lot");
    String json = (String) lot.get("opening_hours_json");
    try {
      @SuppressWarnings("unchecked")
      Map<String,String> m = om.readValue(json, Map.class);

      ZonedDateTime zs = start.atZone(ZONE);
      ZonedDateTime ze = end.atZone(ZONE);

      boolean weekend = zs.getDayOfWeek()==DayOfWeek.SATURDAY || zs.getDayOfWeek()==DayOfWeek.SUNDAY;
      String key = weekend ? "Sat-Sun" : "Mon-Fri";
      String range = m.get(key);
      if(range == null) throw new IllegalArgumentException("opening hours not configured for " + key);

      String[] parts = range.split("-");
      LocalTime open = LocalTime.parse(parts[0], HM);
      LocalTime close = LocalTime.parse(parts[1], HM);

      if(zs.toLocalDate().equals(ze.toLocalDate()) == false) {
        throw new IllegalArgumentException("start/end must be same day in this demo");
      }

      if(zs.toLocalTime().isBefore(open) || ze.toLocalTime().isAfter(close)) {
        throw new IllegalArgumentException("reservation time not within opening hours: " + range);
      }
    } catch(Exception e){
      throw new IllegalArgumentException("invalid opening hours config: " + e.getMessage(), e);
    }
  }
}
