package com.medsafe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

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

    @Column(length = 20, unique = true)
    private String telefone;

    private Integer idade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_perfil", length = 50)
    private TipoPerfil tipoPerfil = TipoPerfil.IDOSO;

    @ToString.Exclude
    @Column(name = "senha_hash", length = 100)
    private String senhaHash;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();
}
