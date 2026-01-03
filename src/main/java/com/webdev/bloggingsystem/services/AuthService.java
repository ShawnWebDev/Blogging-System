package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.dto.RegisterDto;
import com.webdev.bloggingsystem.dto.UserProfile;

public interface AuthService {
    String login(LoginDto loginDto);
    void register(RegisterDto registerDto);
    UserProfile getUserProfile();
}
