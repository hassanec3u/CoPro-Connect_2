package com.copro.connect.config;

import com.copro.connect.model.Resident;
import com.copro.connect.model.User;
import com.copro.connect.repository.ResidentRepository;
import com.copro.connect.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Override
    public void run(String... args) {
        initializeAdminUser();
        initializeResidentsData();
    }
    
    private void initializeAdminUser() {
        // Créer l'utilisateur admin par défaut s'il n'existe pas
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Administrateur");
            admin.setEmail("admin@copro-connect.fr");
            admin.setRole("ADMIN");
            admin.setMfaEnabled(true);
            
            userRepository.save(admin);
            log.info("✅ Admin user created successfully!");
            log.info("   Username: admin");
            log.info("   Password: admin123");
            log.info("   Email: admin@copro-connect.fr");
        } else {
            log.info("ℹ️  Admin user already exists");
        }
    }
    
    private void initializeResidentsData() {
        // Charger les données des résidents depuis residentData.json
        long existingCount = residentRepository.count();
        
        if (existingCount > 0) {
            log.info("ℹ️  Database already contains {} residents. Skipping initialization.", existingCount);
            return;
        }
        
        try {
            log.info("📂 Loading residents data from residentData.json...");
            ClassPathResource resource = new ClassPathResource("residentData.json");
            
            try (InputStream inputStream = resource.getInputStream()) {
                List<Resident> residents = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<Resident>>() {}
                );
                
                // Sauvegarder tous les résidents
                residentRepository.saveAll(residents);
                
                log.info("✅ Successfully loaded {} residents from residentData.json", residents.size());
            }
        } catch (IOException e) {
            log.error("❌ Failed to load residents data: {}", e.getMessage(), e);
            log.warn("⚠️  Application will continue without initial residents data");
        }
    }
}
