package com.medsafe.dto;

import com.medsafe.model.HistoricoIngestao;
import com.medsafe.model.StatusIngestao;

import java.time.LocalDateTime;

public record HistoricoIngestaoResponse(
        Long id,
        Long medicamentoId,
        String medicamentoNome,
        LocalDateTime dataHoraProgramada,
        LocalDateTime dataHoraRealizada,
        StatusIngestao status
) {
    public static HistoricoIngestaoResponse from(HistoricoIngestao historico) {
        return new HistoricoIngestaoResponse(
                historico.getId(),
                historico.getMedicamento().getId(),
                historico.getMedicamento().getNomeComercial(),
                historico.getDataHoraProgramada(),
                historico.getDataHoraRealizada(),
                historico.getStatus()
        );
    }
}
