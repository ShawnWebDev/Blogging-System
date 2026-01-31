package com.webdev.bloggingsystem;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BloggingSystemApplication {

    static void main(String[] args) {
        SpringApplication.run(BloggingSystemApplication.class, args);
    }

/*    @Bean
    @Transactional
    public CommandLineRunner commandLineRunner() {
        return args -> {
            //add initial users
            *//*
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
            appUserRepo.save(bailey);*//*
        };
    }*/

}
