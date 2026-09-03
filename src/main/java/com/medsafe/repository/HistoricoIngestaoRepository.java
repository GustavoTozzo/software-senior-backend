package com.medsafe.repository;

import com.medsafe.model.HistoricoIngestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoIngestaoRepository extends JpaRepository<HistoricoIngestao, Long> {

    List<HistoricoIngestao> findByMedicamentoId(Long medicamentoId);

    List<HistoricoIngestao> findByMedicamentoUsuarioId(Long usuarioId);
}