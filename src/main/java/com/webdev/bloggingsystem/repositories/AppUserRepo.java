package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepo extends Repository<AppUser, Integer> {
    @Query("SELECT u FROM AppUser u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<AppUser> findByUsername(@Param("username") String username);

    Boolean existsByUsername(String username);
}
