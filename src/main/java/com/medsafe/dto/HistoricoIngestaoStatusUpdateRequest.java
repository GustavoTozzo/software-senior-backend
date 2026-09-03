package com.medsafe.dto;

import com.medsafe.model.StatusIngestao;
import jakarta.validation.constraints.NotNull;

public record HistoricoIngestaoStatusUpdateRequest(
        @NotNull(message = "status é obrigatório")
        StatusIngestao status
) {
}
