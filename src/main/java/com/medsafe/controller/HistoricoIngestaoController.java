package com.medsafe.controller;

import com.medsafe.dto.HistoricoIngestaoRequest;
import com.medsafe.dto.HistoricoIngestaoResponse;
import com.medsafe.dto.HistoricoIngestaoStatusUpdateRequest;
import com.medsafe.service.HistoricoIngestaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historico-ingestao")
@RequiredArgsConstructor
public class HistoricoIngestaoController {

    private final HistoricoIngestaoService historicoService;

    @PostMapping
    public ResponseEntity<HistoricoIngestaoResponse> criar(@Valid @RequestBody HistoricoIngestaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historicoService.criar(request));
    }

    @GetMapping
    public List<HistoricoIngestaoResponse> listar(
            @RequestParam(required = false) Long medicamentoId,
            @RequestParam(required = false) Long usuarioId) {
        if (medicamentoId != null) {
            return historicoService.listarPorMedicamento(medicamentoId);
        }
        if (usuarioId != null) {
            return historicoService.listarPorUsuario(usuarioId);
        }
        return historicoService.listarTodos();
    }

    @GetMapping("/{id}")
    public HistoricoIngestaoResponse buscarPorId(@PathVariable Long id) {
        return historicoService.buscarPorId(id);
    }

    @PatchMapping("/{id}/status")
    public HistoricoIngestaoResponse atualizarStatus(
            @PathVariable Long id, @Valid @RequestBody HistoricoIngestaoStatusUpdateRequest request) {
        return historicoService.atualizarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        historicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
