package com.medsafe.service;

import com.medsafe.dto.MedicamentoRequest;
import com.medsafe.dto.MedicamentoResponse;
import com.medsafe.exception.BusinessException;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.Medicamento;
import com.medsafe.model.Usuario;
import com.medsafe.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicamentoService {

    /** Limite abaixo do qual o estoque é considerado baixo (usado nos alertas de reposição). */
    public static final int LIMITE_ESTOQUE_BAIXO = 5;

    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioService usuarioService;

    public MedicamentoResponse criar(MedicamentoRequest request) {
        Usuario usuario = usuarioService.buscarEntidade(request.usuarioId());
        validarCodigoBarrasUnico(request.codigoBarras(), null);

        Medicamento medicamento = new Medicamento();
        medicamento.setUsuario(usuario);
        aplicarRequest(medicamento, request);
        return MedicamentoResponse.from(medicamentoRepository.save(medicamento));
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listarTodos() {
        return medicamentoRepository.findAll().stream()
                .map(MedicamentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listarPorUsuario(Long usuarioId) {
        usuarioService.buscarEntidade(usuarioId);
        return medicamentoRepository.findByUsuarioId(usuarioId).stream()
                .map(MedicamentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse buscarPorId(Long id) {
        return MedicamentoResponse.from(buscarEntidade(id));
    }

    public MedicamentoResponse atualizar(Long id, MedicamentoRequest request) {
        Medicamento medicamento = buscarEntidade(id);
        Usuario usuario = usuarioService.buscarEntidade(request.usuarioId());
        validarCodigoBarrasUnico(request.codigoBarras(), id);

        medicamento.setUsuario(usuario);
        aplicarRequest(medicamento, request);
        return MedicamentoResponse.from(medicamentoRepository.save(medicamento));
    }

    public void excluir(Long id) {
        Medicamento medicamento = buscarEntidade(id);
        medicamentoRepository.delete(medicamento);
    }

    /** Reduz o estoque em 1 unidade quando uma dose é registrada como tomada. Nunca deixa o estoque negativo. */
    void registrarConsumo(Medicamento medicamento) {
        int restante = Math.max(0, medicamento.getQuantidadeAtual() - 1);
        medicamento.setQuantidadeAtual(restante);
        medicamentoRepository.save(medicamento);
    }

    Medicamento buscarEntidade(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
    }

    private void validarCodigoBarrasUnico(String codigoBarras, Long idAtual) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return;
        }
        medicamentoRepository.findByCodigoBarras(codigoBarras).ifPresent(existente -> {
            if (!existente.getId().equals(idAtual)) {
                throw new BusinessException("Já existe um medicamento cadastrado com o código de barras " + codigoBarras);
            }
        });
    }

    private void aplicarRequest(Medicamento medicamento, MedicamentoRequest request) {
        medicamento.setNomeComercial(request.nomeComercial());
        medicamento.setCodigoBarras(request.codigoBarras());
        medicamento.setQuantidadeAtual(request.quantidadeAtual());
        medicamento.setDoseFrequencia(request.doseFrequencia());
        medicamento.setHorariosProgramados(request.horariosProgramados());
    }
}
