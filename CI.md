# CI Pipeline — GitHub Actions

## Visão Geral

Pipeline de Integração Contínua configurada com GitHub Actions para o projeto **oficina** (backend Java/Spring Boot).

**Arquivo:** `.github/workflows/ci.yml`

---

## Fluxo da Pipeline

```
Push para qualquer branch
    ↓
Checkout do código
    ↓
Correção de permissão do Maven wrapper (chmod +x)
    ↓
Configuração do Java 21 (Eclipse Temurin)
    ↓
Compilação (mvnw compile)
    ↓
Execução dos testes unitários (mvnw test)
    ↓
Geração do JAR (mvnw package -DskipTests)
    ↓
Upload do artefato (oficina-0.0.1-SNAPSHOT.jar)
```

---

## Push 1 — Pipeline com Sucesso

**Commit:** `ci: run only unit tests in CI, remove PostgreSQL dependency`
**Resultado:** Sucesso
**Duração:** 53s

| Etapa | Tempo | Status |
|---|---|---|
| Set up job | 0s | Passou |
| Checkout | 1s | Passou |
| Fix Maven wrapper permission | 0s | Passou |
| Set up Java 21 | 0s | Passou |
| Compile | 25s | Passou |
| Run unit tests | 14s | Passou |
| Package JAR | 6s | Passou |
| Upload JAR artifact | 2s | Passou |

**Resultado esperado:** Todos os 9 testes unitários passaram. O JAR foi gerado e disponibilizado como artifact para download (30 dias de retenção).

---

## Push 2 — Pipeline com Falha

**Commit:** `test: break JwtUtilTest to demonstrate CI failure`
**Branch:** `ci/unit-tests-only`
**Resultado:** Falha
**Duração:** 21s

| Etapa | Tempo | Status |
|---|---|---|
| Set up job | 0s | Passou |
| Checkout | 1s | Passou |
| Fix Maven wrapper permission | 0s | Passou |
| Set up Java 21 | 0s | Passou |
| Compile | ~19s | Falhou |
| Run unit tests | — | Não executou |
| Package JAR | — | Não executou |
| Upload JAR artifact | — | Não executou |

**Causa da falha:** Teste propositalmente alterado em `JwtUtilTest.java` — a asserção `assertThat(token).isEqualTo("TOKEN_FALSO")` falha porque o token JWT real nunca será igual a essa string.

**Comportamento esperado da pipeline:**
- Compilação passa (o código é válido)
- Teste falha (asserção incorreta)
- Etapas posteriores (Package, Upload) não são executadas
- Artefato JAR **não** é disponibilizado

---

## Configuração

### Serviço PostgreSQL

O PostgreSQL **não** é utilizado na pipeline CI. Os testes rodam apenas com Mockito (unit tests puros), sem dependência de banco de dados.

### Maven Surefire

Configurado no `pom.xml` para rodar apenas testes unitários:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/service/*Test.java</include>
            <include>**/controller/*Test.java</include>
            <include>**/security/*Test.java</include>
            <include>**/exception/*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

### Testes Incluídos (9 classes)

| Pacote | Classes |
|---|---|
| `service/` | AtendimentoServiceTest, ClienteServiceTest, FuncionarioServiceTest, RefreshTokenServiceTest, VeiculoServiceTest |
| `controller/` | AuthControllerTest |
| `security/` | JwtUtilTest, SecurityInputValidationTest |
| `exception/` | GlobalExceptionHandlerTest |

### Testes Excluídos (não rodam no CI)

- `e2e/*` — precisam de Docker Compose completo (PostgreSQL dedicado)
- `integration/*` — precisam de PostgreSQL real
- `performance/*` — precisam de PostgreSQL real
- `concurrency/*` — precisam de PostgreSQL real
- `OficinaApplicationTests` — precisa de Spring Context completo
