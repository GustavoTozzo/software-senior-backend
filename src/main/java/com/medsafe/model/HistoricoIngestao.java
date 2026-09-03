package com.medsafe.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historico_ingestao")
public class HistoricoIngestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "data_hora_programada", nullable = false)
    private LocalDateTime dataHoraProgramada;

    @Column(name = "data_hora_realizada")
    private LocalDateTime dataHoraRealizada;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusIngestao status = StatusIngestao.PENDENTE;
}