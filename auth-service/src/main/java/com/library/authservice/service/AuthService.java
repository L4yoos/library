package com.library.authservice.service;

import com.library.authservice.dto.JwtResponse;
import com.library.authservice.dto.LoginRequest;
import com.library.authservice.dto.RegistrationRequest;

public interface AuthService {
    void registerUser(RegistrationRequest request);
    JwtResponse authenticateUser(LoginRequest request);
}