package com.medsafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FarmaciaRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 150, message = "nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "telefone é obrigatório")
        @Size(max = 20, message = "telefone deve ter no máximo 20 caracteres")
        String telefone,

        String whatsappLink
) {
}
