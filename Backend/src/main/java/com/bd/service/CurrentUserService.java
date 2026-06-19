package com.bd.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bd.model.AppUser;
import com.bd.repository.AppUserRepository;

@Service
public class CurrentUserService {
    private final AppUserRepository users;

    public CurrentUserService(AppUserRepository users) {
        this.users = users;
    }

    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication required");
        }
        return authentication.getName();
    }

    public AppUser user() {
        return users.findByUsername(username()).orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
