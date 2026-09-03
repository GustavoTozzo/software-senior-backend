package com.medsafe;

import com.medsafe.dto.FarmaciaRequest;
import com.medsafe.dto.HistoricoIngestaoRequest;
import com.medsafe.dto.HistoricoIngestaoStatusUpdateRequest;
import com.medsafe.dto.MedicamentoRequest;
import com.medsafe.dto.UsuarioRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de ponta a ponta (controller -> service -> repository -> H2) cobrindo o
 * fluxo basico de cadastrar um Usuario e um Medicamento vinculado a ele.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioMedicamentoFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCriarUsuarioEMedicamentoVinculado() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest("Maria Silva", "11999990000", 72, null);

        String usuarioJson = mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.tipoPerfil", is("IDOSO")))
                .andReturn().getResponse().getContentAsString();

        Long usuarioId = objectMapper.readTree(usuarioJson).get("id").asLong();

        MedicamentoRequest medicamentoRequest = new MedicamentoRequest(
                usuarioId, "Losartana 50mg", "7891234567890", 30, "1x ao dia", "08:00");

        mockMvc.perform(post("/api/medicamentos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medicamentoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId", is(usuarioId.intValue())))
                .andExpect(jsonPath("$.quantidadeAtual", is(30)));

        mockMvc.perform(get("/api/medicamentos").param("usuarioId", usuarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void deveRetornar404ParaUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveRetornar400ParaUsuarioSemNome() throws Exception {
        UsuarioRequest invalido = new UsuarioRequest("", null, null, null);

        mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists());
    }

    @Test
    void deveGerarLinkDoWhatsappQuandoNaoInformado() throws Exception {
        FarmaciaRequest request = new FarmaciaRequest("Farmácia Popular Centro", "(11) 91234-5678", null);

        mockMvc.perform(post("/api/farmacias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.whatsappLink", is("https://wa.me/5511912345678")));
    }

    @Test
    void deveDarBaixaNoEstoqueAoMarcarDoseComoTomada() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest("João Pereira", "11988887777", 80, null);
        String usuarioJson = mockMvc.perform(post("/api/usuarios")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andReturn().getResponse().getContentAsString();
        Long usuarioId = objectMapper.readTree(usuarioJson).get("id").asLong();

        MedicamentoRequest medicamentoRequest = new MedicamentoRequest(
                usuarioId, "Metformina 850mg", null, 10, "2x ao dia", "08:00,20:00");
        String medicamentoJson = mockMvc.perform(post("/api/medicamentos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medicamentoRequest)))
                .andReturn().getResponse().getContentAsString();
        Long medicamentoId = objectMapper.readTree(medicamentoJson).get("id").asLong();

        HistoricoIngestaoRequest historicoRequest = new HistoricoIngestaoRequest(
                medicamentoId, java.time.LocalDateTime.now());
        String historicoJson = mockMvc.perform(post("/api/historico-ingestao")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(historicoRequest)))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andReturn().getResponse().getContentAsString();
        Long historicoId = objectMapper.readTree(historicoJson).get("id").asLong();

        HistoricoIngestaoStatusUpdateRequest statusUpdate =
                new HistoricoIngestaoStatusUpdateRequest(com.medsafe.model.StatusIngestao.TOMADO);
        mockMvc.perform(patch("/api/historico-ingestao/{id}/status", historicoId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("TOMADO")))
                .andExpect(jsonPath("$.dataHoraRealizada").exists());

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId))
                .andExpect(jsonPath("$.quantidadeAtual", is(9)));
    }
}
