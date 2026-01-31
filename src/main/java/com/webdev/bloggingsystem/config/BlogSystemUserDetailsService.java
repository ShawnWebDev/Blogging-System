package com.webdev.bloggingsystem.config;

import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.Role;
import com.webdev.bloggingsystem.entities.UsersRoles;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.RoleRepo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogSystemUserDetailsService implements UserDetailsService {
    private final AppUserRepo appUserRepo;
    private final RoleRepo roleRepo;

    public BlogSystemUserDetailsService(AppUserRepo appUserRepo, RoleRepo roleRepo) {
        this.appUserRepo = appUserRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        Set<Integer> roleIds = appUser.getRoleIds().stream().map(UsersRoles::roleId).collect(Collectors.toSet());
        Set<Role> roles = roleRepo.findAllByIdIn(roleIds);
        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRole().name()));
        }

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
