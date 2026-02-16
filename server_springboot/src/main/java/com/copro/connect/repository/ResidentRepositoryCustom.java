package com.copro.connect.repository;

import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.model.Resident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ResidentRepositoryCustom {
    Page<Resident> findWithFilters(String search, String batiment, String statutLot, Pageable pageable);
    StatisticsResponse calculateStatistics();
    Optional<Resident> findByLotIdIgnoreCase(String lotId);
}
