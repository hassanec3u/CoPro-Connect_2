package com.copro.connect.repository;

import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.model.Resident;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ResidentRepositoryCustomImpl implements ResidentRepositoryCustom {
    
    private final MongoTemplate mongoTemplate;
    
    @Override
    public Page<Resident> findWithFilters(String search, String batiment, String statutLot, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();
        
        // Search filter (if provided)
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim();
            // Escape special regex characters and perform a "contains" search
            String escapedTerm = Pattern.quote(searchTerm);
            Pattern pattern = Pattern.compile(".*" + escapedTerm + ".*", Pattern.CASE_INSENSITIVE);
            
            criteria.add(new Criteria().orOperator(
                Criteria.where("proprietaireNom").regex(pattern),
                Criteria.where("lotId").regex(pattern),
                Criteria.where("porte").regex(pattern),
                Criteria.where("occupants.nom").regex(pattern),
                Criteria.where("happixAccounts.nom").regex(pattern)
            ));
        }
        
        // Building filter (if provided)
        if (batiment != null && !batiment.trim().isEmpty()) {
            criteria.add(Criteria.where("batiment").is(batiment));
        }

        // Status filter (if provided)
        if (statutLot != null && !statutLot.trim().isEmpty()) {
            criteria.add(Criteria.where("statutLot").is(statutLot));
        }

        // Combine all criteria
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        
        // Create a Pageable with sorting (from Pageable if present, otherwise default sort)
        Pageable finalPageable;
        if (pageable.getSort().isSorted()) {
            // Use Pageable sorting
            finalPageable = pageable;
        } else {
            // Default sort (batiment ASC, porte ASC)
            finalPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                    Sort.Order.asc("batiment"),
                    Sort.Order.asc("porte")
                )
            );
        }
        
        // Apply sorting and pagination
        query.with(finalPageable);

        // Count total
        long total = mongoTemplate.count(query, Resident.class);

        // Retrieve results
        List<Resident> residents = mongoTemplate.find(query, Resident.class);
        
        return new PageImpl<>(residents, finalPageable, total);
    }
    
    @Override
    public StatisticsResponse calculateStatistics() {
        // Retrieve all residents
        List<Resident> allResidents = mongoTemplate.findAll(Resident.class);

        // Calculate statistics
        long totalLots = allResidents.size();
        
        // Total occupants
        long totalOccupants = allResidents.stream()
            .mapToLong(r -> r.getOccupants() != null ? r.getOccupants().size() : 0)
            .sum();
        
        // Total comptes Happix
        long totalHappix = allResidents.stream()
            .mapToLong(r -> r.getHappixAccounts() != null ? r.getHappixAccounts().size() : 0)
            .sum();
        
        // Number of unique buildings
        long totalBatiments = allResidents.stream()
            .map(Resident::getBatiment)
            .filter(b -> b != null && !b.isEmpty())
            .distinct()
            .count();
        
        // Count by status
        Map<String, Long> statutCount = new HashMap<>();
        allResidents.forEach(r -> {
            String statut = r.getStatutLot() != null && !r.getStatutLot().isEmpty()
                ? r.getStatutLot()
                : "Undefined";
            statutCount.put(statut, statutCount.getOrDefault(statut, 0L) + 1);
        });

        // Count by building
        Map<String, Long> batimentCount = new HashMap<>();
        allResidents.forEach(r -> {
            String bat = r.getBatiment() != null && !r.getBatiment().isEmpty()
                ? r.getBatiment()
                : "Undefined";
            batimentCount.put(bat, batimentCount.getOrDefault(bat, 0L) + 1);
        });

        // Lots with/without occupants
        long lotsAvecOccupants = allResidents.stream()
            .filter(r -> r.getOccupants() != null && !r.getOccupants().isEmpty())
            .count();
        long lotsVides = totalLots - lotsAvecOccupants;
        
        // Average occupants per lot
        double moyenneOccupants = totalLots > 0 
            ? Math.round((double) totalOccupants / totalLots * 10.0) / 10.0 
            : 0.0;
        
        // Count Happix accounts by type
        Map<String, Long> happixByType = new HashMap<>();
        allResidents.forEach(r -> {
            if (r.getHappixAccounts() != null) {
                r.getHappixAccounts().forEach(h -> {
                    String type = h.getType() != null && !h.getType().isEmpty()
                        ? h.getType()
                        : "Undefined";
                    happixByType.put(type, happixByType.getOrDefault(type, 0L) + 1);
                });
            }
        });
        
        return new StatisticsResponse(
            totalLots,
            totalBatiments,
            totalOccupants,
            totalHappix,
            statutCount,
            batimentCount,
            lotsAvecOccupants,
            lotsVides,
            moyenneOccupants,
            happixByType
        );
    }
    
    @Override
    public Optional<Resident> findByLotIdIgnoreCase(String lotId) {
        if (lotId == null || lotId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Create a regex for case-insensitive search
        Pattern pattern = Pattern.compile("^" + Pattern.quote(lotId.trim()) + "$", Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("lotId").regex(pattern));
        
        Resident resident = mongoTemplate.findOne(query, Resident.class);
        return Optional.ofNullable(resident);
    }
}
