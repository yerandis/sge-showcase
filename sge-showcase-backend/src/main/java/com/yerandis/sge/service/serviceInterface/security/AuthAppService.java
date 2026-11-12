package com.yerandis.sge.service.serviceInterface.security;

import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.security.AuthResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface AuthAppService {
    AuthResponse login(@Valid LoginRequest request);

    AuthResponse register(@Valid RegisterRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(UUID id);

    void changePassword(String username, String currentPassword, String newPassword);
}
