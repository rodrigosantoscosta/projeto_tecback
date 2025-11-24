# Documentação do Módulo de Segurança (Security)

## Visão Geral
O módulo de segurança implementa autenticação e autorização usando JWT (JSON Web Token) com Spring Security. O sistema é stateless, não mantendo sessões HTTP.

---

## 1. JwtAuthenticationFilter.java

**Propósito:** Filtro que processa tokens JWT em cada requisição HTTP.

**Fluxo de Execução:**
1. Extrai o header "Authorization" da requisição
2. Valida se o token começa com "Bearer "
3. Extrai o username do token JWT
4. Carrega os detalhes do usuário do banco de dados
5. Valida se o token é válido (username correto e não expirado)
6. Define o usuário autenticado no SecurityContext
7. Passa a requisição para o próximo filtro


**Métodos Principais:**
- `doFilterInternal()`: Processa cada requisição HTTP uma única vez

**Fluxo Detalhado do doFilterInternal:**
```
1. Obtém header "Authorization"
2. Se existir e começar com "Bearer ", extrai o token (remove 7 caracteres)
3. Tenta extrair o username do token
4. Se username extraído e sem autenticação prévia:
   - Carrega detalhes do usuário
   - Valida se token é válido
   - Se válido, cria token de autenticação e armazena no SecurityContext
5. Passa requisição para próximo filtro
```

---

## 2. JwtUtil.java

**Propósito:** Utilitário para operações com JWT (gerar, validar, extrair informações).

**Anotações Usadas:**
- `@Component`: Registra a classe como bean gerenciado pelo Spring
- `@Value`: Injeta valores de propriedades do `application.properties`

**Propriedades Configuráveis:**
- `jwt.secret`: Chave secreta para assinar tokens (padrão: "troque-por-uma-chave-muito-secreta-com-muitos-caracteres")
- `jwt.expiration-ms`: Tempo de expiração em ms (padrão: 3600000 = 1 hora)

**Métodos Principais:**

### `generateToken(UserDetails userDetails)`
Gera novo token JWT para usuário autenticado.
- Obtém data/hora atual
- Calcula data de expiração (agora + expirationMs)
- Cria builder JWT com:
  - Subject: username
  - IssuedAt: data de criação
  - Expiration: data de expiração
  - Signature: HMAC SHA-256
- Retorna token compactado (header.payload.signature)

### `extractUsername(String token)`
Extrai o username (subject) do token JWT.

### `extractExpiration(String token)`
Extrai a data de expiração do token JWT.

### `extractClaim(String token, Function<Claims, T> resolver)`
Extrai um claim específico do token de forma genérica.
- Cria parser JWT com chave de assinatura
- Valida e faz parse do token
- Extrai o corpo (claims)
- Aplica função resolver para extrair claim desejado

### `isTokenValid(String token, UserDetails userDetails)`
Valida se o token é válido para o usuário.
- Verifica se username no token = username do usuário
- Verifica se token não está expirado

### `isTokenExpired(String token)` (privado)
Verifica se token expirou comparando data de expiração com data/hora atual.

---

## 3. UsuarioPrincipal.java

**Propósito:** Implementação de `UserDetails` do Spring Security, representando o usuário autenticado.

**Anotações Usadas:**
- Nenhuma anotação de Spring (é um POJO)
- Implementa interface `UserDetails` do Spring Security

**Atributos:**
- `id`: UUID único do usuário
- `username`: Nome de usuário para login
- `password`: Senha hash (BCrypt)
- `authorities`: Permissões/roles do usuário

**Métodos Principais:**

### `fromFuncionario(Funcionario f)` (factory method)
Cria UsuarioPrincipal a partir de uma entidade Funcionario.
- Extrai cargo do funcionário
- Cria GrantedAuthority com o cargo
- Retorna nova instância de UsuarioPrincipal

### Métodos de UserDetails:
- `getAuthorities()`: Retorna autoridades/permissões
- `getPassword()`: Retorna senha hash
- `getUsername()`: Retorna nome de usuário
- `isAccountNonExpired()`: Sempre retorna true
- `isAccountNonLocked()`: Sempre retorna true
- `isCredentialsNonExpired()`: Sempre retorna true
- `isEnabled()`: Sempre retorna true

---

## 4. SecurityConfig.java

**Propósito:** Configuração centralizada de segurança da aplicação.

**Anotações Usadas:**
- `@Configuration`: Marca a classe como configuração do Spring
- `@RequiredArgsConstructor`: Gera construtor com campos final (Lombok)
- `@Bean`: Registra métodos como beans gerenciados
- `@Value`: Injeta propriedades de configuração

**Propriedades Configuráveis:**
- `app.security.enabled`: Ativa/desativa segurança (padrão: true)

**Métodos Principais:**

### `jwtAuthenticationFilter()`
Cria e configura o filtro JWT para autenticação.
- Retorna instância de JwtAuthenticationFilter

### `filterChain(HttpSecurity http)`
Configura as regras de segurança da aplicação.

**Configurações:**
1. **CSRF Desabilitado**: Necessário para APIs RESTful com JWT
2. **Sessão Stateless**: Não usa sessões HTTP, apenas JWT
3. **Autorização de Requisições:**
   - POST `/funcionarios`: Sem autenticação (criação de conta)
   - `/funcionarios/login`: Sem autenticação (login)
   - `/v3/**`, `/swagger-ui/**`: Sem autenticação (documentação)
   - `/`, `/index.html`, `/style.css`, `/validation.css`: Sem autenticação (frontend)
   - Todas outras: Requer autenticação

4. **Filtro JWT**: Adicionado antes do filtro de autenticação padrão

### `authenticationManager(AuthenticationConfiguration config)`
Configura o gerenciador de autenticação do Spring Security.

### `passwordEncoder()`
Configura codificador de senhas usando BCrypt.

---

## Fluxo Completo de Autenticação

```
1. Usuário faz login em /funcionarios/login com username/password
2. Spring Security valida credenciais usando PasswordEncoder (BCrypt)
3. Se válido, JwtUtil.generateToken() cria um JWT
4. Token é retornado ao cliente
5. Cliente envia token em requisições subsequentes no header: "Authorization: Bearer <token>"
6. JwtAuthenticationFilter intercepta a requisição
7. Extrai e valida o token
8. Se válido, cria UsernamePasswordAuthenticationToken
9. Armazena no SecurityContext
10. Requisição é processada com usuário autenticado
```

---

## Fluxo de Validação de Token

```
1. JwtAuthenticationFilter recebe requisição com header "Authorization: Bearer <token>"
2. Extrai token (remove "Bearer ")
3. JwtUtil.extractUsername() extrai username do token
4. CustomUserDetailsService.loadUserByUsername() carrega usuário do BD
5. JwtUtil.isTokenValid() valida:
   - Username no token = username do usuário
   - Token não expirou
6. Se válido:
   - Cria UsernamePasswordAuthenticationToken
   - Armazena em SecurityContextHolder
   - Requisição continua autenticada
7. Se inválido:
   - Requisição continua sem autenticação
   - Se endpoint requer autenticação, retorna 403 Forbidden
```

---

## Segurança

1. **Chave Secreta**: Em produção, use uma chave muito mais longa e segura
2. **HTTPS**: Sempre use HTTPS em produção para proteger tokens em trânsito
3. **Expiração**: Tokens expiram após 1 hora (configurável)
4. **Stateless**: Não há sessões no servidor, melhor escalabilidade
5. **BCrypt**: Senhas são hasheadas com BCrypt, nunca armazenadas em texto plano

---

## Configuração Recomendada para Produção

Adicionar ao `application.properties`:
```properties
jwt.secret=uma-chave-muito-longa-e-segura-com-muitos-caracteres-aleatorios
jwt.expiration-ms=3600000
app.security.enabled=true
```

---

## Dependências Necessárias

- Spring Security
- JJWT (JSON Web Token library)
- Lombok (para @RequiredArgsConstructor)
- Spring Boot Web
