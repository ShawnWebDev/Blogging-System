package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RsaKeyProperties.class})
public class BloggingSystemApplication {

    static void main(String[] args) {
        SpringApplication.run(BloggingSystemApplication.class, args);
    }

}
