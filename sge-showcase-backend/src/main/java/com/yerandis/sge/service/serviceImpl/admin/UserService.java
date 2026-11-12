package com.yerandis.sge.service.serviceImpl.admin;

import com.yerandis.sge.dto.request.admin.UserRequest;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.admin.UserHistoryResponse;
import com.yerandis.sge.dto.response.admin.UserResponse;
import com.yerandis.sge.dto.response.system.Changes;
import com.yerandis.sge.entity.admin.AuditLog;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.entity.system.Employee;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.mapper.admin.UserMapper;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.repository.admin.UserRepository;
import com.yerandis.sge.repository.system.AuditLogRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.service.serviceImpl.system.AuditLogService;
import com.yerandis.sge.service.serviceInterface.admin.UserAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserAppService {

    private static final String ENTITY_TYPE = "USER";

    private final UserRepository        userRepository;
    private final RoleRepository        roleRepository;      // ← para buscar roles por ID
    private final EmployeeRepository    employeeRepository;  // ← para vincular empleado
    private final AuditLogRepository    auditLogRepository;
    private final UserMapper            userMapper;
    private final PasswordEncoder       passwordEncoder;     // ← para hashear contraseña
    private final AuditLogService       auditLogService;

    // ── findAll ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(String search, Pageable pageable) {
        Page<User> userPage = userRepository.searchUser(search, pageable);
        Page<UserResponse> responsePage = userPage.map(userMapper::entityToResponse);
        return new PageResponse<>(responsePage);
    }

    // ── findById ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return userMapper.entityToResponse(user);
    }

    // ── create ────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        // Validación: username único
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new BusinessException("USER_ALREADY_EXISTS",
                    "Ya existe un usuario con el nombre: " + request.getUsername()
            );
        }

        //  Validacion: email unico.
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("USER_ALREADY_EXISTS", "Ya existe un usuario con el email: " + request.getEmail()
            );
        }

        // Validación: la contraseña es obligatoria al crear
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("", "La contraseña es obligatoria al crear un usuario");
        }

        // Construir la entidad base
        User user = userMapper.requestToEntity(request);

        // Hashear la contraseña ANTES de guardar
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Buscar y asignar roles por ID
        Set<Role> roles = resolveRoles(request.getRoleIds());
        user.setRoles(roles);

        // Vincular empleado si se especificó
        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND",
                            "Empleado no encontrado con ID: " + request.getEmployeeId()
                    ));
            user.setEmployee(employee);
        }

        User savedUser = userRepository.save(user);

        //  AUDITORIA: registro de la creacion de este usuario
        String status = "SUCCESS";
        if (savedUser == null) status = "FAILED";

        auditLogService.create(
                ENTITY_TYPE,
                savedUser.getId(),
                "CREATE",
                null,
                snapshotUser(savedUser),
                "USERS",
                "SUCCESS"
        );


        return userMapper.entityToResponse(savedUser);
    }

    // ── update ────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // Guardar el estado anterior antes de modificar la entidad.
        String oldSnapshot = snapshotUser(user);

        // Verificar username único (excluyendo el usuario actual)
        String newUsername = request.getUsername().trim().toLowerCase();
        if (!user.getUsername().equals(newUsername) &&
                userRepository.existsByUsername(newUsername)) {
            throw new BusinessException("USER_ALREADY_EXISTS",
                    "Ya existe otro usuario con el nombre: " + request.getUsername()
            );
        }

        // Actualizar campos básicos (el mapper no hashea la contraseña)
        userMapper.updateEntityFromRequest(request, user);

        // Si viene contraseña nueva, hashearla
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Actualizar roles
        user.setRoles(resolveRoles(request.getRoleIds()));

        // Actualizar empleado vinculado
        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND",
                            "Empleado no encontrado con ID: " + request.getEmployeeId()
                    ));
            user.setEmployee(employee);
        } else {
            user.setEmployee(null); // desvincular si se envía null
        }

        User updatedUser = userRepository.save(user);

        //  AUDITORIA: registro de la actualizacion de este usuario
        String status = "SUCCESS";
        if (updatedUser == null) status = "FAILED";

        auditLogService.create(
                ENTITY_TYPE,
                updatedUser.getId(),
                "UPDATE",
                oldSnapshot,
                snapshotUser(updatedUser),
                "USERS",
                "SUCCESS"
        );


        return userMapper.entityToResponse(updatedUser);
    }

    // ── delete ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID id) {
        // CORRECCIÓN: la condición estaba invertida
        // Antes: if (existsById) throw → lanzaba excepción si el usuario SÍ existía
        // Ahora: if (!existsById) throw → lanza excepción si NO existe
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", id);
        }

        // Cargar para obtener el nombre antes de eliminar (para la notificación)
        User toDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        String oldSnapshot = snapshotUser(toDelete);
        String username = toDelete.getUsername();
        userRepository.deleteById(id);

        //  AUDITORIA: registro de la eliminacion de este usuario
        String status = "SUCCESS";
        if (toDelete != null) status = "FAILED";

        auditLogService.create(
                ENTITY_TYPE,
                id,
                "DELETE",
                oldSnapshot,
                null,
                "USERS",
                "SUCCESS"
        );

    }

    // ── toggleActive ──────────────────────────────────────────────
    // Método adicional útil para activar/desactivar sin editar todo el usuario

    @Transactional
    public UserResponse toggleActive(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        String oldSnapshot = snapshotUser(user);
        user.setActive(!user.isActive());

        User    updatedUser = userRepository.save(user);
        String  title       = updatedUser.isActive() ? "Usuario activado" : "Usuario desactivado";

        //  AUDITORIA: registro de la actualizacion de este usuario
        String status = "SUCCESS";
        if (updatedUser == null) status = "FAILED";

        auditLogService.create(
                ENTITY_TYPE,
                updatedUser.getId(),
                "UPDATE",
                oldSnapshot,
                snapshotUser(updatedUser),
                "USERS",
                "SUCCESS"
        );

        return userMapper.entityToResponse(updatedUser);
    }

    // ── Helper privado: resolver roles por ID ─────────────────────

    private Set<Role> resolveRoles(List<UUID> roleIds) {

        if (roleIds == null || roleIds.isEmpty()) {
            if (roleRepository.findByIsDefaultTrue().isPresent()) {
                Role roleByDefault = roleRepository.findByIsDefaultTrue()
                        .get();
                List<Role> roleList = new ArrayList<>();
                roleList.add(roleByDefault);
                return new HashSet<>(roleList);
            }
        }
        List<Role> found = roleRepository.findAllById(roleIds);

        // Verificar que todos los IDs solicitados existen
        if (found.size() != roleIds.size()) {
            throw new BusinessException("", "Uno o más roles especificados no existen en el sistema");
        }

        return new HashSet<>(found);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserHistoryResponse> getHistory(UUID id) {
        //  verificar q el user existe
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", id);
        }

        List<AuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(ENTITY_TYPE, id);

        return logs.stream()
                .map(log -> new UserHistoryResponse(
                        log.getId(),
                        log.getAction(),
                        log.getUsername(),
                        parseChanges(log.getOldValue(), log.getNewValue()),
                        log.getCreatedAt(),
                        log.getStatus(),
                        log.getModule()
                ))
                .toList();
    }

    /**
     * Captura solo datos auditables del usuario.
     * Nunca incluir la contraseña ni su hash en el historial.
     */
    private String snapshotUser(User user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("username", user.getUsername());
        snapshot.put("email", user.getEmail());
        snapshot.put("firstName", user.getFirstName());
        snapshot.put("lastName", user.getLastName());
        snapshot.put("avatarUrl", user.getAvatarUrl());
        snapshot.put("active", user.isActive());

        snapshot.put(
                "roles",
                user.getRoles().stream()
                        .map(Role::getName)
                        .sorted()
                        .toList()
        );

        snapshot.put(
                "employee",
                user.getEmployee() != null
                        ? user.getEmployee().getId().toString()
                        : null
        );

        try {
            return new ObjectMapper().writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo generar la instantánea de auditoría", e
            );
        }
    }

    private List<Changes> parseChanges(String oldJson, String newJson) {

        // Una creación no tiene estado anterior y una eliminación no tiene estado nuevo.
        // Usamos mapas vacíos para poder presentar esos cambios en el historial.
        ObjectMapper mapper = new ObjectMapper();

        // Mapas vacíos para que las altas y bajas también generen diferencias.
        Map<String, Object> oldMap = oldJson == null
                ? Map.of()
                : mapper.readValue(oldJson, new TypeReference<>() {});

        Map<String, Object> newMap = newJson == null
                ? Map.of()
                : mapper.readValue(newJson, new TypeReference<>() {});

        try {

            Set<String> fields = new LinkedHashSet<>();

            // Unimos las claves de ambos estados para incluir altas y bajas.
            fields.addAll(oldMap.keySet());
            fields.addAll(newMap.keySet());

            return fields.stream()
                    .filter(field ->
                            !Objects.equals(oldMap.get(field), newMap.get(field)))
                    .map(field -> new Changes(
                            field,
                            Objects.toString(oldMap.get(field), ""),
                            Objects.toString(newMap.get(field), "")
                    ))
                    .toList();

        } catch (Exception e) {
            log.debug("Error: {}", e.getMessage());
            return List.of();
        }
    }
}
