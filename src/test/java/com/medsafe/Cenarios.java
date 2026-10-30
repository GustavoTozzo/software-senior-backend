package com.medsafe;

import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Passos de preparacao reutilizados pelos testes de integracao. */
final class Cenarios {

    private Cenarios() {
    }

    static Map<String, Object> corpoMedicamento(
            Long usuarioId, String nome, String codigoBarras, int quantidade, String horarios) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("usuarioId", usuarioId);
        corpo.put("nomeComercial", nome);
        corpo.put("codigoBarras", codigoBarras);
        corpo.put("quantidadeAtual", quantidade);
        corpo.put("doseFrequencia", "conforme horarios");
        corpo.put("horariosProgramados", horarios);
        return corpo;
    }

    /** Cadastra um medicamento para o usuario e devolve o id gerado. */
    static Long criarMedicamento(
            MockMvc mockMvc, ObjectMapper objectMapper, SessaoDeTeste sessao,
            String nome, String codigoBarras, int quantidade, String horarios) throws Exception {
        String resposta = mockMvc.perform(post("/api/medicamentos")
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpoMedicamento(sessao.usuarioId(), nome, codigoBarras, quantidade, horarios))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
