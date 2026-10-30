package com.medsafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de ponta a ponta (controller -> service -> repository -> H2) dos CRUDs e regras de negocio,
 * sempre autenticado como um usuario recem-cadastrado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioMedicamentoFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCriarMedicamentoVinculadoAoUsuarioLogado() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Maria Silva");

        mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(sessao.usuarioId(), "Losartana 50mg", "7891234567890", 30, "08:00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId", is(sessao.usuarioId().intValue())))
                .andExpect(jsonPath("$.quantidadeAtual", is(30)))
                .andExpect(jsonPath("$.estoqueBaixo", is(false)));

        mockMvc.perform(get("/api/medicamentos").header("Authorization", sessao.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deveSinalizarEstoqueBaixo() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Estoque Curto");

        mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(sessao.usuarioId(), "Losartana", null, 3, "08:00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estoqueBaixo", is(true)));
    }

    @Test
    void deveRetornar404ParaMedicamentoInexistente() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Sem Medicamento");

        mockMvc.perform(get("/api/medicamentos/999999").header("Authorization", sessao.bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveValidarCamposDoMedicamento() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Dados Ruins");

        mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(sessao.usuarioId(), "", null, -1, "8h e 20h"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nomeComercial").exists())
                .andExpect(jsonPath("$.fieldErrors.quantidadeAtual").exists())
                .andExpect(jsonPath("$.fieldErrors.horariosProgramados").exists());
    }

    @Test
    void jsonMalFormadoERotaInexistenteNaoViram500() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Requisição Ruim");

        mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content("{nao e json"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/nao-existe").header("Authorization", sessao.bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveGerarLinkDoWhatsappQuandoNaoInformado() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Quem Liga");

        mockMvc.perform(post("/api/farmacias")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("nome", "Farmácia Popular Centro", "telefone", "(11) 91234-5678"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.whatsappLink", is("https://wa.me/5511912345678")));
    }

    @Test
    void deveDarBaixaNoEstoqueAoMarcarDoseComoTomada() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "João Pereira");
        Long medicamentoId = Cenarios.criarMedicamento(
                mockMvc, objectMapper, sessao, "Metformina 850mg", null, 10, "08:00,20:00");

        String historicoJson = mockMvc.perform(post("/api/historico-ingestao")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "medicamentoId", medicamentoId,
                                "dataHoraProgramada", LocalDateTime.of(2026, 11, 2, 8, 0).toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.medicamentoNome", is("Metformina 850mg")))
                .andReturn().getResponse().getContentAsString();
        Long historicoId = objectMapper.readTree(historicoJson).get("id").asLong();

        mockMvc.perform(patch("/api/historico-ingestao/{id}/status", historicoId)
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("status", "TOMADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("TOMADO")))
                .andExpect(jsonPath("$.dataHoraRealizada").exists());

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId).header("Authorization", sessao.bearer()))
                .andExpect(jsonPath("$.quantidadeAtual", is(9)));

        mockMvc.perform(patch("/api/historico-ingestao/{id}/status", historicoId)
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("status", "TOMADO"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId).header("Authorization", sessao.bearer()))
                .andExpect(jsonPath("$.quantidadeAtual", is(9)));
    }
}
