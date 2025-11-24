# Testes dos endpoints

## 1) Cliente
###  url: ```localhost:8080/clientes```

### cadastrarClienteCompleto
### Exemplos:

#### Cadastro com sucesso
```json
{
"nomeCompleto": "João Silva Santos Oliveira",
"cpfCNPJ": "123.456.789-09",
"telefone": "(11) 99999-9999",
"email": "joao.silva@email.com",
"cep": "01001000",
"numero": "123",
"complemento": "Apt 45"
}
```

```json
{
  "nomeCompleto": "Maria Santos Costa",
  "cpfCNPJ": "987.654.321-00",
  "telefone": "(21) 88888-8888",
  "email": "maria.santos@email.com",
  "cep": "22010010",
  "numero": "456"
}
```

#### Cadastro com CPF inválido 
```json
{
"nomeCompleto": "Carlos Pereira",
"cpfCNPJ": "123",
"telefone": "(11) 90000-0000",
"email": "carlos@email.com",
"cep": "12345678",
"numero": "100"
}
```

#### E-mail já cadastrado
```json
{
"nomeCompleto": "Joana Lima",
"cpfCNPJ": "321.654.987-00",
"telefone": "(31) 91111-1111",
"email": "joao.silva@email.com",
"cep": "30140071",
"numero": "22"
}
```

## listarTodosClientes
``` locahost:8080/clientes ```

## listarClientePorId

``` locahost:8080/clientes/<id> ```


## deletarClientePorId

` locahost:8080/clientes/<id>`

## Dica:
1. Crie uma variavel no Insomnia: base_url = http://localhost:8080 <br>
2. Para referenciar use assim na URL: `{{ base_url }}/clientes/1`

---

## 2) Veículo
### url: ```localhost:8080/veiculos```

### cadastrarVeiculoCompleto (POST)
**Endpoint:** `POST {{ base_url }}/veiculos`

#### Cadastro com sucesso - Veículo completo
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

#### Cadastro com sucesso - Veículo padrão antigo
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

#### Cadastro sem campos opcionais
```json
{
  "placa": "GHI5J89",
  "marca": "Chevrolet",
  "modelo": "Onix",
  "ano": 2023,
  "clienteId": "seu-uuid-de-cliente-aqui"
}
```

#### Cadastro com placa inválida (deve falhar)
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

#### Cadastro com ano inválido (deve falhar)
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

#### Cadastro com cliente inexistente (deve falhar)
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

---

### listarTodos (GET)
**Endpoint:** `GET {{ base_url }}/veiculos`

*Sem body necessário*

---

### buscarPorId (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/<uuid-do-veiculo>`

**Exemplo:** `GET {{ base_url }}/veiculos/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

---

### buscarPorPlaca (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/placa/<placa>`

**Exemplo:** `GET {{ base_url }}/veiculos/placa/ABC1D23`

*Sem body necessário*

---

### listarVeiculosPorCliente (GET)
**Endpoint:** `GET {{ base_url }}/veiculos/cliente/<uuid-do-cliente>`

**Exemplo:** `GET {{ base_url }}/veiculos/cliente/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

---

### associarVeiculoAoCliente(no momento não é necessário) (PUT)
**Endpoint:** `PUT {{ base_url }}/veiculos/<uuid-do-veiculo>/associar/<uuid-do-cliente>`

**Exemplo:** `PUT {{ base_url }}/veiculos/123e4567-e89b-12d3-a456-426614174000/associar/987e6543-e21b-12d3-a456-426614174000`

*Sem body necessário*

---

### deletarPorId (DELETE)
**Endpoint:** `DELETE {{ base_url }}/veiculos/<uuid-do-veiculo>`

**Exemplo:** `DELETE {{ base_url }}/veiculos/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

---

## Observações sobre Veículos:
- **Formato de Placa:** Aceita padrão Mercosul (ABC1D23) e antigo (ABC1234)
- **Placa:** Sempre em maiúsculas, 7 caracteres
- **Ano:** Deve estar entre 1900 e 2100
- **Quilometragem:** Opcional, não pode ser negativa
- **Cor:** Opcional, máximo 30 caracteres
- **ClienteId:** Obrigatório e deve ser um UUID válido de um cliente existente

## 3) Funcionário
### url: ```localhost:8080/funcionarios```

### Registrar Funcionário (POST)
**Endpoint:** `POST {{ base_url }}/funcionarios`

#### Exemplo válido:
```json
{
  "nome": "João Silva",
  "cpf": "12345678909",
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

#### Exemplo de erro (CPF inválido - menos de 11 dígitos):
```json
{
  "nome": "Bruno Lima",
  "cpf": "123",
  "cargo": "Atendente",
  "telefone": "21977776666",
  "email": "bruno.lima@oficina.com",
  "usuario": "brunolima",
  "senha": "OutraSenha456"
}
```

### Listar Todos os Funcionários (GET)
**Endpoint:** `GET {{ base_url }}/funcionarios`

*Sem body necessário*

### Buscar Funcionário por ID (GET)
**Endpoint:** `GET {{ base_url }}/funcionarios/<uuid-do-funcionario>`

**Exemplo:** `GET {{ base_url }}/funcionarios/123e4567-e89b-12d3-a456-426614174000`

*Sem body necessário*

### Login (POST)
**Endpoint:** `POST {{ base_url }}/funcionarios/login`

#### Exemplo de login válido:
```json
{
  "usuario": "joaosilva",
  "senha": "senha123"
}
```

#### Exemplo de login inválido:
```json
{
  "usuario": "joaosilva",
  "senha": "senhaerrada"
}
```

### Observações:
- O campo `usuario` deve ser único
- A `senha` deve ter no mínimo 6 caracteres
- O `cpf` deve ter exatamente 11 dígitos numéricos (sem formatação)
- O `telefone` deve ter 10 ou 11 dígitos numéricos (sem formatação)
- O `email` deve ser válido
- O `usuario` deve ter entre 3 e 50 caracteres
- Campos obrigatórios: nome, cpf, cargo, telefone, email, usuario, senha