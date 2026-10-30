package com.medsafe.dto;

public record AuthResponse(
        String token,
        long expiraEmSegundos,
        UsuarioResponse usuario
) {
}
