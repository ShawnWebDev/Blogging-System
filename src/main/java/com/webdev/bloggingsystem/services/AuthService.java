package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.dto.UserProfile;

public interface AuthService {
    String login(LoginDto loginDto);
    void register(LoginDto loginDto, String email);
    UserProfile getUserProfile();
}
