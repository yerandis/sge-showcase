package com.yerandis.sge.controller.dashboard;

import com.yerandis.sge.dto.response.admin.ApiResponse;
import com.yerandis.sge.dto.response.dashboard.DashboardOverviewResponse;
import com.yerandis.sge.service.serviceInterface.dashboard.DashboardAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAppService dashboardAppService;

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview() {
        // El servicio decide qué secciones devuelve según los permisos actuales.
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Resumen operativo obtenido",
                        dashboardAppService.getOverview()
                )
        );
    }
}