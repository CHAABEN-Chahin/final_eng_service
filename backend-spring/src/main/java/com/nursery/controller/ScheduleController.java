package com.nursery.controller;

import com.nursery.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> updateSchedule(@PathVariable String scheduleId,
                                                               @RequestBody Map<String, Object> body) {
        String timeSlot = (String) body.get("timeSlot");
        String activityName = (String) body.get("activityName");
        String description = (String) body.get("description");
        Integer participantCount = body.get("participantCount") instanceof Number
                ? ((Number) body.get("participantCount")).intValue() : null;
        Map<String, Object> result = scheduleService.updateSchedule(scheduleId, timeSlot, activityName,
                description, participantCount);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable String scheduleId) {
        Map<String, Object> result = scheduleService.deleteSchedule(scheduleId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }
}
