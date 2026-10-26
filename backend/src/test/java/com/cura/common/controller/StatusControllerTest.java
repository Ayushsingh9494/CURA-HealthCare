package com.cura.common.controller;

import com.cura.auth.security.JwtAuthenticationFilter;
import com.cura.common.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc test for {@link StatusController}.
 *
 * <p>This test uses {@code @WebMvcTest} — it only loads the web layer
 * (the controller and its security config), NOT the full application context.
 * This makes it very fast to run.
 *
 * <p>Learning note: @WebMvcTest is a "test slice" — Spring Boot's way of
 * loading only the parts of the context relevant to a specific layer.
 * Other test slices: @DataJpaTest (JPA layer), @JsonTest (JSON serialisation).
 */
@WebMvcTest(StatusController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthFilter;

    @Test
    void getStatus_returnsOkWithUpStatus() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.disclaimer").exists());
    }

    @Test
    void getStatus_requiresNoAuthentication() throws Exception {
        // The endpoint must be public — no 401 without a token
        mockMvc.perform(get("/api/v1/status"))
            .andExpect(status().isOk());
    }
}
