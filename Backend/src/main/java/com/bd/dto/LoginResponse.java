package com.bd.dto;

public class LoginResponse {
    private boolean authenticated;
    private String token; // demo token
    private UserInfo user;
    private String role;

    public LoginResponse(boolean authenticated, String token, UserInfo user, String role) {
        this.authenticated = authenticated;
        this.token = token;
        this.user = user;
        this.role = role;
    }

    public boolean isAuthenticated() { return authenticated; }
    public String getToken() { return token; }
    public UserInfo getUser() { return user; }
    public String getRole() { return role; }

    public static class UserInfo {
        private String id;
        private String name;

        public UserInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
        public String getId() { return id; }
        public String getName() { return name; }
    }
}