package com.medsafe.security;

import com.medsafe.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private static final String EMISSOR = "medsafe-senior";

    private final JwtEncoder encoder;
    private final Duration validade;

    public TokenService(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.validade = propriedades.expiration();
    }

    /** O subject do token e o id do usuario; e ele que identifica quem esta logado nas requisicoes. */
    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .subject(usuario.getId().toString())
                .claim("perfil", usuario.getTipoPerfil().name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    public long validadeEmSegundos() {
        return validade.toSeconds();
    }
}
