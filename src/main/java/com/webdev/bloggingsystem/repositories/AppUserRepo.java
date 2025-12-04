package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.AppUser;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppUserRepo extends JpaRepository<AppUser, Integer> {
    @Query("SELECT u FROM AppUser u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<AppUser> findAppUserAndRolesByUsername(@Param("username") String username);

    @Query("SELECT u.username FROM AppUser u WHERE u.id = :id")
    String findUsernameById(@Param("id") Integer id);

    @Query("SELECT u.id AS userId, u.username AS username " +
            "FROM AppUser u " +
            "WHERE u.id IN :ids")
    List<Tuple> findUsernamesById(@Param("ids") Set<Integer> ids);

    @Query("SELECT u.id FROM AppUser u WHERE u.username = :username")
    Integer getIdByUsername(@Param("username") String username);
}