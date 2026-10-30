package com.medsafe.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "telefone é obrigatório")
        String telefone,

        @NotBlank(message = "senha é obrigatória")
        String senha
) {
}
