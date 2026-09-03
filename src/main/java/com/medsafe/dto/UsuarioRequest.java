package com.medsafe.dto;

import com.medsafe.model.TipoPerfil;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 150, message = "nome deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 20, message = "telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Min(value = 0, message = "idade não pode ser negativa")
        Integer idade,

        TipoPerfil tipoPerfil
) {
}
