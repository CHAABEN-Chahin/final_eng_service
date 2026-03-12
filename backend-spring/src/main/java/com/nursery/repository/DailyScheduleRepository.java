package com.nursery.repository;

import com.nursery.model.DailySchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DailyScheduleRepository extends MongoRepository<DailySchedule, String> {
    List<DailySchedule> findByNurseryIdOrderByTimeSlotAsc(String nurseryId);
}
