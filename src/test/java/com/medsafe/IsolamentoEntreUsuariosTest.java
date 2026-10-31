package com.medsafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Um usuario nao enxerga nem altera os dados de outro. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IsolamentoEntreUsuariosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usuarioNaoAcessaMedicamentoDeOutro() throws Exception {
        SessaoDeTeste dona = SessaoDeTeste.registrar(mockMvc, objectMapper, "Dona do Remédio");
        SessaoDeTeste intruso = SessaoDeTeste.registrar(mockMvc, objectMapper, "Outro Usuário");
        Long medicamentoId = Cenarios.criarMedicamento(mockMvc, objectMapper, dona, "Losartana 50mg", null, 30, "08:00");

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId).header("Authorization", intruso.bearer()))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/medicamentos/{id}", medicamentoId)
                        .header("Authorization", intruso.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(intruso.usuarioId(), "Trocado", null, 1, "09:00"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/medicamentos/{id}", medicamentoId).header("Authorization", intruso.bearer()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/historico-ingestao")
                        .param("medicamentoId", medicamentoId.toString())
                        .header("Authorization", intruso.bearer()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId).header("Authorization", dona.bearer()))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioNaoListaNemCriaDadosEmNomeDeOutro() throws Exception {
        SessaoDeTeste dona = SessaoDeTeste.registrar(mockMvc, objectMapper, "Dona dos Dados");
        SessaoDeTeste intruso = SessaoDeTeste.registrar(mockMvc, objectMapper, "Curioso");
        Cenarios.criarMedicamento(mockMvc, objectMapper, dona, "Metformina 850mg", null, 20, "12:00");

        mockMvc.perform(get("/api/medicamentos")
                        .param("usuarioId", dona.usuarioId().toString())
                        .header("Authorization", intruso.bearer()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios/{id}", dona.usuarioId()).header("Authorization", intruso.bearer()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", intruso.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(dona.usuarioId(), "Intruso", null, 1, "10:00"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/medicamentos").header("Authorization", intruso.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void mesmoCodigoDeBarrasValeParaUsuariosDiferentesMasNaoRepeteNoMesmoUsuario() throws Exception {
        SessaoDeTeste primeira = SessaoDeTeste.registrar(mockMvc, objectMapper, "Primeira");
        SessaoDeTeste segunda = SessaoDeTeste.registrar(mockMvc, objectMapper, "Segunda");
        String codigo = "7891000100103";

        Cenarios.criarMedicamento(mockMvc, objectMapper, primeira, "Losartana", codigo, 30, "08:00");
        Cenarios.criarMedicamento(mockMvc, objectMapper, segunda, "Losartana", codigo, 30, "08:00");

        mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", primeira.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Cenarios.corpoMedicamento(primeira.usuarioId(), "Repetido", codigo, 5, "09:00"))))
                .andExpect(status().isUnprocessableEntity());
    }
}
