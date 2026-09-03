package com.medsafe.dto;

import com.medsafe.model.Medicamento;

public record MedicamentoResponse(
        Long id,
        Long usuarioId,
        String nomeComercial,
        String codigoBarras,
        Integer quantidadeAtual,
        String doseFrequencia,
        String horariosProgramados
) {
    public static MedicamentoResponse from(Medicamento medicamento) {
        return new MedicamentoResponse(
                medicamento.getId(),
                medicamento.getUsuario().getId(),
                medicamento.getNomeComercial(),
                medicamento.getCodigoBarras(),
                medicamento.getQuantidadeAtual(),
                medicamento.getDoseFrequencia(),
                medicamento.getHorariosProgramados()
        );
    }
}
