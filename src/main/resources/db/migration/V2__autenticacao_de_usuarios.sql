-- Login por telefone + senha: guarda o hash BCrypt e impede dois usuarios com o mesmo telefone.
-- A coluna e opcional para nao quebrar linhas criadas antes da autenticacao existir.
ALTER TABLE usuarios ADD COLUMN senha_hash VARCHAR(100);

CREATE UNIQUE INDEX uk_usuarios_telefone ON usuarios (telefone);
