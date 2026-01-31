package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.entities.*;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CategoryRepo;
import com.webdev.bloggingsystem.repositories.RoleRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class BloggingSystemApplication {

    static void main(String[] args) {
        SpringApplication.run(BloggingSystemApplication.class, args);
    }

    @Bean
    @Transactional
    public CommandLineRunner commandLineRunner(AppUserRepo appUserRepo) {
        return args -> {
            //add initial users
            /*
            Role adminRole = roleRepo.findByRole(RoleType.ADMIN);
            Role userRole = roleRepo.findByRole(RoleType.USER);
            AppUser me = new AppUser(
                "Shawn",
                    passwordEncoder.encode("blue.truck1"),
                    "email@email.com"
            );
            me.addRole(adminRole);
            me.addRole(userRole);
            appUserRepo.save(me);
            AppUser bailey = new AppUser(
                    "bailey",
                    passwordEncoder.encode("squeaker1"),
                    "email@email.com"
            );
            bailey.addRole(userRole);
            appUserRepo.save(bailey);*/


        };
    }

}
