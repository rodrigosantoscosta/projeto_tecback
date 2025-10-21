# OFICINA - Sistema de Gestão Mecânica

## Especificação do Projeto (BackEnd)


## 1) Stack e objetivo
- **Stack**: Spring Boot 3.x, Spring Web (APIs REST), Spring Data JPA, Bean Validation, Lombok, PostgreSQL, REST Template (ViaCEP integration).
- **Objetivo**: API para gestão de oficina mecânica com cadastro de clientes, veículos, atendimentos e integração com ViaCEP para endereços automáticos.
- **Autenticação**: não obrigatória (pode ser Plus).

## 2) Modelo relacional (tabelas) mapeado com ORM

### 2.1 `clientes`
- `id` BIGINT **PK** AUTO_INCREMENT
- `nome` VARCHAR(100) **NOT NULL**
- `cpf` VARCHAR(11) **UNIQUE NOT NULL**
- `telefone` VARCHAR(20) **NOT NULL**
- `email` VARCHAR(100) **UNIQUE NOT NULL**
- `data_cadastro` TIMESTAMP **NOT NULL**

### 2.2 `enderecos`
- `id` BIGINT **PK** AUTO_INCREMENT
- `cep` VARCHAR(9) **NOT NULL**
- `logradouro` VARCHAR(200) **NOT NULL**
- `numero` VARCHAR(10) **NOT NULL**
- `complemento` VARCHAR(100)
- `bairro` VARCHAR(100) **NOT NULL**
- `localidade` VARCHAR(100) **NOT NULL**
- `uf` VARCHAR(2) **NOT NULL**
- `cliente_id` BIGINT **FK** → `clientes(id)` (**ON DELETE CASCADE**)

### 2.3 `veiculos`
- `id` BIGINT **PK** AUTO_INCREMENT
- `marca` VARCHAR(50) **NOT NULL**
- `modelo` VARCHAR(50) **NOT NULL**
- `ano` SMALLINT **NOT NULL**
- `placa` VARCHAR(7) **UNIQUE**
- `cliente_id` BIGINT **FK** → `clientes(id)` (**ON DELETE CASCADE**)

### 2.4 `atendimentos`
- `id` BIGINT **PK** AUTO_INCREMENT
- `descricao` TEXT **NOT NULL**
- `valor` DECIMAL(10,2) **NOT NULL**
- `status` VARCHAR(20) **NOT NULL** (`AGENDADO` | `EM_ANDAMENTO` | `CONCLUIDO` | `CANCELADO`)
- `data_entrada` TIMESTAMP **NOT NULL**
- `data_saida` TIMESTAMP
- `cliente_id` BIGINT **FK** → `clientes(id)`
- `veiculo_id` BIGINT **FK** → `veiculos(id)`

## 3) Validações
- **Nome**: obrigatório, máx. 100 caracteres
- **CPF**: obrigatório, único, 11 dígitos, com validação de dígitos verificadores
- **Telefone**: obrigatório, formato válido
- **E-mail**: obrigatório, único, formato válido
- **CEP**: obrigatório, formato válido, integração com ViaCEP
- **Placa**: formato Mercosul ou antigo
- **Ano do veículo**: entre 1900–2100
- **Valor do atendimento**: positivo

## 4) APIs REST (Spring Web)

### Clientes
- `POST /clientes/cadastro` - Cadastrar cliente com endereço automático
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

### CEP
- `GET /api/cep/{cep}` - Consultar endereço por CEP

## 5) Consultas JPQL implementadas
1. Buscar cliente por CPF exato
2. Listar clientes ordenados por nome
3. Buscar veículos por marca (case-insensitive)
4. Listar atendimentos por status
5. Buscar atendimentos de um cliente específico
6. Listar veículos de um cliente
7. Contar atendimentos por status
8. Buscar clientes por parte do nome

## 6) Integrações externas
- **ViaCEP API**: Busca automática de endereços
- **REST Template**: Configurado para consumo de APIs externas

## 7) Status do Projeto

### Implementado
- [x] Estrutura base do Spring Boot
- [x] Entidades JPA (Cliente, Endereco, Veiculo, Atendimento)
- [x] Repositories com Spring Data JPA
- [x] Services com lógica de negócio
- [x] Controllers REST
- [x] Integração com ViaCEP
- [x] Validações customizadas
- [x] Configuração PostgreSQL
- [x] Tratamento de erros

### Em Desenvolvimento
- [ ] Testes unitários
- [ ] Documentação Swagger
- [ ] Paginação nas consultas

### Próximas Implementações
- [ ] Autenticação e autorização JWT
- [ ] Upload de imagens dos veículos
- [ ] Relatórios e dashboard
- [ ] Notificações por e-mail
- [ ] Integração com sistema de pagamento

## 8) Configuração e Execução

### Pré-requisitos
- Java 17+
- PostgreSQL
- Maven 3.6+

### Configuração do Banco
```sql
CREATE DATABASE oficina;