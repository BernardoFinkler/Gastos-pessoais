CREATE TABLE usuarios (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          nome VARCHAR(100) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          senha VARCHAR(255) NOT NULL,
                          salario_mensal NUMERIC(15,2),
                          reserva_emergencia_mensal NUMERIC(15,2),
                          criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                                token VARCHAR(512) NOT NULL UNIQUE,
                                expira_em TIMESTAMP NOT NULL,
                                criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE categorias (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                            nome VARCHAR(100) NOT NULL,
                            tipo VARCHAR(20) NOT NULL,
                            essencialidade VARCHAR(20) NOT NULL,
                            padrao BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE lancamentos (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                             categoria_id UUID NOT NULL REFERENCES categorias(id),
                             tipo VARCHAR(20) NOT NULL,
                             valor NUMERIC(15,2) NOT NULL,
                             data DATE NOT NULL,
                             descricao VARCHAR(255),
                             recorrente BOOLEAN NOT NULL DEFAULT false,
                             meses_recorrencia INT,
                             criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE contas_fixas (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                              nome VARCHAR(100) NOT NULL,
                              valor_base NUMERIC(15,2) NOT NULL,
                              tipo_gasto VARCHAR(20) NOT NULL,
                              essencialidade VARCHAR(20) NOT NULL,
                              ativa BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE contas_fixas_registros (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        conta_fixa_id UUID NOT NULL REFERENCES contas_fixas(id) ON DELETE CASCADE,
                                        usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                                        mes INT NOT NULL,
                                        ano INT NOT NULL,
                                        valor_real NUMERIC(15,2) NOT NULL,
                                        observacao VARCHAR(255),
                                        UNIQUE (conta_fixa_id, mes, ano)
);

CREATE TABLE orcamentos (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                            categoria_id UUID NOT NULL REFERENCES categorias(id) ON DELETE CASCADE,
                            mes INT NOT NULL,
                            ano INT NOT NULL,
                            valor_limite NUMERIC(15,2) NOT NULL,
                            UNIQUE (usuario_id, categoria_id, mes, ano)
);