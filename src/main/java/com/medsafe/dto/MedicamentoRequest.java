package com.medsafe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MedicamentoRequest(
        @NotNull(message = "usuarioId é obrigatório")
        Long usuarioId,

        @NotBlank(message = "nomeComercial é obrigatório")
        @Size(max = 150, message = "nomeComercial deve ter no máximo 150 caracteres")
        String nomeComercial,

        @Size(max = 50, message = "codigoBarras deve ter no máximo 50 caracteres")
        String codigoBarras,

        @NotNull(message = "quantidadeAtual é obrigatório")
        @Min(value = 0, message = "quantidadeAtual não pode ser negativa")
        Integer quantidadeAtual,

        @NotBlank(message = "doseFrequencia é obrigatório")
        String doseFrequencia,

        @NotBlank(message = "horariosProgramados é obrigatório")
        @Pattern(
                regexp = "^([01]\\d|2[0-3]):[0-5]\\d(,([01]\\d|2[0-3]):[0-5]\\d)*$",
                message = "horariosProgramados deve ter horários no formato HH:mm separados por vírgula")
        String horariosProgramados
) {
}
