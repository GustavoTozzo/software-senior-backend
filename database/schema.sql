CREATE DATABASE medsafe_db;

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
    usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
    nome_comercial VARCHAR(150) NOT NULL,
    codigo_barras VARCHAR(50) UNIQUE,
    quantidade_atual INT NOT NULL,
    dose_frequencia VARCHAR(100) NOT NULL, 
    horarios_programados TEXT NOT NULL 
);

-- 3. Tabela de Histórico de Ingestão
CREATE TABLE historico_ingestao (
    id SERIAL PRIMARY KEY,
    medicamento_id INT REFERENCES medicamentos(id) ON DELETE CASCADE,
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