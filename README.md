<<<<<<< HEAD
# MedSafe Senior (nome temporário) - Backend & Database API

API RESTful desenvolvida em **Java com Spring Boot** para gerenciar as regras de negócio, persistência de dados e segurança do aplicativo *MedSafe Senior*.

## Stack Tecnológica
* **Linguagem:** Java (versão 17+)
* **Framework:** Spring Boot (Spring Web, Spring Data JPA, Spring Security)
* **Banco de Dados:** PostgreSQL
* **Gerenciador de Dependências:** Maven / Gradle

## Arquitetura de Camadas (Clean Architecture / MVC Adaptado)
O projeto do backend está organizado nos seguintes pacotes principais:
- `controller`: Controladores REST responsáveis por expor os endpoints HTTP.
- `service`: Camada de regras de negócio da aplicação.
- `repository`: Interfaces de comunicação com o banco de dados via Spring Data JPA.
- `model` (ou `entity`): Entidades de mapeamento objeto-relacional (ORM) para o PostgreSQL.

## Modelagem Inicial do Banco de Dados (PostgreSQL)
As principais entidades mapeadas no banco relacional são:
1. **Usuarios:** Armazena dados cadastrais (nome, telefone, idade).
2. **Medicamentos:** Vinculado ao usuário, armazena nome comercial, código de barras e quantidade em estoque.
3. **HistoricoIngestao:** Registra o status e o horário programado/realizado da ingestão dos remédios.
4. **Farmacias:** Contatos e links de direcionamento rápido (WhatsApp/Telefone) para reposição de estoque.
=======
# software-senior-backend
Código Java com Spring Boot e scripts do PostgreSQL do nosso aplicativo mobile.
>>>>>>> 2f6452fb381ad258bf8a9db4265c9d84692c3e82
