# OFICINA - Sistema de Gestão para Oficina Mecânica

Sistema completo de CRM para oficina mecânica com backend Spring Boot (Java 21), frontend React + TypeScript e PostgreSQL. Gerencia clientes, veículos, atendimentos e funcionários com autenticação JWT, refresh token rotation, testes unitários e E2E, e deploy via Docker Compose.

## Grupo

- Alexander Augusto de Figueiredo Baxendale
- Pedro Neto Amâncio de Lima
- Rodrigo Santos Costa

## Sumário

- [Stack](#stack)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Modelo relacional](#modelo-relacional)
- [Endpoints REST](#endpoints-rest)
- [Como executar](#como-executar)
- [Testes](#testes)
- [Regras de negócio e casos de borda](#regras-de-negócio-e-casos-de-borda)
- [Testando a API com Postman](#testando-a-api-com-postman)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Segurança](#segurança)

## Stack

### Backend

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 |
| Spring Web, Data JPA, Security, Validation | - |
| PostgreSQL | 16 (produção) |
| Flyway | Migration de banco |
| Lombok | 1.18.32 |
| MapStruct | 1.6.3 |
| JJWT | 0.11.5 |
| SpringDoc OpenAPI | 2.5.0 |
| Maven | 3.9.6 (Wrapper incluso) |

### Frontend

| Tecnologia | Versão |
|---|---|
| React | 19 |
| TypeScript | 6.0 |
| Vite | 8.0 |
| Tailwind CSS | 4.3 |
| React Router | 7.16 |
| Zustand | 5.0 |
| React Query (TanStack) | 5.100 |
| React Hook Form + Zod | 7.77 / 4.4 |
| Axios | 1.16 |
| Lucide React | 1.17 |
| Nginx (proxy reverso em Docker) | alpine |

### Ferramentas

- **Docker** + Docker Compose (todos os serviços)
- **Swagger/OpenAPI** em `/swagger-ui/index.html`
- **Postman** collection na raiz (`oficina_postman_collection.json`)

### Arquitetura de rede (Docker)

```
Navegador → localhost:3000 (Nginx)
  ├── /api/* → proxy reverso → backend :8080
  └── /*     → arquivos estáticos (SPA React)
```

Em ambiente Docker o frontend nunca chama o backend diretamente. O Nginx do container `crm-oficina` escuta na porta 80 (mapeada para `3000` no host) e faz proxy reverso de requisições `/api/` para `oficina-app:8080`, eliminando problemas de CORS.

## Estrutura do projeto

```
projeto_tecback/
├── crm-oficina/                        # Frontend React
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── store/
│   │   ├── types/
│   │   └── utils/
│   ├── Dockerfile
│   ├── nginx.conf                      # Proxy reverso (/api/ → backend)
│   ├── .dockerignore
│   ├── .env                            # Apenas dev local (VITE_API_URL)
│   └── package.json
├── src/                                # Backend Spring Boot
│   ├── main/java/br/com/oficina/oficina/
│   │   ├── config/                     # Swagger, CORS, RestTemplate
│   │   ├── controller/                 # REST endpoints
│   │   ├── dto/                        # Data Transfer Objects
│   │   ├── exception/                  # Tratamento de erros
│   │   ├── mapper/                     # MapStruct
│   │   ├── model/                      # Entidades JPA
│   │   ├── repository/                 # Spring Data JPA
│   │   ├── security/                   # JWT, SecurityConfig
│   │   ├── service/                    # Lógica de negócio
│   │   └── validator/                  # CPF/CNPJ
│   └── test/java/br/com/oficina/oficina/
│       ├── controller/                 # Testes unitários
│       ├── service/
│       ├── security/
│       ├── e2e/                        # Testes E2E (Docker)
│       └── config/                     # Stubs de teste
├── docker-compose.yml
├── Dockerfile                          # Backend (multi-stage)
├── pom.xml
└── src/main/resources/
│   ├── application.properties          # Produção (PostgreSQL + Flyway)
│   ├── application-docker.properties   # Profile Docker
│   └── application-railway.properties  # Profile Railway
└── src/test/resources/
    ├── application-test.properties     # Profile test
    └── application-e2e.properties      # Profile E2E (PostgreSQL real)
```

## Modelo relacional

### `clientes`

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| nome_completo | VARCHAR(150) | NOT NULL |
| cpf_cnpj | VARCHAR(14) | UNIQUE NOT NULL |
| telefone | VARCHAR(20) | NOT NULL |
| email | VARCHAR(254) | UNIQUE NOT NULL |
| data_cadastro | TIMESTAMP | NOT NULL |
| endereco_id | UUID | FK → enderecos(id) |

### `enderecos`

| Coluna | Tipo | Restrições |
|---|---|---|
| id | BIGINT | PK (auto-increment) |
| cep | VARCHAR(9) | NOT NULL |
| logradouro | VARCHAR(200) | NOT NULL |
| numero | VARCHAR(10) | NOT NULL |
| complemento | VARCHAR(100) | |
| bairro | VARCHAR(100) | NOT NULL |
| localidade | VARCHAR(100) | NOT NULL |
| uf | VARCHAR(2) | NOT NULL |

### `veiculos`

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| placa | VARCHAR(7) | UNIQUE NOT NULL |
| modelo | VARCHAR(50) | NOT NULL |
| marca | VARCHAR(50) | NOT NULL |
| ano | SMALLINT | NOT NULL |
| cor | VARCHAR(30) | |
| quilometragem | DOUBLE | |
| data_cadastro | TIMESTAMP | NOT NULL |
| cliente_id | UUID | FK → clientes(id) |

### `funcionarios`

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| nome | VARCHAR(150) | NOT NULL |
| cpf_cnpj | VARCHAR(14) | UNIQUE NOT NULL |
| usuario | VARCHAR(50) | UNIQUE NOT NULL |
| senha_hash | VARCHAR(60) | NOT NULL |
| cargo | VARCHAR(50) | NOT NULL |
| telefone | VARCHAR(20) | |
| email | VARCHAR(254) | |
| data_cadastro | TIMESTAMP | NOT NULL |

### `atendimentos`

| Coluna | Tipo | Restrições |
|---|---|---|
| id | BIGINT | PK (auto-increment) |
| descricao_servico | TEXT | |
| status | VARCHAR(20) | NOT NULL (AGUARDANDO, AGENDADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO) |
| data_entrada | TIMESTAMP | NOT NULL |
| data_conclusao | TIMESTAMP | |
| data_cadastro | TIMESTAMP | NOT NULL |
| cliente_id | UUID | FK → clientes(id) |
| veiculo_id | UUID | FK → veiculos(id) |
| funcionario_id | UUID | FK → funcionarios(id) |

### `refresh_tokens`

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| token | VARCHAR(255) | UNIQUE NOT NULL |
| revogado | BOOLEAN | NOT NULL |
| criado_em | TIMESTAMP | NOT NULL |
| expira_em | TIMESTAMP | NOT NULL |
| funcionario_id | UUID | FK → funcionarios(id) |

## Endpoints REST

### Autenticação (públicos)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/login` | Login — retorna accessToken + refreshToken |
| POST | `/auth/refresh` | Renova accessToken usando refreshToken |
| POST | `/auth/logout` | Revoga refreshToken |

### Funcionários

| Método | Rota | Descrição |
|---|---|---|
| POST | `/funcionarios` | Cadastrar (público) |
| GET | `/funcionarios` | Listar todos |
| GET | `/funcionarios/{id}` | Buscar por ID |
| GET | `/funcionarios/me` | Dados do funcionário logado |
| PUT | `/funcionarios/{id}` | Atualizar |
| DELETE | `/funcionarios/{id}` | Deletar |

### Clientes

| Método | Rota | Descrição |
|---|---|---|
| POST | `/clientes` | Cadastrar (com endereço via CEP) |
| GET | `/clientes` | Listar todos |
| GET | `/clientes/{id}` | Buscar por ID |
| GET | `/clientes/cpfCNPJ/{cpfCnpj}` | Buscar por CPF/CNPJ |
| PUT | `/clientes/{id}` | Atualizar |
| DELETE | `/clientes/{id}` | Deletar |

### Veículos

| Método | Rota | Descrição |
|---|---|---|
| POST | `/veiculos` | Cadastrar |
| GET | `/veiculos` | Listar todos |
| GET | `/veiculos/{id}` | Buscar por ID |
| GET | `/veiculos/placa/{placa}` | Buscar por placa |
| GET | `/veiculos/cliente/{clienteId}` | Listar por cliente |
| GET | `/veiculos/total-veiculos` | Contar total |
| PUT | `/veiculos/{id}` | Atualizar |
| DELETE | `/veiculos/{id}` | Deletar por ID |
| DELETE | `/veiculos/placa/{placa}` | Deletar por placa |

### Atendimentos

| Método | Rota | Descrição |
|---|---|---|
| POST | `/atendimentos/cadastrar` | Cadastrar |
| GET | `/atendimentos/listar-todos` | Listar todos |
| GET | `/atendimentos/id/{id}` | Buscar por ID |
| GET | `/atendimentos/cliente ID/{clienteId}` | Listar por cliente |
| GET | `/atendimentos/listar-ordem-decrescente` | Listar ordem decrescente |
| GET | `/atendimentos/listar-concluidos` | Listar concluídos |
| PUT | `/atendimentos/atualizar/{id}` | Atualizar |
| DELETE | `/atendimentos/delete/{id}` | Deletar |

### Integrações externas

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/viacep/endereco/{cep}` | Buscar endereço por CEP |
| GET | `/api/feriados/{ano}` | Listar feriados nacionais |

## Como executar

### Pré-requisitos

- [Docker](https://www.docker.com/) + Docker Compose
- Git
- Node.js 22+ (apenas para desenvolvimento do frontend)

### Subir todos os serviços (recomendado)

```bash
docker compose up -d
```

Aguardar todos os containers ficarem saudáveis. Serviços disponíveis:

| Serviço | URL | Observação |
|---|---|---|
| Frontend (React + Nginx) | `http://localhost:3000` | Servidor Nginx com proxy reverso |
| API (direta) | `http://localhost:8080` | Acessível também via `/api/*` no frontend |
| Swagger | `http://localhost:8080/swagger-ui/index.html` | |
| PostgreSQL | `localhost:5432` | |

### Parar tudo

```bash
docker compose down
```

Para remover também o volume do banco:

```bash
docker compose down -v
```

### Desenvolvimento local (sem Docker)

**Backend:**

```bash
# Requer Java 21 + Maven 3.6+
./mvnw spring-boot:run -Dspring.profiles.active=docker
```

**Frontend:**

```bash
cd crm-oficina
npm install
npm run dev
```

O frontend em dev roda em `http://localhost:5173` com Vite.

## Testes

O projeto possui **testes automatizados em múltiplas camadas**: unitários, integração, segurança, concorrência e E2E. Todos são executados via Docker, sem necessidade de JDK local.

> **Atenção:** existem dois perfis de teste independentes:
> - **`test`** — roda no container `oficina-tests` contra o banco `postgres` (usado para integração, segurança, concorrência, performance e E2E de exceções)
> - **`e2e`** — roda no container `oficina-e2e-tests` contra o banco `postgres-e2e` (usado para E2E de CRUD)

### Executar todos os testes (perfil `test`)

```bash
# Todos os testes do perfil test (unitários + integração + segurança + concorrência + performance + E2E de exceções)
docker compose --profile test run --rm oficina-tests mvn test

# Após a primeira execção, o banco pode conter dados de testes anteriores.
# Para garantir um estado limpo, recrie o volume do banco:
docker compose --profile test down -v && docker compose --profile test run --rm oficina-tests mvn test
```

### Executar uma classe ou método específico

```bash
# Via Docker (perfil test)
docker compose --profile test run --rm oficina-tests mvn test -Dtest=ClienteServiceTest
docker compose --profile test run --rm oficina-tests mvn test -Dtest="ClienteServiceTest\$AtualizarCliente"
docker compose --profile test run --rm oficina-tests mvn test -Dtest=FuncionarioServiceTest#deveLancarExcecaoQuandoInexistente

# Localmente (requer JDK 21)
./mvnw test -Dtest="ClienteServiceTest"
./mvnw test -Dtest="ClienteServiceTest\$AtualizarCliente"
```

### Visualizar relatórios de testes

```bash
# Relatório texto do Surefire (PowerShell)
Get-Content "target\surefire-reports\br.com.oficina.oficina.service.ClienteServiceTest.txt"
```

---

### 1. Testes unitários

Testam serviços, controladores e utilitários de forma isolada com Mockito.

| Classe | Testes | Escopo |
|---|---|---|
| `OficinaApplicationTests` | 1 | Context load do Spring |
| `AuthControllerTest` | 19 | Login, refresh, logout — sucesso e falhas |
| `JwtUtilTest` | 17 | Geração, extração de claims, expiração e tokens inválidos |
| `RefreshTokenServiceTest` | 19 | Criar, rotacionar, validar, revogar tokens |
| `ClienteServiceTest` | 19 | Cadastrar, buscar por ID/CPF, listar, atualizar (com/sem CEP), deletar |
| `VeiculoServiceTest` | 18 | Cadastrar, buscar por ID/placa, listar, atualizar, deletar, contar |
| `AtendimentoServiceTest` | 27 | Cadastrar, buscar, listar, atualizar, transições de status, deletar |
| `FuncionarioServiceTest` | 23 | Cadastrar, buscar, autenticar, listar, atualizar, deletar |
| `GlobalExceptionHandlerTest` | 10 | Mapeamento de exceções customizadas, mascaramento de erros de banco |
| **Total** | **153** | |

---

### 2. Testes de integração

Validam comportamentos reais com banco PostgreSQL (Docker).

| Classe | Testes | Escopo |
|---|---|---|
| `DataIntegrityExceptionIntegrationTest` | 4 | Violações de FK em PostgreSQL real (funcionário, cliente, veículo com atendimentos vinculados) |

---

### 3. Testes de segurança

| Classe | Testes | Escopo |
|---|---|---|
| `SecurityInputValidationTest` | 18 | SQL injection em buscas, XSS em campos de entrada, CPF/CNPJ inválidos, credenciais vazias |

---

### 4. Testes de concorrência

| Classe | Testes | Escopo |
|---|---|---|
| `DeleteConcurrencyTest` | 4 | Race conditions entre `countBy` e `delete` — documenta a rede de segurança `DataIntegrityViolationException` |

---

### 5. Testes E2E

#### E2E — Contrato de erro (`test`)

Testam o `GlobalExceptionHandler` via HTTP real com token JWT.

| Classe | Testes | Escopo |
|---|---|---|
| `ExceptionHandlerE2ETest` | 7 | 403 sem token, 401 credenciais inválidas, 404 ID inexistente, 409 duplicado, 500 RuntimeException (documenta information leakage), 400 validação |

#### E2E — CRUD completo (`e2e`)

Roda no container `oficina-e2e-tests` com banco `postgres-e2e` isolado.

```bash
# Todos os E2E de CRUD
docker compose --profile e2e run --rm oficina-e2e-tests mvn test "-Dspring.profiles.active=e2e" "-Dtest=*E2ETest"

# Classe específica
docker compose --profile e2e run --rm oficina-e2e-tests mvn test "-Dspring.profiles.active=e2e" "-Dtest=AtendimentoE2ETest"
```

| Classe | Testes | Cenários cobertos |
|---|---|---|
| `AuthE2ETest` | 8 | Login com credenciais válidas/inválidas, refresh token, logout, token revogado |
| `ClienteE2ETest` | 14 | CRUD completo, busca por CPF/CNPJ, 403 sem token, 409 por CPF/email duplicado |
| `VeiculoE2ETest` | 16 | CRUD completo, busca por placa, deleção por placa, 409 por placa duplicada |
| `AtendimentoE2ETest` | 13 | CRUD completo, transições de status válidas e inválidas |
| `FuncionarioE2ETest` | 9 | CRUD completo, 409 por CPF/usuário/email duplicados |
| **Total** | **60** | |

---

### Pular testes no build

```bash
./mvnw clean package -DskipTests
```

## Regras de negócio e casos de borda

### Cliente

| ID | Regra | Violação | Status HTTP |
|---|---|---|---|
| CLI-01 | **CPF/CNPJ único** — não podem existir dois clientes com o mesmo documento | `RecursoJaCadastradoException`: "CPF/CNPJ já cadastrado no sistema" | 409 |
| CLI-02 | **Email único** — não podem existir dois clientes com o mesmo email | `RecursoJaCadastradoException`: "Email já cadastrado no sistema" | 409 |
| CLI-03 | **CEP deve ser resolvível via ViaCEP** no cadastro e na atualização (se CEP/numero/complemento mudarem) | `CepNaoEncontradoException`: "CEP não encontrado: {cep}" | 400 |
| CLI-04 | **CEP deve conter exatamente 8 dígitos** (formatado ou não) | `MethodArgumentNotValidException` | 400 |
| CLI-05 | **Cliente deve existir** nas operações de busca, atualização e deleção por ID ou CPF/CNPJ | `ClienteNaoEncontradoException`: "Cliente não encontrado com ID: {id}" | 404 |
| CLI-06 | **Cliente com veículos não pode ser deletado** — é necessário remover ou transferir os veículos primeiro | `ClienteComVeiculosException`: "Não é possível deletar o cliente. Existem {N} veículo(s) associado(s)." | 409 |
| CLI-07 | **Cliente com atendimentos não pode ser deletado** — é necessário remover ou encerrar os atendimentos antes | `ClienteComAtendimentosException`: "Não é possível deletar o cliente. Existem {N} atendimento(s) associado(s)." | 409 |
| CLI-08 | **Endereço só é refeito via ViaCEP na atualização se CEP, número ou complemento forem alterados** — se os 3 forem iguais, o endereço existente é mantido | N/A | N/A |
| CLI-09 | **Nome, telefone e email são normalizados** (trim; email minúsculo) | N/A | N/A |

### Veículo

| ID | Regra | Violação | Status HTTP |
|---|---|---|---|
| VEI-01 | **Placa única** — não podem existir dois veículos com a mesma placa | `RecursoJaCadastradoException`: "Placa já cadastrada no sistema" | 409 |
| VEI-02 | **Placa não pode conflitar com outro veículo na atualização** — se a placa foi alterada, verifica unicidade | `RecursoJaCadastradoException`: "Placa já cadastrada para outro veículo" | 409 |
| VEI-03 | **Cliente associado deve existir** no cadastro e atualização | `ClienteNaoEncontradoException`: "Cliente não encontrado com ID: {id}" | 404 |
| VEI-04 | **Veículo deve existir** nas operações de busca, atualização e deleção por ID ou placa | `VeiculoNaoEncontradoException`: "Veículo não encontrado com ID: {id}" | 404 |
| VEI-05 | **Placa deve seguir formato Mercosul (AAA0A00) ou brasileiro antigo (AAA0000)** — letras maiúsculas | `MethodArgumentNotValidException` | 400 |
| VEI-06 | **Ano entre 1900 e 2100** | `MethodArgumentNotValidException` | 400 |
| VEI-07 | **Quilometragem não pode ser negativa** (campo opcional) | `MethodArgumentNotValidException` | 400 |
| VEI-08 | **Placa é normalizada** (maiúscula, sem espaços) antes de qualquer operação | N/A | N/A |
| VEI-09 | **Veículo com atendimentos não pode ser deletado** — tanto por ID quanto por placa, a deleção é bloqueada se houver atendimentos vinculados | `VeiculoComAtendimentosException`: "Não é possível deletar o veículo. Existem {N} atendimento(s) associado(s)." | 409 |

### Atendimento

| ID | Regra | Violação | Status HTTP |
|---|---|---|---|
| ATD-01 | **Transição de status é validada** — o estado atual deve permitir a transição solicitada | `TransicaoStatusInvalidaException`: "Não é permitido alterar o status de '{atual}' para '{novo}'." | 422 |
| ATD-02 | **Transições permitidas:** `AGUARDANDO → ANDAMENTO`, `AGUARDANDO → CANCELADO`, `ANDAMENTO → CONCLUIDO`, `ANDAMENTO → CANCELADO`. `CONCLUIDO` e `CANCELADO` são terminais | N/A | N/A |
| ATD-03 | **Cliente associado deve existir** no cadastro, atualização e listagem por cliente | `ClienteNaoEncontradoException` | 404 |
| ATD-04 | **Veículo associado deve existir** (buscado por placa) | `VeiculoNaoEncontradoException`: "Veículo não encontrado com a placa: {placa}" | 404 |
| ATD-05 | **Funcionário associado deve existir** no cadastro e atualização | `FuncionarioNaoEncontrado`: "Funcionário não encontrado com ID: {id}" | 404 |
| ATD-06 | **Atendimento deve existir** nas operações de busca, atualização e deleção por ID | `AtendimentoNaoEncontrado`: "Atendimento não encontrado com ID: {id}" | 404 |
| ATD-07 | **Status padrão é `AGUARDANDO`** se não informado no cadastro | N/A | N/A |
| ATD-08 | **Data de entrada padrão é `LocalDateTime.now()`** se não informada | N/A | N/A |
| ATD-09 | **`dataConclusao` é setada automaticamente** quando o status transiciona para `CONCLUIDO` ou `CANCELADO` | N/A | N/A |
| ATD-10 | **Status inicial não é validado** — o DTO pode enviar `CONCLUIDO` já no cadastro e não será rejeitado | Caso de borda | N/A |

### Funcionário

| ID | Regra | Violação | Status HTTP |
|---|---|---|---|
| FUN-01 | **CPF/CNPJ único** — não podem existir dois funcionários com o mesmo documento | `RecursoJaCadastradoException`: "CPF/CNPJ já cadastrado no sistema" | 409 |
| FUN-02 | **Usuário único** no cadastro | `RecursoJaCadastradoException`: "Usuário já cadastrado no sistema" | 409 |
| FUN-03 | **Email único** no cadastro (campo opcional, mas se fornecido deve ser único) | `RecursoJaCadastradoException`: "Email já cadastrado no sistema" | 409 |
| FUN-04 | **DTO de cadastro não pode ser nulo** | `IllegalArgumentException`: "Dados do funcionário são obrigatórios" | 400 |
| FUN-05 | **Usuário deve conter apenas letras minúsculas, números, pontos, hífens ou sublinhados** | `MethodArgumentNotValidException` | 400 |
| FUN-06 | **Senha mínimo 8 caracteres** | `MethodArgumentNotValidException` | 400 |
| FUN-07 | **Senha é codificada com BCrypt** antes de persistir | N/A | N/A |
| FUN-08 | **Usuário não pode ser alterado** na atualização (evitaria invalidação de tokens) | N/A | N/A |
| FUN-09 | **Senha é opcional na atualização** — se não fornecida, a senha existente é mantida | N/A | N/A |
| FUN-10 | **Funcionário deve existir** nas operações de busca, atualização e deleção por ID | `FuncionarioNaoEncontrado`: "Funcionário não encontrado com ID: {id}" | 404 |
| FUN-11 | **Funcionário com atendimentos não pode ser deletado** — a deleção é bloqueada se houver atendimentos vinculados ao funcionário | `FuncionarioComAtendimentosException`: "Não é possível deletar o funcionário. Existem {N} atendimento(s) associado(s)." | 409 |

### Autenticação / Segurança

| ID | Regra | Violação | Status HTTP |
|---|---|---|---|
| AUTH-01 | **Endpoints públicos:** `POST /funcionarios`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, Swagger, OPTIONS | N/A | N/A |
| AUTH-02 | **Demais endpoints exigem `Authorization: Bearer <token>`** | Resposta padrão do Spring Security (403 Forbidden) | 403 |
| AUTH-03 | **AccessToken expira em 15 minutos** | Filtro silencia e requisição segue sem autenticação → 403 | 403 |
| AUTH-04 | **RefreshToken expira em 7 dias** e é armazenado no banco | N/A | N/A |
| AUTH-05 | **Login inválido** (usuário/senha incorretos) | `CredenciaisInvalidasException`: "Credenciais inválidas" | 401 |
| AUTH-06 | **RefreshToken revogado ou expirado** | `CredenciaisInvalidasException` | 401 |
| AUTH-07 | **Refresh token rotation** — ao usar um refresh token, ele é revogado e um novo é emitido; tokens anteriores do mesmo usuário também são revogados | N/A | N/A |
| AUTH-08 | **Limpeza automática** de tokens revogados/expirados toda noite às 3 AM (`@Scheduled`) | N/A | N/A |
| AUTH-09 | **CORS restrito** a `localhost:5173`, `localhost:4173`, `localhost:3000` | N/A | N/A |
| AUTH-10 | **JWT expirado/inválido é silenciado** — não retorna erro customizado, apenas deixa a requisição sem autenticação | Caso de borda | 403 |

### Endereço / CEP

| ID | Regra | Violação | Status HTTP |
|---|---|---|---|
| CEP-01 | **CEP é resolvido via ViaCEP** nas operações de cadastro e atualização de cliente | `CepNaoEncontradoException`: "CEP não encontrado: {cep}" | 400 |
| CEP-02 | **CEP deve conter 8 dígitos** após limpeza de não-numéricos | `IllegalArgumentException` ou `MethodArgumentNotValidException` | 400 |
| CEP-03 | **Consulta direta de CEP** (`GET /api/viacep/endereco/{cep}`) é pública (sem autenticação) | 404 se CEP não encontrado | 404 |
| CEP-04 | **ViaCEP retorna `{"erro": true}`** para CEPs inexistentes — tratado como `CepNaoEncontradoException` | Caso de borda | 400 |

## Testando a API com Postman

### Importar a collection

1. Abra o Postman e clique em **Import**.
2. Selecione `oficina_postman_collection.json` na raiz do projeto.
3. A collection "Oficina Mecânica API" aparecerá com 34 requisições em 6 pastas.

### Variáveis globais

| Variável | Padrão | Descrição |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | URL base da API |
| `token` | *(vazio)* | AccessToken (preenchido no login) |
| `refreshToken` | *(vazio)* | RefreshToken (preenchido no login) |
| `usuarioLogin` | `admin` | Usuário de autenticação |
| `senhaLogin` | `senha123` | Senha de autenticação |
| `funcionarioId` | *(vazio)* | ID do funcionário |
| `funcionarioCpf` | *(vazio)* | CPF do funcionário (ex: `52998224725`) — deve ser único e válido |
| `clienteId` | *(vazio)* | ID do cliente |
| `clienteCpf` | *(vazio)* | CPF do cliente (ex: `11144477735`) — deve ser único e válido |
| `veiculoId` | *(vazio)* | ID do veículo |
| `veiculoPlaca` | `ABC1D23` | Placa do veículo |
| `atendimentoId` | *(vazio)* | ID do atendimento |

### Fluxo recomendado

```
Auth
  └─ 1. Cadastrar Funcionário (público)
  └─ 2. Login → salva token
  └─ GET /funcionarios/me

Clientes
  └─ Cadastrar cliente → salva {{clienteId}}
  └─ Listar / Buscar

Veículos
  └─ Cadastrar veículo → salva {{veiculoId}}
  └─ Listar / Buscar por placa

Atendimentos
  └─ Cadastrar atendimento → salva {{atendimentoId}}
  └─ Listar / Atualizar status

Integrações Externas
  └─ ViaCEP — buscar endereço
  └─ Brasil API — feriados
```

> A collection usa **Bearer Token** configurado no workspace. Após o login o token é injetado automaticamente.

> **Importante:** Os CPFs usados nos requests de cadastro (`funcionarioCpf` e `clienteCpf`) devem ser **válidos** (com dígitos verificadores corretos) e **únicos** no banco. CPFs duplicados retornam **409 Conflict**. Use um gerador de CPF online antes de executar a collection, ou altere os valores das variáveis a cada execução.

## Variáveis de ambiente

### Backend

Arquivo `.env` na raiz (usado apenas para execução local fora do Docker):

```env
SPRING_PROFILES_ACTIVE=docker
JWT_SECRET=chave-para-ambiente-local
JWT_EXPIRATION_MS=3600000
```

### Frontend

Arquivo `crm-oficina/.env` (usado apenas em `npm run dev`; ignorado no build Docker):

```env
VITE_API_URL=http://localhost:8080
```

> No Docker o `VITE_API_URL` é injetado como `/api` via build arg no `docker-compose.yml`, fazendo com que as chamadas passem pelo proxy reverso do Nginx.

## Segurança

- Autenticação via JWT (jjwt 0.11.5)
- AccessToken: 15 min de validade
- RefreshToken: 7 dias (armazenado no banco, revogável)
- BCrypt para hash de senhas
- Endpoints públicos: apenas `POST /funcionarios`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` e Swagger
- Demais endpoints exigem header `Authorization: Bearer <token>`
