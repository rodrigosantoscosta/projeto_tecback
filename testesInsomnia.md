# Testes dos endpoints

# Cliente
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


