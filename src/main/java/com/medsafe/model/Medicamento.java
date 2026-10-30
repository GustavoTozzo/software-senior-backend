package com.medsafe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Entity
@Table(name = "medicamentos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_medicamentos_usuario_codigo_barras", columnNames = {"usuario_id", "codigo_barras"})
})
public class Medicamento {

    /** Quantidade a partir da qual o estoque e considerado baixo (alerta de reposicao). */
    public static final int LIMITE_ESTOQUE_BAIXO = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "nome_comercial", nullable = false, length = 150)
    private String nomeComercial;

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Min(0)
    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual;

    @Column(name = "dose_frequencia", nullable = false, length = 100)
    private String doseFrequencia;

    @Column(name = "horarios_programados", nullable = false, columnDefinition = "TEXT")
    private String horariosProgramados;

    public boolean estoqueBaixo() {
        return quantidadeAtual != null && quantidadeAtual <= LIMITE_ESTOQUE_BAIXO;
    }
}
