package com.copro.connect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CoproConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoproConnectApplication.class, args);
    }
}
