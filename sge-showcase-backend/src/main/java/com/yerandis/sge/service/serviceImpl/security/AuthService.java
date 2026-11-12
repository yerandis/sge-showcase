package com.yerandis.sge.service.serviceImpl.security;

import com.yerandis.sge.dto.request.security.LoginRequest;
import com.yerandis.sge.dto.request.security.RegisterRequest;
import com.yerandis.sge.dto.response.security.AuthResponse;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.entity.system.Employee;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.repository.admin.UserRepository;
import com.yerandis.sge.security.JwtService;
import com.yerandis.sge.security.LoginAttemptService;
import com.yerandis.sge.service.serviceImpl.system.AuditLogService;
import com.yerandis.sge.service.serviceInterface.security.AuthAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * AuthService: lógica de negocio de autenticación.
 *
 * Dependencias:
 *   AuthService → UserRepository
 *   AuthService → JwtService
 *   AuthService → AuthenticationManager (de ApplicationConfig)
 *   AuthService → PasswordEncoder (de ApplicationConfig)
 *   AuthService → UserDetailsService (de ApplicationConfig)
 *
 * Ninguna de estas crea ciclos porque ApplicationConfig
 * no depende de AuthService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthAppService {

    private static final String ENTITY_TYPE = "USER";

    private final UserRepository       userRepository;
    private final EmployeeRepository   employeeRepository;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder      passwordEncoder;
    private final UserDetailsService   userDetailsService;
    private final RoleRepository       roleRepository;
    private final LoginAttemptService  loginAttemptService;
    private final AuditLogService      auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String key = request.getUsername().toLowerCase();

        if (loginAttemptService.isBlocked(key)) {
            throw new BusinessException("AUTH_LOCKED",
                    "Demasiados intentos fallidos. Intenta de nuevo en unos minutos.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            loginAttemptService.recordSuccess(key);

            User user = (User) userDetailsService.loadUserByUsername(request.getUsername());
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return buildAuthResponse(user);

        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(key);
            log.warn("Intento de login fallido para usuario '{}': {}", request.getUsername(), e.getMessage());
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "Usuario o contraseña incorrectos");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USER_ALREADY_EXISTS",
                    "El usuario '" + request.getUsername() + "' ya existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("USER_ALREADY_EXISTS",
                    "Ya existe una cuenta con el email '" + request.getEmail() + "'");
        }

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("", "Empleado no encontrado"));
        }

        Set<Role> defaultRoles = new HashSet<>();
        roleRepository.findByIsDefaultTrue().ifPresent(defaultRoles::add);

        User user = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(defaultRoles)
                .employee(employee)
                .active(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new BusinessException("", "Token de refresco inválido o expirado");
        }

        String username = jwtService.extractUsername(refreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = (User) userDetails;

        return buildAuthResponse(user);
    }

//    public AuthResponse refreshToken(String refreshTokenValue) {
//        if (!jwtService.isTokenValid(refreshTokenValue)) {
//            throw new BusinessException("AUTH_INVALID_REFRESH_TOKEN", "Token de refresco inválido o expirado");
//        }
//        String username = jwtService.extractUsername(refreshTokenValue);
//        User user = (User) userDetailsService.loadUserByUsername(username);
//
//        int tokenVersion = jwtService.extractTokenVersion(refreshTokenValue);
//        if (tokenVersion != user.getTokenVersion()) {
//            throw new BusinessException("AUTH_INVALID_REFRESH_TOKEN", "La sesión fue cerrada. Inicia sesión de nuevo.");
//        }
//        return buildAuthResponse(user);
//    }
//
    @Transactional
    public void logout(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
//            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {

        //  AUDITORIA: registro de la actualizacion de este usuario
        String status = "SUCCESS";

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "AUTH_INVALID_CREDENTIALS",
                        "No se pudo validar el usuario"
                ));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {

            status = "FAILED";

            auditLogService.create(ENTITY_TYPE, user.getId(),
                    "UPDATE",
                    "CREATED",
                    "UPDATED",
                    "USERS",
                    status
            );
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "La contraseña actual es incorrecta");
        }

        if (newPassword == null || newPassword.isBlank()) {

            status = "FAILED";

            auditLogService.create(ENTITY_TYPE, user.getId(),
                    "UPDATE",
                    "CREATED",
                    "UPDATED",
                    "USERS",
                    status
            );
            throw new BusinessException("PASSWORD_INVALID", "La nueva contraseña es obligatoria");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {

            status = "FAILED";

            auditLogService.create(ENTITY_TYPE, user.getId(),
                    "UPDATE",
                    "CREATED",
                    "UPDATED",
                    "USERS",
                    status
            );
            throw new BusinessException("PASSWORD_INVALID", "La nueva contraseña debe ser diferente de la actual");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditLogService.create(ENTITY_TYPE, user.getId(),
                "UPDATE",
                "CREATED",
                "UPDATED",
                "USERS",
                status
        );

    }


    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user, user.getTokenVersion());
        String refreshToken = jwtService.generateRefreshToken(user);

        List<String> permissions = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        List<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .sorted()
                .toList();

        UUID employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .roles(roleNames)
                        .permissions(permissions)
                        .employeeId(employeeId)
                        .avatarUrl(user.getAvatarUrl())
                        .email(user.getEmail())
                        .fullName(user.getFirstName() + " " + user.getLastName())
                        .build())
                .build();
    }
}
