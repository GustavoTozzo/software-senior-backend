package com.medsafe;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicacao contra um PostgreSQL de verdade (embarcado), aplicando as migrations do
 * Flyway e deixando o Hibernate validar o schema resultante contra as entidades
 * (ddl-auto=validate, o mesmo modo do perfil dev). Os demais testes rodam em H2 com o schema
 * gerado pelo Hibernate e, por isso, nao detectam divergencia entre migrations e entidades.
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
        "spring.datasource.driver-class-name=org.postgresql.Driver"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostgresSchemaValidationTest {

    private static final EmbeddedPostgres POSTGRES = iniciarPostgres();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static EmbeddedPostgres iniciarPostgres() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @Test
    void migrationsDoFlywayBatemComAsEntidadesJpa() {
    }

    @Test
    void fluxoCompletoFuncionaNoPostgres() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Maria no Postgres");
        Long medicamentoId = Cenarios.criarMedicamento(
                mockMvc, objectMapper, sessao, "Losartana 50mg", "7891234567890", 30, "08:00,20:00");

        String resposta = mockMvc.perform(get("/api/historico-ingestao/agenda")
                        .param("data", "2026-11-02")
                        .header("Authorization", sessao.bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode agenda = objectMapper.readTree(resposta);
        org.junit.jupiter.api.Assertions.assertEquals(2, agenda.size());

        mockMvc.perform(patch("/api/historico-ingestao/{id}/status", agenda.get(0).get("id").asLong())
                        .header("Authorization", sessao.bearer())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("status", "TOMADO"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/medicamentos/{id}", medicamentoId).header("Authorization", sessao.bearer()))
                .andExpect(jsonPath("$.quantidadeAtual", is(29)));

        mockMvc.perform(get("/api/historico-ingestao/agenda")
                        .param("data", "2026-11-02")
                        .header("Authorization", sessao.bearer()))
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].status", is("TOMADO")));
    }
}
