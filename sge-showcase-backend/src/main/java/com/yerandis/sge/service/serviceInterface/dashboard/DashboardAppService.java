package com.yerandis.sge.service.serviceInterface.dashboard;

import com.yerandis.sge.dto.response.dashboard.DashboardOverviewResponse;

public interface DashboardAppService {

    // Devuelve únicamente las métricas y secciones autorizadas.
    DashboardOverviewResponse getOverview();
}