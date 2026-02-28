package com.webdev.bloggingsystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .maximumSessions(1).maxSessionsPreventsLogin(false).expiredUrl("/blog?logout")
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/loginError","/favicon.ico", "/*.css", "/*.js").permitAll()
                        .requestMatchers(HttpMethod.GET,"/blog/createPost").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/blog","/blog/**", "/blog/post/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/blog/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/blog/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/blog/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/blog")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/loginSuccess", true)
                        .failureUrl("/loginError")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/logoutSuccess")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}