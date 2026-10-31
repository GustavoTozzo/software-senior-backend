package com.medsafe;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;

/**
 * Sobe a API de verdade (perfil dev, Flyway rodando as migrations) contra um PostgreSQL embarcado,
 * para desenvolver e testar o app sem instalar nem configurar um Postgres na maquina.
 * Os dados ficam so em memoria/pasta temporaria e somem ao encerrar.
 *
 * Execucao: ./mvnw spring-boot:test-run -Dspring-boot.run.main-class=com.medsafe.ExecutarComPostgresEmbarcado
 */
public class ExecutarComPostgresEmbarcado {

    public static void main(String[] args) throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postgres.close();
            } catch (Exception ignorado) {
                // encerrando: a pasta temporaria do banco e limpa pelo proprio sistema
            }
        }));

        System.setProperty("DB_URL", "jdbc:postgresql://localhost:" + postgres.getPort() + "/postgres");
        SpringApplication.run(SoftwareSeniorBackendApplication.class, args);
    }
}
