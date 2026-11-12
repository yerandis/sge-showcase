package com.yerandis.sge.repository.system;

import com.yerandis.sge.entity.admin.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Historial de cambios de una entidad específica.
     * Se ordena del más reciente al más antiguo.
     *
     * Spring Data genera:
     * SELECT * FROM audit_logs
     * WHERE entity_type = ? AND entity_id = ?
     * ORDER BY created_at DESC
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            UUID entityId
    );

    /**
     * Todos los logs de un usuario.
     */
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    // Consulta paginada para no cargar todo el historial en el dashboard.
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
  }
