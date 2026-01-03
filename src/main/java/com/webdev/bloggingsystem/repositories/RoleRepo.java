package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Role;
import com.webdev.bloggingsystem.entities.RoleType;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepo extends CrudRepository<Role, Integer> {
    Role findByRole(RoleType role);
}
