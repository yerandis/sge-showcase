package com.yerandis.sge.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

/**
 * Respuesta del dashboard.
 *
 * Los valores nulos indican que esa métrica no está disponible
 * para los permisos del usuario actual.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardOverviewResponse(
        Long totalEmployees,
        Long activeEmployees,
        Long inactiveEmployees,
        Long totalDepartments,
        Long hiresLast30Days,
        List<DashboardActivityDto> recentActivity,
        Set<String> visibleSections
) {}