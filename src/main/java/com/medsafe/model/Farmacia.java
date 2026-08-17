package com.medsafe.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "farmacias")
public class Farmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "whatsapp_link", length = 255)
    private String whatsappLink;
}