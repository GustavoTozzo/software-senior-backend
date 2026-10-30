package com.medsafe;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

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
class PostgresSchemaValidationTest {

    private static final EmbeddedPostgres POSTGRES = iniciarPostgres();

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
}
