package com.cura.user.dto;

import com.cura.user.entity.Role;
import com.cura.user.entity.User;
import com.cura.user.entity.UserProfile;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean isActive,
        boolean emailVerified,
        String firstName,
        String lastName,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        String bloodGroup,
        String address,
        String profileImagePath
) {
    public static UserResponse fromEntity(User user) {
        UserProfile p = user.getProfile();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                p != null ? p.getFirstName() : null,
                p != null ? p.getLastName() : null,
                p != null ? p.getPhone() : null,
                p != null ? p.getDateOfBirth() : null,
                p != null ? p.getGender() : null,
                p != null ? p.getBloodGroup() : null,
                p != null ? p.getAddress() : null,
                p != null ? p.getProfileImagePath() : null
        );
    }
}
