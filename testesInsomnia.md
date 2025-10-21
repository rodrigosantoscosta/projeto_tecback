# Testes dos endpoints
## cadastrarCliente
### localhost:8080/clientes/cadastro
### Exemplos:
`{
"nome": "João Silva",
"cpf": "12345678901",
"telefone": "(11) 99999-9999",
"email": "joao@email.com",
"cep": "01001000",
"numero": "123",
"complemento": "Apt 45"
} `

` {
 "nome": "Ana Costa",
  "cpf": "55566677788",
  "telefone": "(31) 66666-6666",
  "email": "ana.costa@email.com",
  "cep": "30130010",
  "numero": "101",
  "complemento": "Sala 201"
}`

## listarTodosClientes

### localhost:8080/clientes

## deletarClientePorId

` locahost:8080/clientes/<id>`

## Dica:
1. Crie uma variavel no Insomnia: base_url = http://localhost:8080 <br>
2. Para referenciar use assim na URL: `{{ base_url }}/clientes/1`


