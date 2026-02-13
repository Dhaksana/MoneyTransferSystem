// src/main/java/com/bd/controller/AuthController.java
package com.bd.controller;

import com.bd.dto.LoginRequest;
import com.bd.dto.LoginResponse;
import com.bd.service.AuthService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = { RequestMethod.POST, RequestMethod.OPTIONS },
        allowCredentials = "true",
        maxAge = 3600
)
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<LoginResponse> resp = auth.login(request);
        return resp.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).body(
                        new Object() { public final String message = "Invalid account id / holder name, or inactive account"; }
                ));
    }

        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String holderName) {
        Optional<LoginResponse> resp = auth.register(username, password, holderName);
        return resp.<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(400).body(
                new Object() { public final String message = "Registration failed (username may exist or password does not meet rules: min 8 chars, 1 uppercase, 1 symbol)"; }
            ));
        }
}