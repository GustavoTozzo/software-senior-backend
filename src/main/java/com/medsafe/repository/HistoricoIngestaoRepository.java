package com.medsafe.repository;
import com.medsafe.model.HistoricoIngestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoIngestaoRepository extends JpaRepository<HistoricoIngestao, Long> {
}