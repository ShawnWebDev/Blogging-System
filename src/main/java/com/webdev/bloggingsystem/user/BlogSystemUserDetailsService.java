package com.webdev.bloggingsystem.user;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;

@Service
public class BlogSystemUserDetailsService implements UserDetailsService {

    private final AppUserDao appUserDao;

    public BlogSystemUserDetailsService(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        RoleType roleType = appUser.getRole();
        Collection<GrantedAuthority> authorities = new HashSet<>();

        if (roleType.equals(RoleType.ADMIN)) {
            authorities.add(new SimpleGrantedAuthority(RoleType.USER.name()));
        }
        authorities.add(new SimpleGrantedAuthority(appUser.getRole().name()));

        return new User(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.getIsActive(),
                true,
                true,
                true,
                authorities
        );
    }
}
