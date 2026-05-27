-- =============================================================
-- V1__create_schema.sql
-- Schema do Sistema Oficina Mecânica
--
-- Nomes de coluna alinhados com as entidades JPA:
--   - PhysicalNamingStrategyStandardImpl: usa nome literal do campo
--   - @Column(name=...) sobrescreve quando presente
-- =============================================================

-- enderecos
-- Endereco.localidade → @Column(name="cidade") → coluna cidade
-- Endereco.uf        → @Column(name="estado")  → coluna estado
CREATE TABLE IF NOT EXISTS enderecos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    cep         VARCHAR(9)   NOT NULL,
    logradouro  VARCHAR(200) NOT NULL,
    numero      VARCHAR(10)  NOT NULL,
    complemento VARCHAR(100),
    bairro      VARCHAR(100) NOT NULL,
    cidade      VARCHAR(100) NOT NULL,
    estado      VARCHAR(2)   NOT NULL
);

-- clientes
-- @Column(name="data_cadastro") → coluna data_cadastro
-- @Column(name="nome_completo") → coluna nome_completo
-- @Column(name="cpf_cnpj")     → coluna cpf_cnpj
-- @Column(name="endereco_id")  → coluna endereco_id
CREATE TABLE IF NOT EXISTS clientes (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf_cnpj      VARCHAR(14)  NOT NULL UNIQUE,
    telefone      VARCHAR(20)  NOT NULL,
    email         VARCHAR(254) NOT NULL UNIQUE,
    data_cadastro TIMESTAMP,
    endereco_id   BIGINT,
    CONSTRAINT fk_cliente_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id)
);

-- funcionarios
-- Sem @Column na maioria dos campos → PhysicalNamingStrategyStandardImpl usa nome literal
-- Exceções com @Column explícito: cpf_cnpj, senha_hash
-- dataCadastro sem @Column → adicionado @Column(name="data_cadastro") na entidade
CREATE TABLE IF NOT EXISTS funcionarios (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    cpf_cnpj      VARCHAR(14)  NOT NULL UNIQUE,
    usuario       VARCHAR(50)  NOT NULL UNIQUE,
    senha_hash    VARCHAR(60)  NOT NULL,
    cargo         VARCHAR(50)  NOT NULL,
    telefone      VARCHAR(20),
    email         VARCHAR(254),
    data_cadastro TIMESTAMP
);

-- veiculos
-- @Column(name="data_cadastro") → coluna data_cadastro
-- @Column(name="cliente_id")    → coluna cliente_id
CREATE TABLE IF NOT EXISTS veiculos (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    placa         VARCHAR(7)   NOT NULL UNIQUE,
    modelo        VARCHAR(50)  NOT NULL,
    marca         VARCHAR(50)  NOT NULL,
    ano           INTEGER      NOT NULL,
    cor           VARCHAR(30),
    quilometragem DOUBLE,
    data_cadastro TIMESTAMP    NOT NULL,
    cliente_id    UUID         NOT NULL,
    CONSTRAINT fk_veiculo_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- atendimentos
-- @Column(name="data_entrada")      → coluna data_entrada
-- @Column(name="data_conclusao")    → coluna data_conclusao
-- @Column(name="data_cadastro")     → coluna data_cadastro
-- descricaoServico sem @Column(name) → campo literal: descricaoServico
--   mas @Column(columnDefinition="TEXT") presente → Hibernate usa nome do campo: descricaoServico
--   Corrigido abaixo para descricao_servico via @Column(name=) na entidade (ver fix da entidade)
CREATE TABLE IF NOT EXISTS atendimentos (
    id                  UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    descricao_servico   CLOB,
    status              VARCHAR(20) NOT NULL,
    data_entrada        TIMESTAMP,
    data_conclusao      TIMESTAMP,
    data_cadastro       TIMESTAMP,
    cliente_id          UUID        NOT NULL,
    veiculo_id          UUID        NOT NULL,
    funcionario_id      UUID        NOT NULL,
    CONSTRAINT fk_atendimento_cliente     FOREIGN KEY (cliente_id)     REFERENCES clientes(id),
    CONSTRAINT fk_atendimento_veiculo     FOREIGN KEY (veiculo_id)     REFERENCES veiculos(id),
    CONSTRAINT fk_atendimento_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
);
