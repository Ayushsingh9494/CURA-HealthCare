package com.cura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CURA — Clinical Unified Record & Assistance Platform
 *
 * <p>Entry point for the Spring Boot modular monolith. All domain modules
 * (auth, user, doctor, appointment, emr, prescription, triage, emergency,
 * notification, audit) are loaded as packages under this root.
 *
 * <p>This is an educational prototype. It uses synthetic data and simulated
 * integrations. It must NOT be used for real patient care.
 */
@SpringBootApplication
public class CuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuraApplication.class, args);
    }
}
