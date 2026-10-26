package com.cura.auth.service;

import com.cura.auth.dto.LoginRequest;
import com.cura.auth.dto.RefreshTokenRequest;
import com.cura.auth.dto.RegisterRequest;
import com.cura.auth.dto.TokenResponse;
import com.cura.auth.entity.RefreshToken;
import com.cura.auth.repository.RefreshTokenRepository;
import com.cura.auth.security.JwtTokenProvider;
import com.cura.common.exception.AccessDeniedException;
import com.cura.common.exception.ConflictException;
import com.cura.common.exception.ResourceNotFoundException;
import com.cura.user.dto.UserResponse;
import com.cura.user.entity.Role;
import com.cura.user.entity.User;
import com.cura.user.entity.UserProfile;
import com.cura.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void register_whenEmailNotTaken_createsUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest(
                "patient@cura.local",
                "StrongP@ss123",
                Role.PATIENT,
                "Priya",
                "Sharma",
                "+919876543210"
        );

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        when(tokenProvider.generateAccessToken(eq(10L), eq("patient@cura.local"), eq(Role.PATIENT)))
                .thenReturn("access_token_abc");
        when(tokenProvider.generateRefreshTokenString()).thenReturn("refresh_token_xyz");
        when(tokenProvider.calculateRefreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(604800));

        TokenResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(10L, response.userId());
        assertEquals("patient@cura.local", response.email());
        assertEquals(Role.PATIENT, response.role());
        assertEquals("access_token_abc", response.accessToken());
        assertEquals("refresh_token_xyz", response.refreshToken());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_whenEmailAlreadyExists_throwsConflictException() {
        RegisterRequest request = new RegisterRequest(
                "existing@cura.local",
                "StrongP@ss123",
                Role.PATIENT,
                "John",
                "Doe",
                null
        );

        when(userRepository.existsByEmailIgnoreCase("existing@cura.local")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("doctor@cura.local", "Doctor@123");

        User user = new User("doctor@cura.local", "hashed_password", Role.DOCTOR);
        user.setId(5L);

        when(userRepository.findByEmailIgnoreCase("doctor@cura.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Doctor@123", "hashed_password")).thenReturn(true);
        when(tokenProvider.generateAccessToken(5L, "doctor@cura.local", Role.DOCTOR)).thenReturn("doc_jwt");
        when(tokenProvider.generateRefreshTokenString()).thenReturn("doc_refresh");
        when(tokenProvider.calculateRefreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(604800));

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("doc_jwt", response.accessToken());
        assertEquals(Role.DOCTOR, response.role());
    }

    @Test
    void login_withWrongPassword_throwsAccessDeniedException() {
        LoginRequest request = new LoginRequest("doctor@cura.local", "WrongPassword");

        User user = new User("doctor@cura.local", "hashed_password", Role.DOCTOR);
        when(userRepository.findByEmailIgnoreCase("doctor@cura.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed_password")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> authService.login(request));
    }

    @Test
    void login_whenUserDoesNotExist_throwsAccessDeniedException() {
        LoginRequest request = new LoginRequest("unknown@cura.local", "Secret");

        when(userRepository.findByEmailIgnoreCase("unknown@cura.local")).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> authService.login(request));
    }

    @Test
    void login_whenAccountIsDeactivated_throwsAccessDeniedException() {
        LoginRequest request = new LoginRequest("deactivated@cura.local", "Secret");

        User user = new User("deactivated@cura.local", "hashed_password", Role.PATIENT);
        user.setActive(false);

        when(userRepository.findByEmailIgnoreCase("deactivated@cura.local")).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_whenValid_revokesOldAndIssuesNew() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid_refresh_token");

        User user = new User("patient@cura.local", "hash", Role.PATIENT);
        user.setId(8L);

        RefreshToken token = new RefreshToken(user, "valid_refresh_token", Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findValidByToken("valid_refresh_token")).thenReturn(Optional.of(token));
        when(tokenProvider.generateAccessToken(8L, "patient@cura.local", Role.PATIENT)).thenReturn("new_access");
        when(tokenProvider.generateRefreshTokenString()).thenReturn("new_refresh");
        when(tokenProvider.calculateRefreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(604800));

        TokenResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access", response.accessToken());
        assertEquals("new_refresh", response.refreshToken());
        assertTrue(token.isRevoked());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void logout_revokesUserTokens() {
        authService.logout(15L);
        verify(refreshTokenRepository).revokeAllByUserId(15L);
    }

    @Test
    void getCurrentUser_whenExists_returnsProfile() {
        User user = new User("priya@cura.local", "hash", Role.PATIENT);
        user.setId(22L);
        UserProfile profile = new UserProfile(user, "Priya", "Sharma", "+919999999999");
        user.setProfile(profile);

        when(userRepository.findByIdWithProfile(22L)).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser(22L);

        assertNotNull(response);
        assertEquals(22L, response.id());
        assertEquals("priya@cura.local", response.email());
        assertEquals("Priya", response.firstName());
        assertEquals("Sharma", response.lastName());
    }

    @Test
    void getCurrentUser_whenNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByIdWithProfile(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser(999L));
    }
}
