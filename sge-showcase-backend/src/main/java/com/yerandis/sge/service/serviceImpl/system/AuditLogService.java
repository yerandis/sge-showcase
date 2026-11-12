package com.yerandis.sge.service.serviceImpl.system;

import com.yerandis.sge.entity.admin.AuditLog;
import com.yerandis.sge.repository.system.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    private String getCurrentUsername() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        // Algunos procesos pueden ejecutarse sin una autenticación asociada.
        // Evitamos un NullPointerException al registrar la auditoría.
        if (auth == null || !auth.isAuthenticated()) {
            return "system";
        }

        return auth.getName();
    }


    public void create(String entityType, UUID entityId, String action,
                       String oldValue, String newValue, String module, String status) {

        String username     = getCurrentUsername();
        String ipAddress    = "";

        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setUsername(username);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setIpAddress(ipAddress);
        auditLog.setModule(module);
        auditLog.setStatus(status);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }
}
