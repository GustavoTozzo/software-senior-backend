package com.medsafe.service;

import com.medsafe.dto.FarmaciaRequest;
import com.medsafe.dto.FarmaciaResponse;
import com.medsafe.exception.ResourceNotFoundException;
import com.medsafe.model.Farmacia;
import com.medsafe.repository.FarmaciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmaciaService {

    private final FarmaciaRepository farmaciaRepository;

    public FarmaciaResponse criar(FarmaciaRequest request) {
        Farmacia farmacia = new Farmacia();
        aplicarRequest(farmacia, request);
        return FarmaciaResponse.from(farmaciaRepository.save(farmacia));
    }

    @Transactional(readOnly = true)
    public List<FarmaciaResponse> listarTodos() {
        return farmaciaRepository.findAll().stream()
                .map(FarmaciaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FarmaciaResponse buscarPorId(Long id) {
        return FarmaciaResponse.from(buscarEntidade(id));
    }

    public FarmaciaResponse atualizar(Long id, FarmaciaRequest request) {
        Farmacia farmacia = buscarEntidade(id);
        aplicarRequest(farmacia, request);
        return FarmaciaResponse.from(farmaciaRepository.save(farmacia));
    }

    public void excluir(Long id) {
        farmaciaRepository.delete(buscarEntidade(id));
    }

    private Farmacia buscarEntidade(Long id) {
        return farmaciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmacia", id));
    }

    private void aplicarRequest(Farmacia farmacia, FarmaciaRequest request) {
        farmacia.setNome(request.nome());
        farmacia.setTelefone(request.telefone());
        String link = request.whatsappLink();
        farmacia.setWhatsappLink((link == null || link.isBlank()) ? gerarLinkWhatsapp(request.telefone()) : link);
    }

    /** Gera um link wa.me a partir do telefone quando a farmácia não informa um link próprio. */
    private String gerarLinkWhatsapp(String telefone) {
        String apenasDigitos = telefone.replaceAll("\\D", "");
        String comCodigoPais = apenasDigitos.startsWith("55") ? apenasDigitos : "55" + apenasDigitos;
        return "https://wa.me/" + comCodigoPais;
    }
}
