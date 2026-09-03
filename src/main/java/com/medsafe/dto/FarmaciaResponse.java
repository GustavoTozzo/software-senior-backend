package com.medsafe.dto;

import com.medsafe.model.Farmacia;

public record FarmaciaResponse(
        Long id,
        String nome,
        String telefone,
        String whatsappLink
) {
    public static FarmaciaResponse from(Farmacia farmacia) {
        return new FarmaciaResponse(
                farmacia.getId(),
                farmacia.getNome(),
                farmacia.getTelefone(),
                farmacia.getWhatsappLink()
        );
    }
}
