package com.medsafe.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    private Integer idade;

    @Column(name = "tipo_perfil", length = 50)
    private String tipoPerfil = "IDOSO";

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();
}