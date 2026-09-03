package com.medsafe.service;

import com.medsafe.dto.UsuarioRequest;
import com.medsafe.dto.UsuarioResponse;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.TipoPerfil;
import com.medsafe.model.Usuario;
import com.medsafe.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse criar(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        aplicarRequest(usuario, request);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(buscarEntidade(id));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarEntidade(id);
        aplicarRequest(usuario, request);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    public void excluir(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuarioRepository.delete(usuario);
    }

    Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private void aplicarRequest(Usuario usuario, UsuarioRequest request) {
        usuario.setNome(request.nome());
        usuario.setTelefone(request.telefone());
        usuario.setIdade(request.idade());
        usuario.setTipoPerfil(request.tipoPerfil() != null ? request.tipoPerfil() : TipoPerfil.IDOSO);
    }
}
