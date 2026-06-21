package com.bd.dto;

import java.util.List;

import com.bd.model.AppUser;

public record UserDetailDTO(
    Long id, String username, String fullName, String email,
    String role, String status, String accountId, String displayName,
    List<AccountDTO> accounts
) {
    public static UserDetailDTO from(AppUser user, List<AccountDTO> accounts) {
        return new UserDetailDTO(
            user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
            user.getRole(), user.getStatus(), user.getAccountId(), user.getDisplayName(),
            accounts
        );
    }
}
