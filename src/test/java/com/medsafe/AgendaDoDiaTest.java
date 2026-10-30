package com.medsafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgendaDoDiaTest {

    private static final String DIA = "2026-11-02";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode agenda(SessaoDeTeste sessao, String data) throws Exception {
        String resposta = mockMvc.perform(get("/api/historico-ingestao/agenda")
                        .param("data", data)
                        .header("Authorization", sessao.bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta);
    }

    @Test
    void geraUmaDosePendentePorHorarioEnaoDuplicaAoConsultarDeNovo() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Agenda Maria");
        Cenarios.criarMedicamento(mockMvc, objectMapper, sessao, "Losartana 50mg", null, 30, "20:00,08:00");

        JsonNode primeira = agenda(sessao, DIA);
        assertAgendaOrdenada(primeira);

        JsonNode segunda = agenda(sessao, DIA);
        org.junit.jupiter.api.Assertions.assertEquals(
                primeira.get(0).get("id").asLong(), segunda.get(0).get("id").asLong());
        org.junit.jupiter.api.Assertions.assertEquals(
                primeira.get(1).get("id").asLong(), segunda.get(1).get("id").asLong());

        mockMvc.perform(get("/api/historico-ingestao").header("Authorization", sessao.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private void assertAgendaOrdenada(JsonNode agenda) {
        org.junit.jupiter.api.Assertions.assertEquals(2, agenda.size());
        org.junit.jupiter.api.Assertions.assertEquals(DIA + "T08:00:00", agenda.get(0).get("dataHoraProgramada").asString());
        org.junit.jupiter.api.Assertions.assertEquals(DIA + "T20:00:00", agenda.get(1).get("dataHoraProgramada").asString());
        org.junit.jupiter.api.Assertions.assertEquals("PENDENTE", agenda.get(0).get("status").asString());
        org.junit.jupiter.api.Assertions.assertEquals("Losartana 50mg", agenda.get(0).get("medicamentoNome").asString());
    }

    @Test
    void marcarDoseComoTomadaAparecaNaAgendaEDaBaixaNoEstoque() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Agenda João");
        Long medicamentoId = Cenarios.criarMedicamento(
                mockMvc, objectMapper, sessao, "Metformina 850mg", null, 10, "08:00");

        Long doseId = agenda(sessao, DIA).get(0).get("id").asLong();
        mockMvc.perform(patch("/api/historico-ingestao/{id}/status", doseId)
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("status", "TOMADO"))))
                .andExpect(status().isOk());

        JsonNode depois = agenda(sessao, DIA);
        org.junit.jupiter.api.Assertions.assertEquals(1, depois.size());
        org.junit.jupiter.api.Assertions.assertEquals("TOMADO", depois.get(0).get("status").asString());

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId).header("Authorization", sessao.bearer()))
                .andExpect(jsonPath("$.quantidadeAtual", is(9)));
    }

    @Test
    void horarioRemovidoDoMedicamentoSaiDaAgendaSeAindaPendente() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Agenda Ana");
        Long medicamentoId = Cenarios.criarMedicamento(
                mockMvc, objectMapper, sessao, "Vitamina D", null, 60, "08:00,20:00");
        org.junit.jupiter.api.Assertions.assertEquals(2, agenda(sessao, DIA).size());

        mockMvc.perform(put("/api/medicamentos/{id}", medicamentoId)
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(sessao.usuarioId(), "Vitamina D", null, 60, "08:00"))))
                .andExpect(status().isOk());

        JsonNode depois = agenda(sessao, DIA);
        org.junit.jupiter.api.Assertions.assertEquals(1, depois.size());
        org.junit.jupiter.api.Assertions.assertEquals(DIA + "T08:00:00", depois.get(0).get("dataHoraProgramada").asString());
    }

    @Test
    void historicoPodeSerFiltradoPorPeriodoEVemDoMaisRecenteParaOMaisAntigo() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Histórico Rita");
        Cenarios.criarMedicamento(mockMvc, objectMapper, sessao, "Losartana", null, 30, "08:00");
        agenda(sessao, "2026-11-01");
        agenda(sessao, "2026-11-02");
        agenda(sessao, "2026-11-03");

        mockMvc.perform(get("/api/historico-ingestao")
                        .param("de", "2026-11-02")
                        .param("ate", "2026-11-03")
                        .header("Authorization", sessao.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dataHoraProgramada", is("2026-11-03T08:00:00")))
                .andExpect(jsonPath("$[1].dataHoraProgramada", is("2026-11-02T08:00:00")));

        mockMvc.perform(get("/api/historico-ingestao").header("Authorization", sessao.bearer()))
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
