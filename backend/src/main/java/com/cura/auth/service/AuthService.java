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
import com.cura.user.entity.User;
import com.cura.user.entity.UserProfile;
import com.cura.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service orchestrating authentication, user registration, JWT generation,
 * refresh token rotation, and session termination.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new user account and associated profile, issuing initial JWT access and refresh tokens.
     *
     * @param request user registration payload
     * @return token response containing access and refresh tokens
     * @throws ConflictException if the email is already registered
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ConflictException("Email " + request.email() + " is already registered");
        }

        User user = new User(
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        UserProfile profile = new UserProfile(
                user,
                request.firstName().trim(),
                request.lastName().trim(),
                request.phone() != null ? request.phone().trim() : null
        );
        user.setProfile(profile);

        User savedUser = userRepository.save(user);

        return issueTokens(savedUser);
    }

    /**
     * Authenticates user credentials and issues a fresh access and refresh token pair.
     *
     * @param request login payload containing email and plaintext password
     * @return token response containing access and refresh tokens
     * @throws AccessDeniedException if credentials are invalid or account is deactivated
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new AccessDeniedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AccessDeniedException("Account has been deactivated");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AccessDeniedException("Invalid email or password");
        }

        return issueTokens(user);
    }

    /**
     * Rotates a valid refresh token and issues a new access token.
     *
     * @param request refresh token payload
     * @return rotated token response
     * @throws AccessDeniedException if token is invalid, expired, or revoked
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken currentToken = refreshTokenRepository.findValidByToken(request.refreshToken())
                .orElseThrow(() -> new AccessDeniedException("Invalid or revoked refresh token"));

        if (currentToken.isExpired()) {
            currentToken.setRevoked(true);
            refreshTokenRepository.save(currentToken);
            throw new AccessDeniedException("Refresh token has expired");
        }

        User user = currentToken.getUser();
        if (!user.isActive()) {
            throw new AccessDeniedException("Account has been deactivated");
        }

        // Revoke the old token (rotation policy)
        currentToken.setRevoked(true);
        refreshTokenRepository.save(currentToken);

        return issueTokens(user);
    }

    /**
     * Revokes all active refresh tokens for the given user, effectively logging them out.
     *
     * @param userId ID of the user logging out
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Retrieves the profile and metadata for the current user.
     *
     * @param userId user ID from security context
     * @return user profile details
     * @throws ResourceNotFoundException if user does not exist
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return UserResponse.fromEntity(user);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshTokenString = tokenProvider.generateRefreshTokenString();
        Instant expiry = tokenProvider.calculateRefreshTokenExpiry();

        RefreshToken refreshToken = new RefreshToken(user, refreshTokenString, expiry);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshTokenString
        );
    }
}
