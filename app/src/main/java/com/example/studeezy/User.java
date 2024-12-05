package com.example.studeezy;

public class User {
    private String id;
    private String name;
    private String email;
    private Boolean hasPremium;
    private Long expirationDate;
    private Long created_at;

    public User(String id, String name, String email, Boolean hasPremium, Long expirationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hasPremium = hasPremium;
        this.expirationDate = expirationDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Boolean isHasPremium() {
        return hasPremium;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public Long getCreatedAt() {
        return  created_at;
    }
}
