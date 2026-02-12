package com.bd.dto;

public class LoginResponse {
    private boolean authenticated;
    private String token; // demo token
    private UserInfo user;

    public LoginResponse(boolean authenticated, String token, UserInfo user) {
        this.authenticated = authenticated;
        this.token = token;
        this.user = user;
    }

    public boolean isAuthenticated() { return authenticated; }
    public String getToken() { return token; }
    public UserInfo getUser() { return user; }

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