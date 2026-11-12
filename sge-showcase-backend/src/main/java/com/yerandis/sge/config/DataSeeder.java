//package com.yerandis.sge.config;
//
//import com.yerandis.sge.entity.admin.Permission;
//import com.yerandis.sge.entity.admin.Role;
//import com.yerandis.sge.entity.admin.User;
//import com.yerandis.sge.repository.admin.PermissionRepository;
//import com.yerandis.sge.repository.admin.RoleRepository;
//import com.yerandis.sge.repository.admin.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * DataSeeder: crea los datos iniciales minimos (permisos, roles ADMIN y
// * USER, usuario admin) si la base de datos esta vacia.
// *
// * Agregado especificamente para sge-showcase: el proyecto original no
// * trae seeder porque los datos iniciales se gestionan manualmente en el
// * entorno privado. Sin esto, una base de datos nueva no tendria forma
// * de iniciar sesion.
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataSeeder implements CommandLineRunner {
//
//    private final PermissionRepository permissionRepository;
//    private final RoleRepository       roleRepository;
//    private final UserRepository       userRepository;
//    private final PasswordEncoder      passwordEncoder;
//
//    private static final List<String[]> PERMISSIONS = List.of(
//            new String[]{"USER_CREATE", "Crear usuarios", "USERS"},
//            new String[]{"USER_READ", "Ver usuarios", "USERS"},
//            new String[]{"USER_UPDATE", "Editar usuarios", "USERS"},
//            new String[]{"USER_DELETE", "Eliminar usuarios", "USERS"},
//            new String[]{"ROLE_CREATE", "Crear roles", "ROLES"},
//            new String[]{"ROLE_READ", "Ver roles", "ROLES"},
//            new String[]{"ROLE_UPDATE", "Editar roles", "ROLES"},
//            new String[]{"ROLE_DELETE", "Eliminar roles", "ROLES"},
//            new String[]{"EMPLOYEE_CREATE", "Crear empleados", "EMPLOYEES"},
//            new String[]{"EMPLOYEE_READ", "Ver empleados", "EMPLOYEES"},
//            new String[]{"EMPLOYEE_UPDATE", "Editar empleados", "EMPLOYEES"},
//            new String[]{"EMPLOYEE_DELETE", "Eliminar empleados", "EMPLOYEES"},
//            new String[]{"DEPARTMENT_CREATE", "Crear departamentos", "DEPARTMENTS"},
//            new String[]{"DEPARTMENT_READ", "Ver departamentos", "DEPARTMENTS"},
//            new String[]{"DEPARTMENT_UPDATE", "Editar departamentos", "DEPARTMENTS"},
//            new String[]{"DEPARTMENT_DELETE", "Eliminar departamentos", "DEPARTMENTS"},
//            new String[]{"REPORT_VIEW", "Ver reportes y analiticas", "REPORTS"}
//    );
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (permissionRepository.count() > 0) {
//            log.info("DataSeeder: ya existen datos, no se siembra nada.");
//            return;
//        }
//
//        log.info("DataSeeder: base de datos vacia, sembrando datos iniciales...");
//
//        Map<String, Permission> savedPermissions = new HashMap<>();
//        for (String[] p : PERMISSIONS) {
//            Permission permission = Permission.builder()
//                    .code(p[0])
//                    .name(p[1])
//                    .module(p[2])
//                    .description(p[1])
//                    .createdAt(LocalDateTime.now())
//                    .build();
//            savedPermissions.put(p[0], permissionRepository.save(permission));
//        }
//
//        // Rol ADMIN: acceso completo, NO es el rol por defecto
//        Role adminRole = Role.builder()
//                .name("ADMIN")
//                .description("Rol con acceso completo al sistema (solo para el showcase)")
//                .isDefault(false)
//                .permissions(new HashSet<>(savedPermissions.values()))
//                .build();
//        adminRole = roleRepository.save(adminRole);
//
//        // Rol USER: lectura basica, ES el rol por defecto para registros nuevos
//        Set<Permission> userPermissions = new HashSet<>();
//        userPermissions.add(savedPermissions.get("EMPLOYEE_READ"));
//        userPermissions.add(savedPermissions.get("DEPARTMENT_READ"));
//
//        Role userRole = Role.builder()
//                .name("USER")
//                .description("Rol basico de solo lectura (rol por defecto para nuevos registros)")
//                .isDefault(true)
//                .permissions(userPermissions)
//                .build();
//        roleRepository.save(userRole);
//
//        User admin = User.builder()
//                .username("admin")
//                .email("admin@sge-showcase.dev")
//                .firstName("Admin")
//                .lastName("Showcase")
//                .password(passwordEncoder.encode("Showcase*2026"))
//                .roles(Set.of(adminRole))
//                .active(true)
//                .build();
//        userRepository.save(admin);
//
//        log.info("DataSeeder: usuario admin creado (username=admin). Rol USER marcado como default para registros nuevos.");
//    }
//}


package com.yerandis.sge.config;

import com.yerandis.sge.entity.admin.Permission;
import com.yerandis.sge.entity.admin.Role;
import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.repository.admin.PermissionRepository;
import com.yerandis.sge.repository.admin.RoleRepository;
import com.yerandis.sge.repository.admin.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DataSeeder: crea los datos iniciales minimos (permisos, roles ADMIN y
 * USER, usuario admin) si la base de datos esta vacia.
 *
 * Agregado especificamente para sge-showcase: el proyecto original no
 * trae seeder porque los datos iniciales se gestionan manualmente en el
 * entorno privado. Sin esto, una base de datos nueva no tendria forma
 * de iniciar sesion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    private static final List<String[]> PERMISSIONS = List.of(
            new String[]{"USER_CREATE", "Crear usuarios", "USERS"},
            new String[]{"USER_READ", "Ver usuarios", "USERS"},
            new String[]{"USER_UPDATE", "Editar usuarios", "USERS"},
            new String[]{"USER_DELETE", "Eliminar usuarios", "USERS"},
            new String[]{"ROLE_CREATE", "Crear roles", "ROLES"},
            new String[]{"ROLE_READ", "Ver roles", "ROLES"},
            new String[]{"ROLE_UPDATE", "Editar roles", "ROLES"},
            new String[]{"ROLE_DELETE", "Eliminar roles", "ROLES"},
            new String[]{"EMPLOYEE_CREATE", "Crear empleados", "EMPLOYEES"},
            new String[]{"EMPLOYEE_READ", "Ver empleados", "EMPLOYEES"},
            new String[]{"EMPLOYEE_UPDATE", "Editar empleados", "EMPLOYEES"},
            new String[]{"EMPLOYEE_DELETE", "Eliminar empleados", "EMPLOYEES"},
            new String[]{"DEPARTMENT_CREATE", "Crear departamentos", "DEPARTMENTS"},
            new String[]{"DEPARTMENT_READ", "Ver departamentos", "DEPARTMENTS"},
            new String[]{"DEPARTMENT_UPDATE", "Editar departamentos", "DEPARTMENTS"},
            new String[]{"DEPARTMENT_DELETE", "Eliminar departamentos", "DEPARTMENTS"},
            new String[]{"REPORT_VIEW", "Ver reportes y analiticas", "REPORTS"}
    );

    @Override
    @Transactional
    public void run(String... args) {

        if (permissionRepository.count() > 0) {
            log.info("DataSeeder: ya existen datos, no se siembra nada.");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "La propiedad app.seed.admin-password es obligatoria para inicializar la base de datos."
            );
        }

        log.info("DataSeeder: base de datos vacia, sembrando datos iniciales...");

        Map<String, Permission> savedPermissions = new HashMap<>();

        for (String[] p : PERMISSIONS) {
            Permission permission = Permission.builder()
                    .code(p[0])
                    .name(p[1])
                    .module(p[2])
                    .description(p[1])
                    .createdAt(LocalDateTime.now())
                    .build();

            savedPermissions.put(p[0], permissionRepository.save(permission));
        }

        // Rol ADMIN: acceso completo, NO es el rol por defecto
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Rol con acceso completo al sistema (solo para el showcase)")
                .isDefault(false)
                .permissions(new HashSet<>(savedPermissions.values()))
                .build();

        adminRole = roleRepository.save(adminRole);

        // Rol USER: lectura basica, ES el rol por defecto para registros nuevos
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(savedPermissions.get("EMPLOYEE_READ"));
        userPermissions.add(savedPermissions.get("DEPARTMENT_READ"));

        Role userRole = Role.builder()
                .name("USER")
                .description("Rol basico de solo lectura (rol por defecto para nuevos registros)")
                .isDefault(true)
                .permissions(userPermissions)
                .build();

        roleRepository.save(userRole);

        User admin = User.builder()
                .username("admin")
                .email("admin@sge-showcase.dev")
                .firstName("Admin")
                .lastName("Showcase")
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of(adminRole))
                .active(true)
                .build();

        userRepository.save(admin);

        log.info("DataSeeder: usuario admin creado (username=admin). Rol USER marcado como default para registros nuevos.");
    }
}
