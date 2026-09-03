package com.medsafe.dto;

import com.medsafe.model.TipoPerfil;
import com.medsafe.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String telefone,
        Integer idade,
        TipoPerfil tipoPerfil,
        LocalDateTime criadoEm
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTelefone(),
                usuario.getIdade(),
                usuario.getTipoPerfil(),
                usuario.getCriadoEm()
        );
    }
}
