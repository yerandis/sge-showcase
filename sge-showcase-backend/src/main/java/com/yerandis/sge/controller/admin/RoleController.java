package com.yerandis.sge.controller.admin;

import com.yerandis.sge.dto.request.admin.RoleRequest;
import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.admin.PermissionResponse;
import com.yerandis.sge.dto.response.admin.RoleResponse;
import com.yerandis.sge.service.serviceInterface.admin.RoleAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleAppService roleAppService;

    // GET /api/v1/roles — lista de roles con sus permisos
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> findAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Roles obtenidos", roleAppService.findAllRoles())
        );
    }

    // GET /api/v1/roles/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleResponse>> findById(@PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success("Rol obtenido", roleAppService.findRoleById(id))
        );
    }

    // POST /api/v1/roles
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody RoleRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rol creado exitosamente", roleAppService.createRole(request)));
    }

    // PUT /api/v1/roles/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request) {

        log.debug("**** controller is default = {}", request.isDefault());
        log.debug("nombre usuario = {}", request.getName());
        return ResponseEntity.ok(
                ApiResponse.success("Rol actualizado exitosamente", roleAppService.updateRole(id, request))
        );
    }

    // DELETE /api/v1/roles/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

        roleAppService.deleteRole(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // GET /api/v1/roles/permissions — todos los permisos disponibles
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> findAllPermissions() {

        return ResponseEntity.ok(
                ApiResponse.success("Permisos obtenidos", roleAppService.findAllPermissions())
        );
    }
}
