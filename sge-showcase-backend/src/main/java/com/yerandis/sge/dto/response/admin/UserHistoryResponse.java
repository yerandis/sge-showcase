package com.yerandis.sge.dto.response.admin;

import com.yerandis.sge.dto.response.system.Changes;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserHistoryResponse {

    private UUID            id;
    private String          action;
    private String          username;
    private List<Changes>   changes;
    private LocalDateTime   createdAt;
    // Resultado y módulo de la operación auditada.
    private String status;
    private String module;
}
