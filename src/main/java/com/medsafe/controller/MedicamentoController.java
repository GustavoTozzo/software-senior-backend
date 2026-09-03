package com.medsafe.controller;

import com.medsafe.dto.MedicamentoRequest;
import com.medsafe.dto.MedicamentoResponse;
import com.medsafe.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @PostMapping
    public ResponseEntity<MedicamentoResponse> criar(@Valid @RequestBody MedicamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.criar(request));
    }

    @GetMapping
    public List<MedicamentoResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return usuarioId != null
                ? medicamentoService.listarPorUsuario(usuarioId)
                : medicamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public MedicamentoResponse buscarPorId(@PathVariable Long id) {
        return medicamentoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public MedicamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody MedicamentoRequest request) {
        return medicamentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        medicamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
