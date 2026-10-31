# MedSafe Senior (nome temporário) - Backend & Database API

API RESTful desenvolvida em **Java com Spring Boot** para gerenciar as regras de negócio, persistência de dados e segurança do aplicativo *MedSafe Senior*.

## Stack Tecnológica
* **Linguagem:** Java 17+
* **Framework:** Spring Boot 4 (Spring MVC, Spring Data JPA, Spring Security como *resource server* JWT, Bean Validation)
* **Banco de Dados:** PostgreSQL, com migrations versionadas pelo Flyway
* **Build:** Maven (wrapper `./mvnw`)
* **Testes:** JUnit 5 + MockMvc (H2 em memória) e PostgreSQL embarcado para validar o schema real

## Arquitetura de Camadas
O projeto está organizado nos seguintes pacotes principais:
- `controller`: controladores REST que expõem os endpoints HTTP.
- `service`: regras de negócio (estoque, agenda do dia, dono dos dados).
- `repository`: interfaces Spring Data JPA.
- `model`: entidades JPA mapeadas para o PostgreSQL.
- `dto`: objetos de entrada/saída da API (nenhuma entidade é exposta diretamente).
- `security`: configuração do Spring Security, emissão do JWT e acesso ao usuário logado.
- `exception`: exceções de negócio e tratamento global de erros (`@RestControllerAdvice`).

## Modelagem do Banco de Dados (PostgreSQL)
1. **usuarios:** nome, telefone (login, único), idade, perfil (IDOSO/CUIDADOR) e hash BCrypt da senha.
2. **medicamentos:** vinculado ao usuário; nome comercial, código de barras (único por usuário), quantidade em estoque, frequência e horários programados (`HH:mm` separados por vírgula).
3. **historico_ingestao:** uma linha por dose programada, com status PENDENTE/TOMADO/PULADO (única por medicamento e horário).
4. **farmacias:** contatos e link de WhatsApp para reposição de estoque.

As tabelas são criadas pelas migrations em `src/main/resources/db/migration` (V1 schema inicial, V2 autenticação, V3 restrições de unicidade). O banco em si (`medsafe_db`) é criado uma única vez, manualmente (veja `database/schema.sql`).

## Autenticação (JWT)
* `POST /api/auth/register` e `POST /api/auth/login` (públicos) devolvem `{ token, expiraEmSegundos, usuario }`.
* Todos os outros endpoints exigem o header `Authorization: Bearer <token>`; sem ele a API responde `401`.
* O token é assinado com HS256; o `subject` é o id do usuário. Cada usuário só lê e altera os **próprios** dados (`403` caso contrário).
* Configuração: `JWT_SECRET` (obrigatória fora do perfil `dev`, mínimo de 32 caracteres) e `JWT_EXPIRATION` (padrão `7d`).

## Endpoints
| Recurso | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Usuário | `GET /api/usuarios/me`, `GET/PUT/DELETE /api/usuarios/{id}` |
| Medicamentos | `POST/GET /api/medicamentos`, `GET/PUT/DELETE /api/medicamentos/{id}` |
| Histórico | `GET /api/historico-ingestao?de=&ate=&medicamentoId=`, `GET /api/historico-ingestao/agenda?data=`, `PATCH /api/historico-ingestao/{id}/status`, `POST/DELETE` |
| Farmácias | `POST/GET /api/farmacias`, `GET/PUT/DELETE /api/farmacias/{id}` |

Regras principais: marcar uma dose como TOMADO dá baixa de 1 unidade no estoque (uma vez por dose); `GET .../agenda` gera as doses PENDENTES do dia a partir dos horários de cada medicamento, sem duplicar; `estoqueBaixo` indica 5 unidades ou menos. Erros seguem o formato `{ status, error, message, fieldErrors }`.

## Como rodar
**Sem instalar PostgreSQL** (banco embarcado, dados somem ao encerrar):
```bash
./mvnw spring-boot:test-run -Dspring-boot.run.main-class=com.medsafe.ExecutarComPostgresEmbarcado
```
**Com um PostgreSQL local:** crie o banco `medsafe_db` e rode `./mvnw spring-boot:run` (perfil `dev`; ajuste `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` se preciso). O Flyway cria as tabelas na primeira execução.

A API sobe em `http://localhost:8080`. O emulador do Android Studio enxerga a máquina em `http://10.0.2.2:8080`.

## Testes
```bash
./mvnw test
```
Cobrem cadastro/login, isolamento entre usuários, CRUDs, regras de estoque, agenda do dia e filtros do histórico. Um teste sobe um PostgreSQL embarcado, aplica as migrations e deixa o Hibernate validar o schema contra as entidades (`ddl-auto=validate`).
