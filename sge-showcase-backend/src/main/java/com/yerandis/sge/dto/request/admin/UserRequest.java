package com.yerandis.sge.dto.request.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    /** Grupo de validación para POST /users (creación) */
    public interface OnCreate {}

    /** Grupo de validación para PUT /users/{id} (edición) */
    public interface OnUpdate {}

    @NotBlank(message = "El nombre de usuario es obligatorio", groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres",
            groups = {OnCreate.class, OnUpdate.class})
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos",
            groups = {OnCreate.class, OnUpdate.class}
    )
    private String username;

    /**
     * Al crear: obligatoria, mínimo 6 caracteres (@NotBlank + @Size, grupo OnCreate).
     * Al editar: opcional — si viene vacía o null, NO se cambia la contraseña.
     *            Si viene con contenido, igual debe cumplir el mínimo de 6
     *            caracteres (@AssertTrue de abajo, grupo OnUpdate).
     */
    @NotBlank(message = "La contraseña es obligatoria al crear un usuario", groups = OnCreate.class)
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres", groups = OnCreate.class)
    private String password;

    @AssertTrue(message = "La contraseña debe tener al menos 8 caracteres", groups = OnUpdate.class)
    public boolean isPasswordValidForUpdate() {
        return password == null || password.isBlank() || password.length() >= 8;
    }

    @NotBlank(message = "El email es requerido", groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Formato de email incorrecto", groups = {OnCreate.class, OnUpdate.class})
    private String email;

    private String firstName;
    private String lastName;
    private String avatarUrl = "";
    private boolean active = true;

    private List<UUID> roleIds = List.of();

    private UUID employeeId;
}