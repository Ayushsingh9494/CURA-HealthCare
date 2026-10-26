package com.cura.common.controller;

import com.cura.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Custom application status endpoint at {@code GET /api/v1/status}.
 *
 * <p>This is separate from Spring Actuator's {@code /actuator/health}.
 * It returns application-level metadata and is the endpoint the React frontend
 * calls on startup to verify backend connectivity.
 *
 * <p>No authentication required — useful for smoke-testing deployments.
 */
@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Status", description = "Application status and connectivity check")
public class StatusController {

    @Value("${spring.application.name:cura}")
    private String appName;

    @Value("${cura.version:0.1.0}")
    private String appVersion;

    /**
     * Returns application status, version, and server timestamp.
     *
     * @return 200 OK with status payload
     */
    @GetMapping
    @Operation(
        summary = "Application status",
        description = "Returns application name, version, and server timestamp. No auth required."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        Map<String, Object> payload = Map.of(
            "status",    "UP",
            "service",   appName.toUpperCase(),
            "version",   appVersion,
            "timestamp", Instant.now().toString(),
            "disclaimer", "Educational prototype — not a validated clinical system"
        );
        return ResponseEntity.ok(ApiResponse.ok(payload, "CURA backend is running"));
    }
}
