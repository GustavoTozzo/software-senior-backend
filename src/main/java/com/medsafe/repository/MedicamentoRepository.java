package com.medsafe.repository;

import com.medsafe.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByUsuarioId(Long usuarioId);

    Optional<Medicamento> findByCodigoBarras(String codigoBarras);
}