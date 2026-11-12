package com.yerandis.sge.service.serviceImpl.system;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.yerandis.sge.dto.request.system.DepartmentRequest;
import com.yerandis.sge.dto.response.admin.PageResponse;
import com.yerandis.sge.dto.response.system.Changes;
import com.yerandis.sge.dto.response.system.DepartmentHistoryResponse;
import com.yerandis.sge.dto.response.system.DepartmentResponse;
import com.yerandis.sge.entity.admin.AuditLog;
import com.yerandis.sge.entity.system.Department;
import com.yerandis.sge.exception.BusinessException;
import com.yerandis.sge.exception.ResourceNotFoundException;
import com.yerandis.sge.mapper.system.DepartmentMapper;
import com.yerandis.sge.repository.system.AuditLogRepository;
import com.yerandis.sge.repository.system.DepartmentRepository;
import com.yerandis.sge.service.serviceInterface.system.DepartmentAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService implements DepartmentAppService {

    private static final String ENTITY_TYPE = "DEPARTMENT";

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper     departmentMapper;
    private final AuditLogService      auditLogService;
    private final AuditLogRepository   auditLogRepository;

    /**
     * ObjectMapper es thread-safe y reutilizable. Un solo bean es suficiente.
     * Serializa los objetos a JSON para guardarlos en old_value / new_value.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── Helpers de serialización ─────────────────────────────────

    /**
     * Convierte un Department a JSON compacto para guardarlo en audit_logs.
     * Solo incluye los campos relevantes para auditoría (no IDs internos).
     */
    private String toJson(Department d) {
        if (d == null) return null;
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name",        d.getName());
            map.put("description", d.getDescription());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("No se pudo serializar Department a JSON: {}", e.getMessage());
            return null;
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> findAll(String search, Pageable pageable) {
        Page<Department> page = departmentRepository.searchDepartment(search, pageable);
        return new PageResponse<>(page.map(departmentMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(UUID id) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id));
        return departmentMapper.toResponse(d);
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName().trim().toLowerCase())) {
            throw new BusinessException("", "Ya existe un departamento con el nombre: " + request.getName());
        }

        Department department  = departmentMapper.toEntity(request);
        Department saved       = departmentRepository.save(department);

        // ✅ CORRECCIÓN: oldValue = null (no existía), newValue = JSON del objeto creado
        auditLogService.create(
                ENTITY_TYPE, saved.getId(),
                "CREATE",
                null,           // ← oldValue: no había estado previo
                toJson(saved),  // ← newValue: JSON real {"name":"Tecnología","description":"..."}
                "DEPARTMENTS",
                "SUCCESS"
        );


        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id));

        // ✅ CORRECCIÓN: capturar el estado ANTES del cambio para comparar después
        String oldJson = toJson(department);

        departmentMapper.updateEntityFromRequest(request, department);
        Department updated = departmentRepository.save(department);

        // ✅ CORRECCIÓN: oldValue = estado anterior, newValue = estado nuevo (ambos JSON reales)
        auditLogService.create(
                ENTITY_TYPE, updated.getId(),
                "UPDATE",
                oldJson,          // ← estado antes del cambio
                toJson(updated),  // ← estado después del cambio
                "DEPARTMENTS",
                "SUCCESS"
        );


        return departmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Department toDelete = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id));

        String departmentName = toDelete.getName();
        // ✅ CORRECCIÓN: capturar el estado ANTES de eliminar
        String oldJson = toJson(toDelete);

        departmentRepository.deleteById(id);

        // ✅ CORRECCIÓN: oldValue = JSON del objeto eliminado, newValue = null
        auditLogService.create(
                ENTITY_TYPE, id,
                "DELETE",
                oldJson,  // ← estado que tenía antes de eliminarse
                null,     // ← newValue: ya no existe
                "DEPARTMENTS",
                "SUCCESS"
        );

    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalDepartments", departmentRepository.count());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentHistoryResponse> getHistory(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Departamento", id);
        }

        List<AuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(ENTITY_TYPE, id);

        return logs.stream()
                .map(log -> new DepartmentHistoryResponse(
                        log.getId(),
                        log.getAction(),
                        log.getUsername(),
                        parseChanges(log.getOldValue(), log.getNewValue()),
                        log.getCreatedAt()
                ))
                .toList();
    }

    /**
     * parseChanges: compara los JSON de before/after y devuelve los campos que cambiaron.
     *
     * ✅ Este método es correcto — el bug estaba en quién lo llamaba.
     *
     * REQUISITO: oldValue y newValue deben ser JSON válidos como:
     *   {"name":"Tecnología","description":"Área de software"}
     *
     * NOT válido (lo que había antes):
     *   "CREATED", "UPDATED", "DELETED"   ← strings literales, no JSON
     */
    private List<Changes> parseChanges(String oldJson, String newJson) {
        // CREATE: oldJson es null → no hay cambios que comparar, lista vacía
        // DELETE: newJson es null → ídem
        if (oldJson == null || newJson == null) return List.of();

        try {
            Map<String, Object> oldMap = objectMapper.readValue(oldJson, new TypeReference<>() {});
            Map<String, Object> newMap = objectMapper.readValue(newJson, new TypeReference<>() {});

            return oldMap.entrySet().stream()
                    .filter(entry -> !Objects.equals(entry.getValue(), newMap.get(entry.getKey())))
                    .map(entry -> new Changes(
                            entry.getKey(),
                            String.valueOf(entry.getValue()),
                            String.valueOf(newMap.getOrDefault(entry.getKey(), ""))
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("Error parseando audit_log JSON — oldJson='{}', newJson='{}': {}",
                    oldJson, newJson, e.getMessage());
            return List.of();
        }
    }
}