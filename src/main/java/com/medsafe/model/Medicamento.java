package com.medsafe.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "medicamentos")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "nome_comercial", nullable = false, length = 150)
    private String nomeComercial;

    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;

    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual;

    @Column(name = "dose_frequencia", nullable = false, length = 100)
    private String doseFrequencia;

    @Column(name = "horarios_programados", nullable = false, columnDefinition = "TEXT")
    private String horariosProgramados;
}