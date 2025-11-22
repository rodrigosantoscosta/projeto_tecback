# OFICINA - Sistema de Gestão para Oficina Mecânica

## Especificação do Projeto (BackEnd)


## 1) Stack e objetivo
### Backend
- **Framework**: Spring Boot 3.5.6
- **Linguagem**: Java 21
- **Dependencias**:
    - Spring Web (APIs REST)
    - Spring Data JPA (ORM)
    - Spring Security (Autenticação e Autorização)
    - Spring Validation (Bean Validation)
    - Lombok 1.18.32 (Redução de boilerplate)
    - MapStruct 1.6.3 (Mapeamento de DTOs)
    - JJWT 0.11.5 (JWT Token)
    - SpringDoc OpenAPI 2.5.0 (Swagger/OpenAPI)
    - Flyway (Versionamento de banco de dados)
    - PostgreSQL (Banco de dados)
    - REST Template (Integração com APIs externas)

### Frontend
- **HTML5**: Estrutura semântica
- **CSS3**: Styling responsivo com gradientes e animações
- **JavaScript**: Funcionalidades interativas
- **Integração ViaCEP**: Busca automática de endereços

### Ferramentas
- **Build**: Maven 3.6+
- **Versionamento DB**: Flyway
- **Documentação API**: Swagger/OpenAPI (SpringDoc)
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
- `id` BIGINT **PK** (auto-increment)
- `cep` VARCHAR(9) **NOT NULL**
- `logradouro` VARCHAR(200) **NOT NULL**
- `numero` VARCHAR(10) **NOT NULL**
- `complemento` VARCHAR(100)
- `bairro` VARCHAR(100) **NOT NULL**
- `cidade` VARCHAR(100) **NOT NULL**
- `estado` VARCHAR(2) **NOT NULL**

### 2.3 `veiculos`
- `id` UUID **PK**
- `placa` VARCHAR(7) **UNIQUE NOT NULL**
- `modelo` VARCHAR(50) **NOT NULL**
- `marca` VARCHAR(50) **NOT NULL**
- `ano` SMALLINT **NOT NULL**
- `cor` VARCHAR(30)
- `quilometragem` DOUBLE
- `data_cadastro` TIMESTAMP **NOT NULL**
- `cliente_id` UUID **FK** → `clientes(id)`

### 2.4 `funcionarios`
- `id` UUID **PK**
- `nome` VARCHAR(150) **NOT NULL**
- `cpf_cnpj` VARCHAR(14) **UNIQUE NOT NULL**
- `usuario` VARCHAR(50) **UNIQUE NOT NULL**
- `senha_hash` VARCHAR(60) **NOT NULL**
- `cargo` VARCHAR(50) **NOT NULL**
- `telefone` VARCHAR(20)
- `email` VARCHAR(254)
- `data_cadastro` TIMESTAMP **NOT NULL**

### 2.5 `atendimentos`
- `id` BIGINT **PK** (auto-increment)
- `descricao_servico` TEXT
- `status` VARCHAR(20) **NOT NULL** (`AGUARDANDO` | `AGENDADO` | `EM_ANDAMENTO` | `CONCLUIDO` | `CANCELADO`)
- `data_entrada` TIMESTAMP **NOT NULL**
- `data_conclusao` TIMESTAMP
- `data_cadastro` TIMESTAMP **NOT NULL**
- `cliente_id` UUID **FK** → `clientes(id)`
- `veiculo_id` UUID **FK** → `veiculos(id)`
- `funcionario_id` UUID **FK** → `funcionarios(id)`

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
- `GET /veiculos/{id}` - Buscar veículo por ID
- `GET /veiculos/placa/{placa}` - Buscar veículo por placa
- `GET /veiculos/cliente/{clienteId}` - Listar veículos por cliente
- `GET /veiculos/total-veiculos` - Contar total de veículos
- `PUT /veiculos/{id}` - Atualizar veículo
- `PUT /veiculos/{veiculoId}/associar/{clienteId}` - Associar veículo a cliente
- `DELETE /veiculos/{id}` - Deletar veículo por ID
- `DELETE /veiculos/placa/{placa}` - Deletar veículo por placa

### Funcionários
- `POST /funcionarios` - Registrar funcionário
- `POST /funcionarios/login` - Login de funcionário
- `GET /funcionarios/me` - Obter funcionário autenticado
- `GET /funcionarios` - Listar todos os funcionários
- `GET /funcionarios/{id}` - Buscar funcionário por ID
- `DELETE /funcionarios/{id}` - Deletar funcionário

### Atendimentos
- `POST /atendimentos` - Criar atendimento
- `GET /atendimentos` - Listar atendimentos
- `GET /atendimentos/cliente/{clienteId}` - Atendimentos por cliente
- `PUT /atendimentos/{id}/status` - Atualizar status
- `PUT /atendimentos/{id}/concluir` - Concluir atendimento

### Integrações Externas
- `GET /api/viacep/endereco/{cep}` - Buscar endereço por CEP
- `GET /api/feriados/{ano}` - Listar feriados nacionais

## 5) Consultas JPQL implementadas
- Listar clientes por CPF/CNPJ
- Listar veículos por cliente
- Listar veículos por placa
- Listar atendimentos por cliente
- Listar atendimentos por status
- Listar funcionários por usuário

## 6) Integrações externas
- **ViaCEP API**: Busca automática de endereços
- **Brasil API**: Consulta de feriados nacionais
- **REST Template**: Configurado para consumo de APIs externas

### Pré-requisitos
- Java 21+
- PostgreSQL
- Maven 3.6+
