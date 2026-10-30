package com.medsafe.service;

import com.medsafe.dto.UsuarioRequest;
import com.medsafe.dto.UsuarioResponse;
import com.medsafe.exception.BusinessException;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.TipoPerfil;
import com.medsafe.model.Usuario;
import com.medsafe.repository.UsuarioRepository;
import com.medsafe.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Perfil do usuario logado. O cadastro de contas e feito pelo AuthService. */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public UsuarioResponse perfilLogado() {
        return UsuarioResponse.from(buscarEntidade(usuarioLogado.id()));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        usuarioLogado.exigirMesmoUsuario(id);
        return UsuarioResponse.from(buscarEntidade(id));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        usuarioLogado.exigirMesmoUsuario(id);
        Usuario usuario = buscarEntidade(id);

        String telefone = Telefone.normalizar(request.telefone());
        usuarioRepository.findByTelefone(telefone).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new BusinessException("Já existe uma conta com esse telefone");
            }
        });

        usuario.setNome(request.nome().trim());
        usuario.setTelefone(telefone);
        usuario.setIdade(request.idade());
        usuario.setTipoPerfil(request.tipoPerfil() != null ? request.tipoPerfil() : TipoPerfil.IDOSO);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public void excluir(Long id) {
        usuarioLogado.exigirMesmoUsuario(id);
        usuarioRepository.delete(buscarEntidade(id));
    }

    Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
