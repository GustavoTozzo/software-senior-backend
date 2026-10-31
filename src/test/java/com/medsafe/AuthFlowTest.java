package com.medsafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String comMascara(String telefone) {
        return "(" + telefone.substring(0, 2) + ") " + telefone.substring(2, 7) + "-" + telefone.substring(7);
    }

    private String json(Map<String, Object> corpo) throws Exception {
        return objectMapper.writeValueAsString(corpo);
    }

    @Test
    void cadastroDevolveTokenENaoExpoeASenha() throws Exception {
        String telefone = SessaoDeTeste.novoTelefone();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json(Map.of("nome", "Maria Silva", "telefone", comMascara(telefone),
                                "idade", 72, "senha", "senha123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.usuario.telefone", is(telefone)))
                .andExpect(jsonPath("$.usuario.tipoPerfil", is("IDOSO")))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist())
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist());
    }

    @Test
    void loginFuncionaComOTelefoneComOuSemMascara() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "João Pereira");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(json(Map.of("telefone", comMascara(sessao.telefone()), "senha", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.id", is(sessao.usuarioId().intValue())));
    }

    @Test
    void senhaErradaETelefoneDesconhecidoRecebemAMesmaMensagem() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Ana Souza");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(json(Map.of("telefone", sessao.telefone(), "senha", "errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Telefone ou senha incorretos")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(json(Map.of("telefone", SessaoDeTeste.novoTelefone(), "senha", "senha123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Telefone ou senha incorretos")));
    }

    @Test
    void naoPermiteDuasContasComOMesmoTelefone() throws Exception {
        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Carlos Lima");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json(Map.of("nome", "Outra Pessoa", "telefone", comMascara(sessao.telefone()),
                                "senha", "senha123"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", is("Já existe uma conta com esse telefone")));
    }

    @Test
    void cadastroValidaSenhaCurtaETelefoneIncompleto() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json(Map.of("nome", "Teste", "telefone", SessaoDeTeste.novoTelefone(), "senha", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.senha").exists());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json(Map.of("nome", "Teste", "telefone", "1234", "senha", "senha123"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void endpointsProtegidosExigemTokenValido() throws Exception {
        mockMvc.perform(get("/api/medicamentos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        mockMvc.perform(get("/api/medicamentos").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());

        SessaoDeTeste sessao = SessaoDeTeste.registrar(mockMvc, objectMapper, "Rita Alves");
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", sessao.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Rita Alves")));
    }
}
