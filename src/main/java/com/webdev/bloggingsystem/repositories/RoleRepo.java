package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Role;
import com.webdev.bloggingsystem.entities.RoleType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RoleRepo extends CrudRepository<Role, Integer> {
    Role findByRole(RoleType role);

    @Query("SELECT * FROM roles WHERE roles.id IN (:roleIds)")
    Set<Role> findAllByIdIn(@Param("roleIds") Set<Integer> roleIds);
}
