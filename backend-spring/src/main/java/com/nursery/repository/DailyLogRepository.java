package com.nursery.repository;

import com.nursery.model.DailyLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DailyLogRepository extends MongoRepository<DailyLog, String> {
    List<DailyLog> findByChildIdOrderByDateDesc(String childId);
    List<DailyLog> findByChildIdAndDate(String childId, String date);
    List<DailyLog> findByNurseryIdOrderByDateDesc(String nurseryId);
    List<DailyLog> findByChildIdAndIsInternalFalseOrderByDateDesc(String childId);
}
