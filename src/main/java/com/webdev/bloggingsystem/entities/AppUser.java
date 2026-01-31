package com.webdev.bloggingsystem.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Table("users")
public class AppUser {
    @Id
    private Integer id;

    private String username;

    private String password;

    private String email;

    private boolean isActive;

    private LocalDate dateCreated;

    @MappedCollection(idColumn = "user_id")
    private Set<UsersRoles> roleIds;

    public AppUser() {}

    public AppUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.isActive = true;
        this.dateCreated = LocalDate.now();
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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

    public Set<UsersRoles> getRoleIds() {
        return roleIds;
    }
    public void addRole(Role role) {
        if (this.roleIds == null) {
            this.roleIds = new HashSet<>();
        }
        this.roleIds.add(new UsersRoles(role.getId()));
    }

    public void removeRole(Role role) {
        if (this.roleIds != null) {
            this.roleIds.remove(new UsersRoles(role.getId()));
        }
    }


    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + dateCreated +
                ", roleIds=" + roleIds +
                '}';
    }
}
