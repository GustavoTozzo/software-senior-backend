package com.medsafe;

import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Usuario ja cadastrado e autenticado, para os testes chamarem endpoints protegidos. */
record SessaoDeTeste(Long usuarioId, String token, String telefone) {

    private static final AtomicLong SEQUENCIA = new AtomicLong(10_000_000L);

    String bearer() {
        return "Bearer " + token;
    }

    /** Telefone unico por chamada, porque o H2 em memoria e compartilhado entre os testes. */
    static String novoTelefone() {
        return "119" + SEQUENCIA.incrementAndGet();
    }

    static SessaoDeTeste registrar(MockMvc mockMvc, ObjectMapper objectMapper, String nome) throws Exception {
        String telefone = novoTelefone();
        String corpo = objectMapper.writeValueAsString(
                java.util.Map.of("nome", nome, "telefone", telefone, "idade", 72, "senha", "senha123"));

        String resposta = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(corpo))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resposta);
        return new SessaoDeTeste(json.get("usuario").get("id").asLong(), json.get("token").asString(), telefone);
    }
}
