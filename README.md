# OFICINA - Sistema de Gestão para Oficina Mecânica

## Especificação do Projeto (BackEnd)


## 1) Stack e objetivo
- **Stack**: Spring Boot 3.x, Spring Web (APIs REST), Spring Data JPA, Bean Validation, Lombok, PostgreSQL, REST Template (ViaCEP integration).
- **Objetivo**: API para gestão de oficina mecânica com cadastro de clientes, veículos, atendimentos e integração com ViaCEP para endereços automáticos.
- **Autenticação**: não obrigatória (pode ser Plus).

## 2) Modelo relacional (tabelas) mapeado com ORM

### 2.1 `clientes`
- `id` UUID **PK** 
- `nome_completo` VARCHAR(150) **NOT NULL**
- `cpf_cnpj` VARCHAR(14) **UNIQUE NOT NULL**
- `telefone` VARCHAR(20) **NOT NULL**
- `email` VARCHAR(254) **UNIQUE NOT NULL**
- `data_cadastro` TIMESTAMP **NOT NULL**
- `endereco_id` UUID **FK** → `enderecos(id)`

### 2.2 `enderecos` 
- `id` UUID **PK**
- `cep` VARCHAR(9) **NOT NULL**
- `logradouro` VARCHAR(200) **NOT NULL**
- `numero` VARCHAR(10) **NOT NULL**
- `complemento` VARCHAR(100)
- `bairro` VARCHAR(100) **NOT NULL/*
- `localidade` VARCHAR(100) **NOT NULL**
- `uf` VARCHAR(2) **NOT NULL**

### 2.3 `veiculos` 
- `id` UUID **PK** 
- `placa` VARCHAR(7) **UNIQUE NOT NULL*
- `modelo` VARCHAR(50) **NOT NULL*
- `marca` VARCHAR(50) **NOT NULL*
- `ano` SMALLINT **NOT NULL*
- `cor` VARCHAR(30)
- `quilometragem` DOUBLE
- `data_cadastro` TIMESTAMP **NOT NULL*
- `cliente_id` UUID **FK** → `clientes(id)`

### 2.4 `atendimentos` 
- `id` UUID **PK**
- `descricao` TEXT **NOT NULL*
- `status` VARCHAR(20) **NOT NULL** (`AGENDADO` | `EM_ANDAMENTO` | `CONCLUIDO` | `CANCELADO`)
- `data_entrada` TIMESTAMP **NOT NULL**
- `data_saida` TIMESTAMP
- `cliente_id` UUID **FK** → `clientes(id)**
- `veiculo_id` UUID **FK** → `veiculos(id)`

## 3) Validações
- **Nome Completo**: obrigatório, máx. 150 caracteres 
- **CPF/CNPJ**: obrigatório, único, 11 ou 14 dígitos, com validação de dígitos verificadores
- **Telefone**: obrigatório, formato válido 
- **E-mail**: obrigatório, único, formato válido, máx. 254 caracteres 
- **CEP**: obrigatório, formato válido, integração com ViaCEP 

## 4) APIs REST (Spring Web)

### Clientes
- `POST /clientes/` - Cadastrar cliente com endereço automático
- `GET /clientes` - Listar todos os clientes
- `GET /clientes/{id}` - Buscar cliente por ID
- `DELETE /clientes/{id}` - Deletar cliente
- `GET /clientes/cpf/{cpf}` - Buscar cliente por CPF

### Veículos
- `POST /veiculos` - Cadastrar veículo
- `GET /veiculos` - Listar veículos
- `GET /veiculos/cliente/{clienteId}` - Listar veículos por cliente
- `GET /veiculos/placa/{placa}` - Buscar veículo por placa

### Atendimentos
- `POST /atendimentos` - Criar atendimento
- `GET /atendimentos` - Listar atendimentos
- `GET /atendimentos/cliente/{clienteId}` - Atendimentos por cliente
- `PUT /atendimentos/{id}/status` - Atualizar status
- `PUT /atendimentos/{id}/concluir` - Concluir atendimento


## 5) Consultas JPQL implementadas

## 6) Integrações externas
- **ViaCEP API**: Busca automática de endereços
- **REST Template**: Configurado para consumo de APIs externas

### Pré-requisitos
- Java 17+
- PostgreSQL
- Maven 3.6+