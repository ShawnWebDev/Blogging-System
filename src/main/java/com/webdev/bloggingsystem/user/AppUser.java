package com.webdev.bloggingsystem.user;

import java.time.LocalDate;

public class AppUser {
    private Integer id;

    private String username;

    private String password;

    private boolean isActive;

    private LocalDate dateCreated;

    private RoleType role;

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static AppUser createUser(String username, String password) {
        AppUser appUser = new AppUser(username, password);
        appUser.isActive(true);
        appUser.setDateCreated(LocalDate.now());
        appUser.setRole(RoleType.USER);
        return appUser;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean getIsActive() {
        return isActive;
    }
    public void isActive(boolean isActive) {
        this.isActive = isActive;
    }
    public LocalDate getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }
    public RoleType getRole() {
        return role;
    }
    public void setRole(RoleType role) {
        this.role = role;
    }


    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + dateCreated +
                ", role=" + role +
                '}';
    }
}
