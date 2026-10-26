package com.cura.auth.controller;

import com.cura.auth.dto.LoginRequest;
import com.cura.auth.dto.RefreshTokenRequest;
import com.cura.auth.dto.RegisterRequest;
import com.cura.auth.dto.TokenResponse;
import com.cura.auth.security.JwtAuthenticationFilter;
import com.cura.auth.security.UserPrincipal;
import com.cura.auth.service.AuthService;
import com.cura.common.config.SecurityConfig;
import com.cura.user.dto.UserResponse;
import com.cura.user.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthFilter;

    @Test
    void register_withValidPayload_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "patient@cura.local",
                "StrongP@ss123",
                Role.PATIENT,
                "Priya",
                "Sharma",
                "+919876543210"
        );
        TokenResponse tokens = new TokenResponse(1L, "patient@cura.local", Role.PATIENT, "jwt_acc", "jwt_ref");

        when(authService.register(any())).thenReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("patient@cura.local"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt_acc"));
    }

    @Test
    void register_withInvalidEmail_returns400ValidationFailed() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "not-an-email",
                "short",
                Role.PATIENT,
                "",
                "",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void login_withValidCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest("patient@cura.local", "StrongP@ss123");
        TokenResponse tokens = new TokenResponse(1L, "patient@cura.local", Role.PATIENT, "jwt_acc", "jwt_ref");

        when(authService.login(any())).thenReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt_acc"));
    }

    @Test
    void refresh_withValidToken_returns200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("ref_token_123");
        TokenResponse tokens = new TokenResponse(1L, "patient@cura.local", Role.PATIENT, "jwt_acc_new", "jwt_ref_new");

        when(authService.refreshToken(any())).thenReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt_acc_new"));
    }

    @Test
    void me_whenAuthenticated_returnsCurrentUser() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "patient@cura.local", "", Role.PATIENT, true);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        UserResponse userResponse = new UserResponse(
                1L,
                "patient@cura.local",
                Role.PATIENT,
                true,
                false,
                "Priya",
                "Sharma",
                "+919876543210",
                null,
                null,
                null,
                null,
                null
        );

        when(authService.getCurrentUser(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("patient@cura.local"))
                .andExpect(jsonPath("$.data.firstName").value("Priya"));
    }
}
