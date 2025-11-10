package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.entities.LoginDto;

public interface AuthService {
    String login(LoginDto loginDto);
    void register(LoginDto loginDto, String email);
}
