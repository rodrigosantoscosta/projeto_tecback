# Testes dos Endpoints - Guia Completo

## Configuração Inicial

### Variáveis do Insomnia
1. Crie uma variável no Insomnia: `base_url = http://localhost:8080`
2. Para referenciar use assim na URL: `{{ base_url }}/clientes/1`

### Nota Importante sobre Segurança
- **Segurança Desabilitada**: `app.security.enabled=false` (padrão para testes)
- **Segurança Ativada**: `app.security.enabled=true` (produção)
- Quando segurança está ativada, endpoints requerem JWT token no header: `Authorization: Bearer <token>`

---

## 1) FUNCIONÁRIOS
### Base URL: ```{{ base_url }}/funcionarios```
### Usuario admin
{
"usuario": "admin",
"senha": "adminadmin"
}

### 1.1 Registrar Funcionário (POST)
**Endpoint:** `POST {{ base_url }}/funcionarios`

#### Exemplo válido:
```json
{
  "nome": "João Silva",
  "cpfCNPJ": "12345678909", 
  "cargo": "Mecânico",
  "telefone": "11999999999",
  "email": "joao@email.com",
  "usuario": "joaosilva",
  "senha": "senha123"
}
```

#### Exemplo de erro (CPF duplicado):
```json
{
  "nome": "Maria Santos",
  "cpfCNPJ": "12345678909",
  "cargo": "Atendente",
  "telefone": "21888888888",
  "email": "maria@email.com",
  "usuario": "marias",
  "senha": "outrasenha"
}
```

#### Exemplo de erro (CPF inválido):
```json
{
  "nome": "Bruno Lima",
  "cpfCNPJ": "123",
  "cargo": "Atendente",
  "telefone": "21977776666",
  "email": "bruno.lima@oficina.com",
  "usuario": "brunolima",
  "senha": "OutraSenha456"
}
```

### 1.2 Login (POST)
**Endpoint:** `POST {{ base_url }}/funcionarios/login`

#### Exemplo de login válido:
```json
{
  "usuario": "joaosilva",
  "senha": "senha123"
}
```

**Resposta esperada:**
```json
{
  "type": "Bearer",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Exemplo de login inválido:
```json
{
  "usuario": "joaosilva",
  "senha": "senhaerrada"
}
```

### 1.3 Obter Funcionário Autenticado (GET)
**Endpoint:** `GET {{ base_url }}/funcionarios/me`

**Headers:**
```
Authorization: Bearer <token_do_login>
```

*Sem body necessário*

### 1.4 Listar Todos os Funcionários (GET)
**Endpoint:** `GET {{ base_url }}/funcionarios`

*Sem body necessário*

### 1.5 Buscar Funcionário por ID (GET)
**Endpoint:** `GET {{ base_url }}/funcionarios/<uuid-do-funcionario>`

**Exemplo:** `GET {{ base_url }}/funcionarios/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### 1.6 Deletar Funcionário (DELETE)
**Endpoint:** `DELETE {{ base_url }}/funcionarios/<uuid-do-funcionario>`

**Exemplo:** `DELETE {{ base_url }}/funcionarios/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### Observações sobre Funcionários:
- O campo `usuario` deve ser único
- A `senha` deve ter no mínimo 6 caracteres
- O `cpfCNPJ` deve ter exatamente 11 dígitos numéricos (sem formatação) // alterar
- O `telefone` deve ter 10 ou 11 dígitos numéricos (sem formatação)
- O `email` deve ser válido
- O `usuario` deve ter entre 3 e 50 caracteres
- Campos obrigatórios: nome, cpf, cargo, telefone, email, usuario, senha

---

## 2) CLIENTES
### Base URL: ```{{ base_url }}/clientes```

### 2.1 Cadastrar Cliente (POST)
**Endpoint:** `POST {{ base_url }}/clientes`

#### Cadastro com sucesso - Completo:
```json
{
  "nomeCompleto": "João Silva Santos Oliveira",
  "cpfCNPJ": "12345678909",
  "telefone": "11999999999",
  "email": "joao.silva@email.com",
  "cep": "01001000",
  "numero": "123",
  "complemento": "Apt 45"
}
```

#### Cadastro com sucesso - Mínimo:
```json
{
  "nomeCompleto": "Maria Santos Costa",
  "cpfCNPJ": "98765432100",
  "telefone": "21988888888",
  "email": "maria.santos@email.com",
  "cep": "22010010",
  "numero": "456"
}
```

#### Cadastro com CPF inválido (deve falhar):
```json
{
  "nomeCompleto": "Carlos Pereira",
  "cpfCNPJ": "123",
  "telefone": "11900000000",
  "email": "carlos@email.com",
  "cep": "12345678",
  "numero": "100"
}
```

#### E-mail já cadastrado (deve falhar):
```json
{
  "nomeCompleto": "Joana Lima",
  "cpfCNPJ": "32165498700",
  "telefone": "31911111111",
  "email": "joao.silva@email.com",
  "cep": "30140071",
  "numero": "22"
}
```

### 2.2 Listar Todos os Clientes (GET)
**Endpoint:** `GET {{ base_url }}/clientes`

*Sem body necessário*

### 2.3 Buscar Cliente por ID (GET)
**Endpoint:** `GET {{ base_url }}/clientes/<uuid-do-cliente>`

**Exemplo:** `GET {{ base_url }}/clientes/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### 2.4 Buscar Cliente por CPF/CNPJ (GET)
**Endpoint:** `GET {{ base_url }}/clientes/cpfCNPJ/<cpf-ou-cnpj>`

**Exemplo:** `GET {{ base_url }}/clientes/cpfCNPJ/12345678909`

*Sem body necessário*

### 2.5 Listar Clientes (GET)
**Endpoint:** `GET {{ base_url }}/clientes`

**Resposta de exemplo:**
```json
[
    {
        "id": "ae46ad37-4f04-46a7-aaf7-91345c4a8e7e",
        "nomeCompleto": "João Silva Santos Oliveira",
        "cpfCNPJ": "12345678909",
        "telefone": "(11) 99999-9999",
        "email": "joao.silva@email.com",
        "endereco": {
            "id": 4,
            "cep": "01001-000",
            "logradouro": "Praça da Sé",
            "numero": "123",
            "complemento": "Apt 45",
            "bairro": "Sé",
            "localidade": "São Paulo",
            "uf": "SP"
        },
        "dataCadastro": "2025-10-23T15:55:37.916894",
        "quantidadeVeiculos": 1
    }
]
```


### 2.6 Atualizar Cliente (PUT)
**Endpoint:** `PUT {{ base_url }}/clientes/{id}`

**Exemplo de atualização de cliente:**
```json
{
  "nomeCompleto": "João Silva Santos Oliveira Atualizado",
  "cpfCNPJ": "12345678909",
  "telefone": "11999998888",
  "email": "joao.novo@email.com",
  "cep": "01001000",
  "numero": "123A",
  "complemento": "Sala 101"
}
```

**Resposta de sucesso:**
```
Cliente atualizado com sucesso
```

**Observações:**
- Atualiza tanto os dados básicos quanto o endereço do cliente
- Se o CEP for alterado, o sistema busca automaticamente os novos dados de endereço
- Mantém o histórico do cadastro original (dataCadastro não é alterada)
- Não é possível alterar o ID do cliente

### 2.7 Deletar Cliente (DELETE)
**Endpoint:** `DELETE {{ base_url }}/clientes/<uuid-do-cliente>`

**Exemplo:** `DELETE {{ base_url }}/clientes/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### Observações sobre Clientes:
- **CPF/CNPJ**: Deve ter 11 dígitos para CPF ou 14 para CNPJ (sem formatação)
- **Email**: Deve ser único e válido
- **Telefone**: Deve ter 10 ou 11 dígitos (sem formatação)
- **CEP**: Deve ter 8 dígitos (sem formatação)
- Campos obrigatórios: nomeCompleto, cpfCNPJ, telefone, email, cep, numero

---

## 3) VEÍCULOS
### Base URL: ```{{ base_url }}/veiculos```

### 3.1 Cadastrar Veículo (POST)
**Endpoint:** `POST {{ base_url }}/veiculos`

#### Cadastro com sucesso - Veículo completo:
```json
{
  "placa": "ABC1D23",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2020,
  "cor": "Prata",
  "quilometragem": 45000.5,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```

#### Cadastro com sucesso - Veículo padrão antigo:
```json
{
  "placa": "DEF4567",
  "marca": "Fiat",
  "modelo": "Palio",
  "ano": 2015,
  "cor": "Branco",
  "quilometragem": 120000,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```

#### Cadastro sem campos opcionais:
```json
{
  "placa": "GHI5J89",
  "marca": "Chevrolet",
  "modelo": "Onix",
  "ano": 2023,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```

#### Cadastro com placa inválida (deve falhar):
```json
{
  "placa": "ABC123",
  "marca": "Honda",
  "modelo": "Civic",
  "ano": 2022,
  "cor": "Preto",
  "quilometragem": 15000,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```

#### Cadastro com ano inválido (deve falhar):
```json
{
  "placa": "JKL6M78",
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 1800,
  "cor": "Azul",
  "quilometragem": 50000,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```

#### Cadastro com cliente inexistente (deve falhar):
```json
{
  "placa": "MNO7P89",
  "marca": "Hyundai",
  "modelo": "HB20",
  "ano": 2021,
  "cor": "Vermelho",
  "quilometragem": 30000,
  "clienteId": "00000000-0000-0000-0000-000000000000"
}
```

### 3.2 Listar Todos os Veículos (GET)
**Endpoint:** `GET {{ base_url }}/veiculos`

*Sem body necessário*

### 3.3 Buscar Veículo por ID (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/<uuid-do-veiculo>`

**Exemplo:** `GET {{ base_url }}/veiculos/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### 3.4 Buscar Veículo por Placa (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/placa/<placa>`

**Exemplo:** `GET {{ base_url }}/veiculos/placa/ABC1D23`

*Sem body necessário*

### 3.5 Listar Veículos por Cliente (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/cliente/<uuid-do-cliente>`

**Exemplo:** `GET {{ base_url }}/veiculos/cliente/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### 3.6 Contar Total de Veículos (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/total-veiculos`

*Sem body necessário*

### 3.7 Atualizar Veículo (PUT)
**Endpoint:** `PUT {{ base_url }}/veiculos/<uuid-do-veiculo>`

**Exemplo:** `PUT {{ base_url }}/veiculos/123e4567-e89b-12d3-a456-426614174000`

```json
{
  "placa": "XYZ9999",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2021,
  "cor": "Preto",
  "quilometragem": 50000,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```


### 3.8 Deletar Veículo por ID (DELETE)
**Endpoint:** `DELETE {{ base_url }}/veiculos/<uuid-do-veiculo>`

**Exemplo:** `DELETE {{ base_url }}/veiculos/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### 3.9 Deletar Veículo por Placa (DELETE)
**Endpoint:** `DELETE {{ base_url }}/veiculos/placa/<placa>`

**Exemplo:** `DELETE {{ base_url }}/veiculos/placa/ABC1D23`

*Sem body necessário*

### Observações sobre Veículos:
- **Formato de Placa:** Aceita padrão Mercosul (ABC1D23) e antigo (ABC1234)
- **Placa:** Sempre em maiúsculas, 7 caracteres
- **Ano:** Deve estar entre 1900 e 2100
- **Quilometragem:** Opcional, não pode ser negativa
- **Cor:** Opcional, máximo 30 caracteres
- **ClienteId:** Obrigatório e deve ser um UUID válido de um cliente existente
- Campos obrigatórios: placa, marca, modelo, ano, clienteId

---

 ## 4) ATENDIMENTOS
### Base URL: ```{{ base_url }}/atendimentos```

### 4.1 Cadastrar Atendimento (POST)
**Endpoint:** `POST {{ base_url }}/atendimentos`

#### Cadastro com sucesso - Atendimento completo:
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "DEF4567",
  "funcionarioId": "abd95873-9445-43b3-a4ed-4a73c00c4dcc"
}
```

#### Cadastro com Id do cliente inválido (deve falhar):
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "123e4567-e89b-12d3-a456-426614174000",
  "veiculoPlaca": "DEF4567",
  "funcionarioId": "abd95873-9445-43b3-a4ed-4a73c00c4dcc"
}
```

#### Cadastro com Id do cliente inexistente (deve falhar):
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "123e4567-e89b-12d3-a456-426614174000",
  "veiculoPlaca": "DEF4567",
  "funcionarioId": "abd95873-9445-43b3-a4ed-4a73c00c4dcc"
}
```

#### Cadastro com Placa do Veículo inválido (deve falhar):
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "1111111111111",
  "funcionarioId": "453e4532-e84b-125-a456-4000"
}
```

#### Cadastro com Placa do Veículo inexistente (deve falhar):
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "FBA5D27",
  "funcionarioId": "453e4532-e84b-125-a456-4000"
}
```

#### Cadastro com Id do Funcionário inválido (deve falhar):
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "FBA5D27",
  "funcionarioId": "111111111111111111111111"
}
```

#### Cadastro com Id do Funcionário inexistente (deve falhar):
```json
{
  "descricaoServico": "Troca de óleo e filtro",
  "statusAtendimento": "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "FBA5D27",
  "funcionarioId": "453e4532-e84b-125-a456-4000"
}
```

### 4.2 Listar Todos os Atendimentos (GET)
**Endpoint:** `GET {{ base_url }}/atendimentos/listar-todos`

*Sem body necessário*

### 4.3 Buscar Atendimento por ID (GET)
**Endpoint:** `GET {{ base_url }}/atendimentos/id/<uuid-do-atendimento>`

**Exemplo:** `GET {{ base_url }}/atendimentos/id/26e72375-156c-401a-9f55-58879513f0a0`

*Sem body necessário*

### 4.4 Buscar Atendimento por ID do Cliente (GET)
**Endpoint:** `GET {{ base_url }}/atendimentos/cliente ID/<uuid-do-cliente>`

**Exemplo:** `GET {{ base_url }}/atendimentos/cliente ID/519cb250-4a72-4ddf-8c25-30d7e058bdef`

*Sem body necessário*

### 4.5 Listar Atendimentos com status "CONCLUIDO" (GET)
**Endpoint:** `GET {{ base_url }}atendimentos/listar-concluidos`


*Sem body necessário*

### 4.6 Listar Atendimentos por Data de Entrada em ordem decrescente  (GET)
**Endpoint:** `GET {{ base_url }}/atendimentos/listar-ordem-decrescente`

*Sem body necessário*

### 4.7 Atualizar Atendimento (PUT)
**Endpoint:** `PUT {{ base_url }}/atendimentos/atualizar/<uuid-do-atendimento>`

**Exemplo:** `PUT {{ base_url }}/atendimentos/atualizar/5be82b05-7c2e-47f2-8af8-0b6a123dd771`

```json
{
  "descricaoServico": "Troca de mangueira e pedal da embrenhagem",
  "statusAtendimento": "ANDAMENTO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "DEF4567",
  "funcionarioId": "abd95873-9445-43b3-a4ed-4a73c00c4dcc"
}
```

### 4.8 Deletar Atendimento por ID (DELETE)
**Endpoint:** `DELETE {{ base_url }}/atendimento/delete/<uuid-do-atendimento>`

**Exemplo:** `DELETE {{ base_url }}/atendimentos/delete/2c1e7b71-d611-4185-a607-8f53daa3cadf`

*Sem body necessário*

### Observações sobre Atendimentos:
- **Status do Atendimento:** Aceita apenas as palavras Aguardando, Andamento, Concluido e Cancelado
- **Datas Cadastro e Entradas:** Eles são gerados no momento da realização do cadastro 
- **Data de Conclusão:** Quando os status de atendimento estiver como concluido ou cancelado, será registrado há hora em que foi inserido
- **ClienteId:** Obrigatório e deve ser um UUID válido de um cliente existente
- **VeiculoPlaca:** Obrigatório e deve ser uma Placa válida de um Veículo existente
- **FuncionarioId:** Obrigatório e deve ser um UUID válido de um Funcionário existente
- Campos obrigatórios: descriçãoServiço, clienteId, veiculoPlaca, funcionarioId

---

## 5) INTEGRAÇÃO COM APIs EXTERNAS

### 5.1 ViaCEP - Buscar Endereço por CEP (GET)
**Endpoint:** `GET {{ base_url }}/api/viacep/endereco/<cep>`

**Exemplo:** `GET {{ base_url }}/api/viacep/endereco/01001000`

**Resposta esperada:**
```json
{
  "logradouro": "Praça da Sé",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP"
}
```

#### CEP inválido (deve retornar 404):
`GET {{ base_url }}/api/viacep/endereco/00000000`

### 5.2 Brasil API - Listar Feriados Nacionais (GET)
**Endpoint:** `GET {{ base_url }}/api/feriados/<ano>`

**Exemplo:** `GET {{ base_url }}/api/feriados/2024`

**Resposta esperada:**
```json
[
  {
    "name": "Ano Novo",
    "date": "2024-01-01",
    "type": "national"
  },
  {
    "name": "Tiradentes",
    "date": "2024-04-21",
    "type": "national"
  }
  
]
```

---

## 6) FLUXO DE TESTE RECOMENDADO

### Passo 1: Criar um Funcionário (POST)
**Endpoint:** `POST {{ base_url }}/funcionarios`

**Body:**
```json
{
  "nome": "João Silva",
  "cpfCNPJ": "12345678909",
  "cargo": "Mecânico",
  "telefone": "11999999999",
  "email": "joao@email.com",
  "usuario": "joaosilva",
  "senha": "senha123"
}
```

**Salve:** `funcionario_id` e `usuario` da resposta

---

### Passo 2: Fazer Login (POST)
**Endpoint:** `POST {{ base_url }}/funcionarios/login`

**Body:**
```json
{
  "usuario": "joaosilva",
  "senha": "senha123"
}
```

**Salve:** `token` da resposta (campo `token`)

**Adicione em Headers para próximas requisições:**
```
Authorization: Bearer <token>
```

---

### Passo 3: Criar um Cliente (POST)
**Endpoint:** `POST {{ base_url }}/clientes`

**Body:**
```json
{
  "nomeCompleto": "Maria Santos Costa",
  "cpfCNPJ": "98765432100",
  "telefone": "21988888888",
  "email": "maria.santos@email.com",
  "cep": "22010010",
  "numero": "456"
}
```

**Salve:** `cliente_id` da resposta

---

### Passo 4: Criar um Veículo (POST)
**Endpoint:** `POST {{ base_url }}/veiculos`

**Body:**
```json
{
  "placa": "ABC1D23",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2020,
  "cor": "Prata",
  "quilometragem": 45000.5,
  "clienteId": "<cliente_id_do_passo_3>"
}
```

**Salve:** `veiculo_id` da resposta

---

### Passo 5: Criar um Atendimento (POST)
**Endpoint:** `POST {{ base_url }}/atendimentos`

**Body:**
```json
{
  "descricaoServico": "Troca do espelho do retrovisor",
  "statusAtendimento": "AGUARDANDO",
  "clienteId": "519cb250-4a72-4ddf-8c25-30d7e058bdef",
  "veiculoPlaca": "DEF4567",
  "funcionarioId": "abd95873-9445-43b3-a4ed-4a73c00c4dcc"
}
```

**Salve:** `atendimento_id` da resposta

---



### Passo 6: Listar Todos os Recursos (GET)
**Endpoints:**
```
GET {{ base_url }}/clientes
GET {{ base_url }}/veiculos
GET {{ base_url }}/funcionarios
GET {{ base_url }}/atendimentos
```

*Sem body necessário*

---

### Passo 7: Buscar Recursos por ID (GET)
**Endpoints:**
```
GET {{ base_url }}/clientes/<cliente_id>
GET {{ base_url }}/veiculos/<veiculo_id>
GET {{ base_url }}/funcionarios/<funcionario_id>
GET {{ base_url }}/funcionarios/<atendimento_id>
```

*Sem body necessário*

---

### Passo 8: Buscar Cliente por CPF (GET)
**Endpoint:** `GET {{ base_url }}/clientes/cpfCNPJ/98765432100`

*Sem body necessário*

---

### Passo 9: Buscar Veículo por Placa (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/placa/ABC1D23`

*Sem body necessário*

---

### Passo 10: Buscar Atendimento por ClienteId (GET)
**Endpoint:** `GET {{ base_url }}/atendimentos/cliente id/519cb250-4a72-4ddf-8c25-30d7e058bdef`

*Sem body necessário*

---

### Passo 11: Listar Veículos por Cliente (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/cliente/<cliente_id>`

*Sem body necessário*

---

### Passo 12: Listar Atendimento por Status CONCLUIDO (GET)
**Endpoint:** `GET {{ base_url }}/atendimentos/listar-concluidos>`

*Sem body necessário*

---

### Passo 13: Atualizar Veículo (PUT)
**Endpoint:** `PUT {{ base_url }}/veiculos/<veiculo_id>`

**Body:**
```json
{
  "placa": "XYZ9999",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2021,
  "cor": "Preto",
  "quilometragem": 50000,
  "clienteId": "<cliente_id>"
}
```

---

### Passo 14: Associar Veículo a Cliente (PUT)
**Endpoint:** `PUT {{ base_url }}/veiculos/<veiculo_id>/associar/<novo_cliente_id>`

*Sem body necessário*

---

### Passo 15: Buscar Endereço por CEP (GET)
**Endpoint:** `GET {{ base_url }}/api/viacep/endereco/22010010`

*Sem body necessário*

---

### Passo 16: Listar Feriados (GET)
**Endpoint:** `GET {{ base_url }}/api/feriados/2024`

*Sem body necessário*

---

### Passo 17: Deletar Recursos (DELETE)
**Endpoints:**
```
DELETE {{ base_url }}/veiculos/<veiculo_id>
DELETE {{ base_url }}/clientes/<cliente_id>
DELETE {{ base_url }}/funcionarios/<funcionario_id>
DELETE {{ base_url }}/atendimentos/<atendimento_id>
```

*Sem body necessário*

---

## 7) DICAS IMPORTANTES

### Usando Variáveis no Insomnia
1. **Criar variável de ambiente:**
   - Clique em "Manage Environments"
   - Crie um novo ambiente
   - Adicione variáveis como:
     ```
     base_url: http://localhost:8080
     cliente_id: <id-do-cliente>
     veiculo_id: <id-do-veiculo>
     funcionario_id: <id-do-funcionario>
     token: <seu-token-jwt>
     ```

2. **Usar em requisições:**
   - URL: `{{ base_url }}/clientes/{{ cliente_id }}`
   - Header: `Authorization: Bearer {{ token }}`

### Testando com Segurança Ativada
Se `app.security.enabled=true`:
1. Faça login em `/funcionarios/login`
2. Copie o token da resposta
3. Em cada requisição, adicione o header:
   ```
   Authorization: Bearer <token>
   ```

### Testando com Segurança Desabilitada
Se `app.security.enabled=false`:
- Nenhum token é necessário
- Todos os endpoints estão acessíveis sem autenticação

### Validações Comuns
- **400 Bad Request:** Dados inválidos ou incompletos
- **401 Unauthorized:** Token ausente ou inválido (quando segurança ativada)
- **403 Forbidden:** Acesso negado
- **404 Not Found:** Recurso não encontrado
- **409 Conflict:** Conflito (ex: email/CPF duplicado)
- **500 Internal Server Error:** Erro no servidor

---

## 8) SWAGGER UI

Acesse a documentação interativa em:
```
http://localhost:8080/swagger-ui.html
```


---

