package com.nursery.service;

import com.nursery.model.DailySchedule;
import com.nursery.repository.DailyScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ScheduleService {

    private final DailyScheduleRepository dailyScheduleRepository;

    public ScheduleService(DailyScheduleRepository dailyScheduleRepository) {
        this.dailyScheduleRepository = dailyScheduleRepository;
    }

    public Map<String, Object> updateSchedule(String scheduleId, String timeSlot, String activityName,
                                               String description, Integer participantCount) {
        Map<String, Object> result = new HashMap<>();
        Optional<DailySchedule> opt = dailyScheduleRepository.findById(scheduleId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Schedule not found");
            return result;
        }
        DailySchedule schedule = opt.get();
        if (timeSlot != null) schedule.setTimeSlot(timeSlot);
        if (activityName != null) schedule.setActivityName(activityName);
        if (description != null) schedule.setDescription(description);
        if (participantCount != null) schedule.setParticipantCount(participantCount);
        schedule.setUpdatedAt(Instant.now());
        dailyScheduleRepository.save(schedule);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", schedule.getId());
        dto.put("timeSlot", schedule.getTimeSlot());
        dto.put("activityName", schedule.getActivityName());
        dto.put("description", schedule.getDescription());
        dto.put("participantCount", schedule.getParticipantCount());

        result.put("success", true);
        result.put("schedule", dto);
        return result;
    }

    public Map<String, Object> deleteSchedule(String scheduleId) {
        Map<String, Object> result = new HashMap<>();
        if (!dailyScheduleRepository.existsById(scheduleId)) {
            result.put("success", false);
            result.put("message", "Schedule not found");
            return result;
        }
        dailyScheduleRepository.deleteById(scheduleId);
        result.put("success", true);
        result.put("message", "Schedule deleted");
        return result;
    }
}
