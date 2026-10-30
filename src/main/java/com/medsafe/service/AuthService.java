package com.medsafe.service;

import com.medsafe.dto.AuthResponse;
import com.medsafe.dto.LoginRequest;
import com.medsafe.dto.RegistroRequest;
import com.medsafe.dto.UsuarioResponse;
import com.medsafe.exception.BusinessException;
import com.medsafe.exception.CredenciaisInvalidasException;
import com.medsafe.model.TipoPerfil;
import com.medsafe.model.Usuario;
import com.medsafe.repository.UsuarioRepository;
import com.medsafe.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final String MENSAGEM_CREDENCIAIS = "Telefone ou senha incorretos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        // Comparado quando o telefone nao existe, para o tempo de resposta nao revelar quais telefones tem conta.
        this.hashFicticio = passwordEncoder.encode("senha-ficticia");
    }

    public AuthResponse registrar(RegistroRequest request) {
        String telefone = Telefone.normalizar(request.telefone());
        if (usuarioRepository.existsByTelefone(telefone)) {
            throw new BusinessException("Já existe uma conta com esse telefone");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setTelefone(telefone);
        usuario.setIdade(request.idade());
        usuario.setTipoPerfil(request.tipoPerfil() != null ? request.tipoPerfil() : TipoPerfil.IDOSO);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));

        return respostaCom(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse entrar(LoginRequest request) {
        String telefone = Telefone.normalizar(request.telefone());
        Usuario usuario = usuarioRepository.findByTelefone(telefone).orElse(null);

        String hash = usuario != null && usuario.getSenhaHash() != null ? usuario.getSenhaHash() : hashFicticio;
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);
        if (usuario == null || usuario.getSenhaHash() == null || !senhaConfere) {
            throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS);
        }
        return respostaCom(usuario);
    }

    private AuthResponse respostaCom(Usuario usuario) {
        return new AuthResponse(tokenService.gerar(usuario), tokenService.validadeEmSegundos(), UsuarioResponse.from(usuario));
    }
}
