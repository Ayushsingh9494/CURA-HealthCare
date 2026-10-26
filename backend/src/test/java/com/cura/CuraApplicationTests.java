package com.cura;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads without errors.
 *
 * <p>Uses the {@code test} profile which:
 * <ul>
 *   <li>Switches to H2 in-memory database</li>
 *   <li>Disables Flyway (so no real DB migrations run)</li>
 *   <li>Uses mock AI and notification providers</li>
 * </ul>
 *
 * <p>If this test fails, the application will not start in any environment.
 * Common causes:
 * <ul>
 *   <li>Missing required property with no default</li>
 *   <li>Bean wiring error (circular dependency, missing @Bean)</li>
 *   <li>Invalid configuration class</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
class CuraApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context fails to load, this test fails with a
        // descriptive error — no assertions needed here.
    }
}
