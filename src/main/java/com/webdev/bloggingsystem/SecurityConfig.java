package com.webdev.bloggingsystem;

import com.webdev.bloggingsystem.errorHandling.LoginFailureHandler;
import com.webdev.bloggingsystem.user.BlogSystemUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider passwordFirstDaoProvider, LoginFailureHandler loginFailureHandler) {
        http
                .authenticationProvider(passwordFirstDaoProvider)
                .csrf(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/blog/post/createPost",
                                "/blog/post/editPost",
                                "/blog/post/editPost/{id}",
                                "/blog/post/deletePost/{id}",
                                "/blog/postComponent/validateSize",
                                "/blog/blogComponent/posts/inProgress",
                                "/categories",
                                "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(
                                "/comment/commentComponent/editForm",
                                "/comment/createComment",
                                "/comment/editComment",
                                "/comment/delete").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/blog?logout")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, _) -> {
                            response.sendRedirect("https://" + request.getServerName() + "/loginSuccess");
                        })
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, _) -> {
                            response.sendRedirect("https://" + request.getServerName() + "/logoutSuccess");
                        })
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /**
     * Sets UserDetailsChecker to check nothing in preAuthChecks (locked, expired, disabled) and sets disabled check after password check (post-auth).
     * Not using the other checks from UserDetails. (Just setting to true in UserDetailsService)
     * */
    @Bean
    public AuthenticationProvider passwordFirstDaoProvider(BlogSystemUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setPreAuthenticationChecks(_ -> {});
        provider.setPostAuthenticationChecks(user -> {
            if (!user.isEnabled())
                throw new DisabledException("Account is disabled");
        });
        return provider;
    }
}