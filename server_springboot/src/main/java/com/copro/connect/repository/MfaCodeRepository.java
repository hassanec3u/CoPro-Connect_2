package com.copro.connect.repository;

import com.copro.connect.model.MfaCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfaCodeRepository extends MongoRepository<MfaCode, String> {

    Optional<MfaCode> findTopByUsernameAndUsedFalseOrderByCreatedAtDesc(String username);

    void deleteAllByUsername(String username);
}
