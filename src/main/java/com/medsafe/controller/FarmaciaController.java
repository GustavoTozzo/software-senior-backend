package com.medsafe.controller;

import com.medsafe.dto.FarmaciaRequest;
import com.medsafe.dto.FarmaciaResponse;
import com.medsafe.service.FarmaciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmacias")
@RequiredArgsConstructor
public class FarmaciaController {

    private final FarmaciaService farmaciaService;

    @PostMapping
    public ResponseEntity<FarmaciaResponse> criar(@Valid @RequestBody FarmaciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmaciaService.criar(request));
    }

    @GetMapping
    public List<FarmaciaResponse> listar() {
        return farmaciaService.listarTodos();
    }

    @GetMapping("/{id}")
    public FarmaciaResponse buscarPorId(@PathVariable Long id) {
        return farmaciaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public FarmaciaResponse atualizar(@PathVariable Long id, @Valid @RequestBody FarmaciaRequest request) {
        return farmaciaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        farmaciaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
