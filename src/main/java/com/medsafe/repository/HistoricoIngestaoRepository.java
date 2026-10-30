package com.medsafe.repository;

import com.medsafe.model.HistoricoIngestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoIngestaoRepository extends JpaRepository<HistoricoIngestao, Long> {

    List<HistoricoIngestao> findByMedicamentoIdOrderByDataHoraProgramadaDesc(Long medicamentoId);

    List<HistoricoIngestao> findByMedicamentoUsuarioIdAndDataHoraProgramadaBetweenOrderByDataHoraProgramadaDesc(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByMedicamentoIdAndDataHoraProgramada(Long medicamentoId, LocalDateTime dataHoraProgramada);
}
