package com.bd.dto;

import com.bd.model.AppUser;

public record AdminUserDTO(Long id, String username, String fullName, String email, String role, String status, String accountId, String displayName) {
    public static AdminUserDTO from(AppUser user) {
        return new AdminUserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole(), user.getStatus(), user.getAccountId(), user.getDisplayName());
    }
}
