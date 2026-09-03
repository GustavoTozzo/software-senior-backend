package com.medsafe.service;

import com.medsafe.dto.HistoricoIngestaoRequest;
import com.medsafe.dto.HistoricoIngestaoResponse;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.HistoricoIngestao;
import com.medsafe.model.Medicamento;
import com.medsafe.model.StatusIngestao;
import com.medsafe.repository.HistoricoIngestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoricoIngestaoService {

    private final HistoricoIngestaoRepository historicoRepository;
    private final MedicamentoService medicamentoService;

    public HistoricoIngestaoResponse criar(HistoricoIngestaoRequest request) {
        Medicamento medicamento = medicamentoService.buscarEntidade(request.medicamentoId());

        HistoricoIngestao historico = new HistoricoIngestao();
        historico.setMedicamento(medicamento);
        historico.setDataHoraProgramada(request.dataHoraProgramada());
        historico.setStatus(StatusIngestao.PENDENTE);
        return HistoricoIngestaoResponse.from(historicoRepository.save(historico));
    }

    @Transactional(readOnly = true)
    public List<HistoricoIngestaoResponse> listarTodos() {
        return historicoRepository.findAll().stream()
                .map(HistoricoIngestaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoIngestaoResponse> listarPorMedicamento(Long medicamentoId) {
        medicamentoService.buscarEntidade(medicamentoId);
        return historicoRepository.findByMedicamentoId(medicamentoId).stream()
                .map(HistoricoIngestaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoIngestaoResponse> listarPorUsuario(Long usuarioId) {
        return historicoRepository.findByMedicamentoUsuarioId(usuarioId).stream()
                .map(HistoricoIngestaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoricoIngestaoResponse buscarPorId(Long id) {
        return HistoricoIngestaoResponse.from(buscarEntidade(id));
    }

    /**
     * Ao marcar TOMADO pela primeira vez, dá baixa de 1 unidade no estoque do medicamento
     * (controle de estoque preditivo depende dessa baixa acontecer uma única vez por dose).
     */
    public HistoricoIngestaoResponse atualizarStatus(Long id, StatusIngestao novoStatus) {
        HistoricoIngestao historico = buscarEntidade(id);
        boolean passandoParaTomado = novoStatus == StatusIngestao.TOMADO
                && historico.getStatus() != StatusIngestao.TOMADO;

        historico.setStatus(novoStatus);
        historico.setDataHoraRealizada(
                novoStatus == StatusIngestao.PENDENTE ? null : java.time.LocalDateTime.now());

        if (passandoParaTomado) {
            medicamentoService.registrarConsumo(historico.getMedicamento());
        }

        return HistoricoIngestaoResponse.from(historicoRepository.save(historico));
    }

    public void excluir(Long id) {
        historicoRepository.delete(buscarEntidade(id));
    }

    private HistoricoIngestao buscarEntidade(Long id) {
        return historicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HistoricoIngestao", id));
    }
}
