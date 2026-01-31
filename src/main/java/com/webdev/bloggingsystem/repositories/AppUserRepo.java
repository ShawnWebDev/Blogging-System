package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.AppUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppUserRepo extends CrudRepository<AppUser, Integer> {
    Optional<AppUser> findByUsername(String username);

}
