package com.bd.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @Test
    void securityConfigCreatesBeans() {
        SecurityConfig config = new SecurityConfig(userDetailsService, jwtUtil);
        assertNotNull(config.passwordEncoder());
        assertNotNull(config.authenticationManager());
    }
}
