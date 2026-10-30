package com.medsafe.service;

import com.medsafe.dto.HistoricoIngestaoRequest;
import com.medsafe.dto.HistoricoIngestaoResponse;
import com.medsafe.exception.BusinessException;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.HistoricoIngestao;
import com.medsafe.model.Medicamento;
import com.medsafe.model.StatusIngestao;
import com.medsafe.repository.HistoricoIngestaoRepository;
import com.medsafe.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoricoIngestaoService {

    private static final LocalDateTime INICIO_DOS_TEMPOS = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime FIM_DOS_TEMPOS = LocalDateTime.of(9999, 12, 31, 23, 59);

    private final HistoricoIngestaoRepository historicoRepository;
    private final MedicamentoService medicamentoService;
    private final UsuarioLogado usuarioLogado;

    public HistoricoIngestaoResponse criar(HistoricoIngestaoRequest request) {
        Medicamento medicamento = medicamentoService.buscarEntidadeDoUsuario(request.medicamentoId());
        if (historicoRepository.existsByMedicamentoIdAndDataHoraProgramada(medicamento.getId(), request.dataHoraProgramada())) {
            throw new BusinessException("Já existe uma dose programada para esse medicamento nesse horário");
        }

        HistoricoIngestao historico = new HistoricoIngestao();
        historico.setMedicamento(medicamento);
        historico.setDataHoraProgramada(request.dataHoraProgramada());
        historico.setStatus(StatusIngestao.PENDENTE);
        return HistoricoIngestaoResponse.from(historicoRepository.save(historico));
    }

    /**
     * Historico do usuario logado, do mais recente para o mais antigo. Com medicamentoId filtra por
     * medicamento; de/ate (inclusivos) limitam o periodo pela data programada.
     */
    @Transactional(readOnly = true)
    public List<HistoricoIngestaoResponse> listar(Long medicamentoId, Long usuarioId, LocalDate de, LocalDate ate) {
        if (usuarioId != null) {
            usuarioLogado.exigirMesmoUsuario(usuarioId);
        }
        LocalDateTime inicio = de != null ? de.atStartOfDay() : INICIO_DOS_TEMPOS;
        LocalDateTime fim = ate != null ? ate.atTime(LocalTime.MAX) : FIM_DOS_TEMPOS;

        List<HistoricoIngestao> registros;
        if (medicamentoId != null) {
            Medicamento medicamento = medicamentoService.buscarEntidadeDoUsuario(medicamentoId);
            registros = historicoRepository.findByMedicamentoIdOrderByDataHoraProgramadaDesc(medicamento.getId()).stream()
                    .filter(h -> !h.getDataHoraProgramada().isBefore(inicio) && !h.getDataHoraProgramada().isAfter(fim))
                    .toList();
        } else {
            registros = historicoRepository.findByMedicamentoUsuarioIdAndDataHoraProgramadaBetweenOrderByDataHoraProgramadaDesc(
                    usuarioLogado.id(), inicio, fim);
        }
        return registros.stream().map(HistoricoIngestaoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HistoricoIngestaoResponse buscarPorId(Long id) {
        return HistoricoIngestaoResponse.from(buscarEntidadeDoUsuario(id));
    }

    /**
     * Ao marcar TOMADO pela primeira vez, da baixa de 1 unidade no estoque do medicamento
     * (o alerta de estoque baixo depende dessa baixa acontecer uma unica vez por dose).
     */
    public HistoricoIngestaoResponse atualizarStatus(Long id, StatusIngestao novoStatus) {
        HistoricoIngestao historico = buscarEntidadeDoUsuario(id);
        boolean passandoParaTomado = novoStatus == StatusIngestao.TOMADO
                && historico.getStatus() != StatusIngestao.TOMADO;

        historico.setStatus(novoStatus);
        historico.setDataHoraRealizada(novoStatus == StatusIngestao.PENDENTE ? null : LocalDateTime.now());

        if (passandoParaTomado) {
            medicamentoService.registrarConsumo(historico.getMedicamento());
        }

        return HistoricoIngestaoResponse.from(historicoRepository.save(historico));
    }

    public void excluir(Long id) {
        historicoRepository.delete(buscarEntidadeDoUsuario(id));
    }

    private HistoricoIngestao buscarEntidadeDoUsuario(Long id) {
        HistoricoIngestao historico = historicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HistoricoIngestao", id));
        usuarioLogado.exigirMesmoUsuario(historico.getMedicamento().getUsuario().getId());
        return historico;
    }
}
