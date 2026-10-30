package com.medsafe.service;

import com.medsafe.dto.MedicamentoRequest;
import com.medsafe.dto.MedicamentoResponse;
import com.medsafe.exception.BusinessException;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.Medicamento;
import com.medsafe.model.Usuario;
import com.medsafe.repository.MedicamentoRepository;
import com.medsafe.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioService usuarioService;
    private final UsuarioLogado usuarioLogado;

    public MedicamentoResponse criar(MedicamentoRequest request) {
        usuarioLogado.exigirMesmoUsuario(request.usuarioId());
        Usuario usuario = usuarioService.buscarEntidade(request.usuarioId());
        validarCodigoBarrasUnico(usuario.getId(), request.codigoBarras(), null);

        Medicamento medicamento = new Medicamento();
        medicamento.setUsuario(usuario);
        aplicarRequest(medicamento, request);
        return MedicamentoResponse.from(medicamentoRepository.save(medicamento));
    }

    /** Medicamentos do usuario logado; se usuarioId for informado, precisa ser o proprio usuario. */
    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listar(Long usuarioId) {
        if (usuarioId != null) {
            usuarioLogado.exigirMesmoUsuario(usuarioId);
        }
        return medicamentoRepository.findByUsuarioId(usuarioLogado.id()).stream()
                .map(MedicamentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse buscarPorId(Long id) {
        return MedicamentoResponse.from(buscarEntidadeDoUsuario(id));
    }

    public MedicamentoResponse atualizar(Long id, MedicamentoRequest request) {
        usuarioLogado.exigirMesmoUsuario(request.usuarioId());
        Medicamento medicamento = buscarEntidadeDoUsuario(id);
        validarCodigoBarrasUnico(medicamento.getUsuario().getId(), request.codigoBarras(), id);

        aplicarRequest(medicamento, request);
        return MedicamentoResponse.from(medicamentoRepository.save(medicamento));
    }

    public void excluir(Long id) {
        medicamentoRepository.delete(buscarEntidadeDoUsuario(id));
    }

    /** Reduz o estoque em 1 unidade quando uma dose e registrada como tomada. Nunca deixa o estoque negativo. */
    void registrarConsumo(Medicamento medicamento) {
        int restante = Math.max(0, medicamento.getQuantidadeAtual() - 1);
        medicamento.setQuantidadeAtual(restante);
        medicamentoRepository.save(medicamento);
    }


    /** Busca o medicamento garantindo que ele pertence ao usuario logado. */
    Medicamento buscarEntidadeDoUsuario(Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
        usuarioLogado.exigirMesmoUsuario(medicamento.getUsuario().getId());
        return medicamento;
    }

    private void validarCodigoBarrasUnico(Long usuarioId, String codigoBarras, Long idAtual) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return;
        }
        medicamentoRepository.findByUsuarioIdAndCodigoBarras(usuarioId, codigoBarras).ifPresent(existente -> {
            if (!existente.getId().equals(idAtual)) {
                throw new BusinessException("Você já cadastrou um medicamento com o código de barras " + codigoBarras);
            }
        });
    }

    private void aplicarRequest(Medicamento medicamento, MedicamentoRequest request) {
        medicamento.setNomeComercial(request.nomeComercial().trim());
        medicamento.setCodigoBarras(request.codigoBarras() == null || request.codigoBarras().isBlank()
                ? null : request.codigoBarras().trim());
        medicamento.setQuantidadeAtual(request.quantidadeAtual());
        medicamento.setDoseFrequencia(request.doseFrequencia());
        medicamento.setHorariosProgramados(request.horariosProgramados());
    }
}
