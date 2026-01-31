package com.webdev.bloggingsystem.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("roles")
public class Role {
    @Id
    private Integer id;

    private RoleType role;

    public Role() {}

    public Role(RoleType role) {
        this.role = role;
    }
    public Integer getId() {
        return id;
    }
    public RoleType getRole() {
        return role;
    }
    public void setRole(RoleType role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", role=" + role +
                '}';
    }

    // compares only id fields, returns false for entities not persisted - where this.id == null
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass() || this.getId() == null) return false;
        Role role = (Role) o;
        return Objects.equals(this.getId(), role.getId());
    }

    // sets hashcode for entities not persisted to 31 as temporary fallback
    @Override
    public int hashCode() {
        if (this.getId() == null) return 31;
        return this.getId().hashCode();
    }
}
