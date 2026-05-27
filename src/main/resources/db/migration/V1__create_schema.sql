-- enderecos
-- campos: localidade (cidade), uf (estado) - nomes do campo Java sem @Column com nome diferente
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
-- campo dataCadastro: sem @Column(name=), Hibernate usa nome do campo literal
CREATE TABLE IF NOT EXISTS clientes (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf_cnpj      VARCHAR(14)  NOT NULL UNIQUE,
    telefone      VARCHAR(20)  NOT NULL,
    email         VARCHAR(254) NOT NULL UNIQUE,
    dataCadastro  TIMESTAMP,
    endereco_id   BIGINT,
    CONSTRAINT fk_cliente_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id)
);

-- funcionarios
CREATE TABLE IF NOT EXISTS funcionarios (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    cpf_cnpj      VARCHAR(14)  NOT NULL UNIQUE,
    usuario       VARCHAR(50)  NOT NULL UNIQUE,
    senha_hash    VARCHAR(60)  NOT NULL,
    cargo         VARCHAR(50)  NOT NULL,
    telefone      VARCHAR(20),
    email         VARCHAR(254),
    dataCadastro  TIMESTAMP
);

-- veiculos
-- campo dataCadastro: sem @Column(name=), Hibernate usa nome do campo literal
CREATE TABLE IF NOT EXISTS veiculos (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    placa         VARCHAR(7)   NOT NULL UNIQUE,
    modelo        VARCHAR(50)  NOT NULL,
    marca         VARCHAR(50)  NOT NULL,
    ano           INTEGER      NOT NULL,
    cor           VARCHAR(30),
    quilometragem DOUBLE,
    dataCadastro  TIMESTAMP    NOT NULL,
    cliente_id    UUID         NOT NULL,
    CONSTRAINT fk_veiculo_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- atendimentos
-- campos sem @Column(name=): descricaoServico, dataEntrada, dataConclusao, dataCadastro
CREATE TABLE IF NOT EXISTS atendimentos (
    id                UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    descricaoServico  CLOB,
    status            VARCHAR(20) NOT NULL,
    dataEntrada       TIMESTAMP,
    dataConclusao     TIMESTAMP,
    dataCadastro      TIMESTAMP,
    cliente_id        UUID        NOT NULL,
    veiculo_id        UUID        NOT NULL,
    funcionario_id    UUID        NOT NULL,
    CONSTRAINT fk_atendimento_cliente     FOREIGN KEY (cliente_id)     REFERENCES clientes(id),
    CONSTRAINT fk_atendimento_veiculo     FOREIGN KEY (veiculo_id)     REFERENCES veiculos(id),
    CONSTRAINT fk_atendimento_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
);
