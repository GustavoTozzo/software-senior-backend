-- Schema inicial do MedSafe Senior (equivalente ao database/schema.sql, gerenciado agora via Flyway)

-- 1. Tabela de Usuários (Idosos e Cuidadores/Familiares)
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20),
    idade INT,
    tipo_perfil VARCHAR(50) DEFAULT 'IDOSO', -- IDOSO ou CUIDADOR
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Medicamentos
CREATE TABLE medicamentos (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome_comercial VARCHAR(150) NOT NULL,
    codigo_barras VARCHAR(50) UNIQUE,
    quantidade_atual INT NOT NULL,
    dose_frequencia VARCHAR(100) NOT NULL,
    horarios_programados TEXT NOT NULL
);

-- 3. Tabela de Histórico de Ingestão
CREATE TABLE historico_ingestao (
    id SERIAL PRIMARY KEY,
    medicamento_id INT NOT NULL REFERENCES medicamentos(id) ON DELETE CASCADE,
    data_hora_programada TIMESTAMP NOT NULL,
    data_hora_realizada TIMESTAMP,
    status VARCHAR(30) DEFAULT 'PENDENTE' -- PENDENTE, TOMADO, PULADO
);

-- 4. Tabela de Farmácias
CREATE TABLE farmacias (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    whatsapp_link VARCHAR(255)
);

CREATE INDEX idx_medicamentos_usuario_id ON medicamentos(usuario_id);
CREATE INDEX idx_historico_medicamento_id ON historico_ingestao(medicamento_id);
