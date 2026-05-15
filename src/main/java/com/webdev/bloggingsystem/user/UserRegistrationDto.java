package com.webdev.bloggingsystem.user;

import com.webdev.bloggingsystem.errorHandling.MatchingPassword;
import com.webdev.bloggingsystem.errorHandling.UniqueEmail;
import com.webdev.bloggingsystem.errorHandling.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@MatchingPassword
public class UserRegistrationDto {
    @NotBlank
    @Size(min = 4, max = 64)
    @UniqueUsername
    private String username;

    @NotBlank
    @Size(max = 255)
    @Email
    @UniqueEmail
    private String email;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.+\\s).+$",
            message = "must have at least 1 digit, 1 uppercase, 1 lowercase. No spaces.")
    @Pattern(regexp = "^(?=.*[.!@#*+_-]).+$",
            message = "must have 1 special character of @#.!*+_-")
    private String password;

    @NotBlank
    private String confirmPassword;

    public UserRegistrationDto() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
