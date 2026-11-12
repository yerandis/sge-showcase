package com.yerandis.sge.controller.security;

import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.security.AuthResponse;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.service.serviceInterface.security.AuthAppService;
import com.yerandis.sge.service.serviceInterface.security.ProfileAvatarAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.yerandis.sge.dto.request.security.ChangePasswordRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthAppService authAppService;
    private final ProfileAvatarAppService profileAvatarAppService;

    /**
     * POST /api/v1/auth/login
     * Ruta pública (SecurityConfig.PUBLIC_ROUTES)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authAppService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    /**
     * POST /api/v1/auth/register
     * En producción: proteger con hasRole('ADMIN')
     */
    // Cambiar de público a protegido (solo ADMIN puede crear usuarios vía este endpoint)
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('USER_CREATE')")  // ← proteger
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado", authAppService.register(request)));
    }

    /**
     * POST /api/v1/auth/refresh
     * Body: { "refreshToken": "eyJ..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El refresh token es obligatorio"));
        }
        AuthResponse response = authAppService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token renovado", response));
    }

    /**
     * POST /api/v1/auth/logout
     * Con JWT: el logout es responsabilidad del cliente (borrar el token).
     * Este endpoint confirma el logout y puede usarse para registrar el evento.
     */
//    @PostMapping("/logout")
//    public ResponseEntity<ApiResponse<Void>> logout() {
//        return ResponseEntity.ok(ApiResponse.success("Sesión cerrada exitosamente"));
//    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
        authAppService.logout(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Sesión cerrada exitosamente"));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        authAppService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada correctamente"));
    }

    @PostMapping(
            value = "/profile/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String avatarUrl = profileAvatarAppService.upload(
                authentication.getName(),
                file
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Foto de perfil actualizada",
                        Map.of("avatarUrl", avatarUrl)
                )
        );
    }
}