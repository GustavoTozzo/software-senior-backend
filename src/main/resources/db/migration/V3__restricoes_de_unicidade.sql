-- O codigo de barras identifica o produto, entao a mesma caixa pode estar cadastrada por
-- usuarios diferentes: a unicidade passa a valer por usuario, nao no sistema inteiro.
ALTER TABLE medicamentos DROP CONSTRAINT medicamentos_codigo_barras_key;

CREATE UNIQUE INDEX uk_medicamentos_usuario_codigo_barras ON medicamentos (usuario_id, codigo_barras);

-- Garante que a agenda do dia seja idempotente: uma dose por medicamento e horario programado.
CREATE UNIQUE INDEX uk_historico_medicamento_programada ON historico_ingestao (medicamento_id, data_hora_programada);
