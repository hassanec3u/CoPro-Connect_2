package com.copro.connect.repository;

import com.copro.connect.model.Resident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentRepository extends MongoRepository<Resident, String>, ResidentRepositoryCustom {
    
    List<Resident> findAllByOrderByBatimentAscPorteAsc();

    List<Resident> findByBatiment(String batiment);
    
    List<Resident> findByStatutLot(String statutLot);
}
