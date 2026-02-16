package com.copro.connect.repository;

import com.copro.connect.model.ResidentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentHistoryRepository extends MongoRepository<ResidentHistory, String> {
    
    List<ResidentHistory> findByApartmentKeyOrderByChangedAtDesc(String apartmentKey);
    
    List<ResidentHistory> findByResidentIdOrderByChangedAtDesc(String residentId);
}
