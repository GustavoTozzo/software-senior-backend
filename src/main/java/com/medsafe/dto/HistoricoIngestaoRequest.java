package com.medsafe.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record HistoricoIngestaoRequest(
        @NotNull(message = "medicamentoId é obrigatório")
        Long medicamentoId,

        @NotNull(message = "dataHoraProgramada é obrigatória")
        LocalDateTime dataHoraProgramada
) {
}
