package com.yerandis.sge.dto.response.dashboard;

import java.time.LocalDateTime;

/**
 * DTO reducido para mostrar actividad en el dashboard.
 *
 * No incluye oldValue ni newValue de AuditLog porque pueden contener
 * información sensible de la entidad modificada.
 */
public record DashboardActivityDto(
        String username,
        String action,
        String entityType,
        String status,
        LocalDateTime createdAt
) {}