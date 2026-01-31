package com.webdev.bloggingsystem.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users_roles")
public record UsersRoles(@Column("role_id") Integer roleId) {
}
