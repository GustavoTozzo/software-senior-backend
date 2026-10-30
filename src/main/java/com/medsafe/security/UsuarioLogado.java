package com.medsafe.security;

import com.medsafe.exception.AcessoNegadoException;
import com.medsafe.exception.CredenciaisInvalidasException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Acesso ao usuario autenticado da requisicao atual (o subject do JWT) e a regra de dono dos dados. */
@Component
public class UsuarioLogado {

    public Long id() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken jwt) {
            return Long.valueOf(jwt.getToken().getSubject());
        }
        throw new CredenciaisInvalidasException("Sessão expirada ou inválida. Entre novamente.");
    }

    /** Impede que um usuario leia ou altere dados de outro. */
    public void exigirMesmoUsuario(Long usuarioId) {
        if (!id().equals(usuarioId)) {
            throw new AcessoNegadoException();
        }
    }
}
