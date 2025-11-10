package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppUserRepo extends CrudRepository<AppUser, Integer> {
    @EntityGraph(value = "eager-fetch-roles", type = EntityGraph.EntityGraphType.LOAD)
    Optional<AppUser> findByUsername(String username);

    Boolean existsByUsername(String username);
}
