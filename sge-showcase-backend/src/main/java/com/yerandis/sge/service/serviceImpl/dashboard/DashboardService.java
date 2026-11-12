package com.yerandis.sge.service.serviceImpl.dashboard;

import com.yerandis.sge.dto.enums.EmployeeStatus;
import com.yerandis.sge.dto.response.dashboard.DashboardActivityDto;
import com.yerandis.sge.dto.response.dashboard.DashboardOverviewResponse;
import com.yerandis.sge.entity.admin.AuditLog;
import com.yerandis.sge.repository.system.AuditLogRepository;
import com.yerandis.sge.repository.system.DepartmentRepository;
import com.yerandis.sge.repository.system.EmployeeRepository;
import com.yerandis.sge.service.serviceInterface.dashboard.DashboardAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements DashboardAppService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Usuario no autenticado"
            );
        }

        // Usamos las autoridades actuales de Spring Security.
        Set<String> permissions = auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());

        Long totalEmployees = null;
        Long activeEmployees = null;
        Long inactiveEmployees = null;
        Long totalDepartments = null;
        Long hiresLast30Days = null;

        List<DashboardActivityDto> recentActivity = null;
        List<String> visibleSections = new ArrayList<>();

        // Las métricas de empleados solo se calculan si el usuario
        // tiene el permiso existente EMPLOYEE_READ.
        if (permissions.contains("EMPLOYEE_READ")) {
            totalEmployees = employeeRepository.count();
            activeEmployees =
                    employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
            inactiveEmployees =
                    employeeRepository.countByStatus(EmployeeStatus.INACTIVE);

            visibleSections.add("employees");
        }

        // Los datos de departamentos se protegen por separado.
        if (permissions.contains("DEPARTMENT_READ")) {
            totalDepartments = departmentRepository.count();
            visibleSections.add("departments");
        }

        // Las métricas analíticas solo se muestran a usuarios con REPORT_VIEW.
        if (permissions.contains("REPORT_VIEW")) {
            hiresLast30Days = employeeRepository.countByHireDateAfter(
                    LocalDate.now().minusDays(30)
            );

            // Solo exponemos campos seguros del log.
            recentActivity = auditLogRepository
                    .findAllByOrderByCreatedAtDesc(
                            PageRequest.of(
                                    0,
                                    8,
                                    Sort.by(Sort.Direction.DESC, "createdAt")
                            )
                    )
                    .getContent()
                    .stream()
                    .map(this::toActivityDto)
                    .toList();

            visibleSections.add("analytics");
            visibleSections.add("recentActivity");
        }

        return new DashboardOverviewResponse(
                totalEmployees,
                activeEmployees,
                inactiveEmployees,
                totalDepartments,
                hiresLast30Days,
                recentActivity,
                Set.copyOf(visibleSections)
        );
    }

    private DashboardActivityDto toActivityDto(AuditLog log) {
        // No incluir log.getOldValue() ni log.getNewValue().
        return new DashboardActivityDto(
                log.getUsername(),
                log.getAction(),
                log.getEntityType(),
                log.getStatus(),
                log.getCreatedAt()
        );
    }
}