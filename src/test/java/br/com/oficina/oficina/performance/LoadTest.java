package br.com.oficina.oficina.performance;

import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de performance/carga que medem tempos de resposta e throughput
 * dos endpoints criticos da aplicacao.
 *
 * <p>Estes testes servem como baseline para detectar regressoes de performance
 * em futuras alteracoes no codigo.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Performance — Load Test Baseline")
class LoadTest {

    @LocalServerPort private int port;
    @Autowired private RestTemplate rest;
    @Autowired private ObjectMapper objectMapper;

    private HttpClient httpClient;
    private String baseUrl;
    private String accessToken;

    private String loadUsuario;

    @BeforeAll
    void setUp() throws Exception {
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        baseUrl = "http://localhost:" + port;

        // Dados aleatorios para evitar conflito com execucoes anteriores no banco persistente
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        loadUsuario = "load.test." + suffix;
        String cpf = gerarCpfValido();
        String email = "load." + suffix + "@oficina.com";

        // Cria um funcionario admin para obter token de acesso
        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
        dto.setNome("Load Test Admin");
        dto.setCpfCNPJ(cpf);
        dto.setCargo("ADMIN");
        dto.setTelefone("11999999999");
        dto.setEmail(email);
        dto.setUsuario(loadUsuario);
        dto.setSenha("senha123");

        ResponseEntity<FuncionarioDTO> resp = rest.postForEntity(
            baseUrl + "/funcionarios", dto, FuncionarioDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Login
        LoginRequest login = new LoginRequest();
        login.setUsuario(loadUsuario);
        login.setSenha("senha123");

        ResponseEntity<AuthResponse> loginResp = rest.postForEntity(
            baseUrl + "/auth/login", login, AuthResponse.class);
        accessToken = loginResp.getBody().getAccessToken();
    }

    private String gerarCpfValido() {
        java.util.Random rand = new java.util.Random();
        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) cpf[i] = rand.nextInt(10);
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += cpf[i] * (10 - i);
        int resto = soma % 11;
        cpf[9] = (resto < 2) ? 0 : 11 - resto;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += cpf[i] * (11 - i);
        resto = soma % 11;
        cpf[10] = (resto < 2) ? 0 : 11 - resto;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) sb.append(cpf[i]);
        return sb.toString();
    }

    private String gerarCnpjValido() {
        java.util.Random rand = new java.util.Random();
        int[] cnpj = new int[14];
        for (int i = 0; i < 12; i++) cnpj[i] = rand.nextInt(10);
        // Calcula 1o digito
        int[] peso1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int soma = 0;
        for (int i = 0; i < 12; i++) soma += cnpj[i] * peso1[i];
        int resto = soma % 11;
        cnpj[12] = (resto < 2) ? 0 : 11 - resto;
        // Calcula 2o digito
        int[] peso2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
        soma = 0;
        for (int i = 0; i < 13; i++) soma += cnpj[i] * peso2[i];
        resto = soma % 11;
        cnpj[13] = (resto < 2) ? 0 : 11 - resto;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) sb.append(cnpj[i]);
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Carga em endpoint publico (POST /funcionarios)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /funcionarios — 50 requisicoes concorrentes devem completar em < 5s (p95)")
    void cadastroFuncionarioDeveSuportarCarga() throws Exception {
        int requests = 50;
        List<Long> latencias = new CopyOnWriteArrayList<>();
        List<Integer> statusCodes = new CopyOnWriteArrayList<>();

        List<CompletableFuture<Void>> futures = IntStream.range(0, requests)
            .mapToObj(i -> {
                String cpf = gerarCpfValido();
                String json = String.format(
                    "{\"nome\":\"Func %d\",\"cpfCNPJ\":\"%s\",\"cargo\":\"MECANICO\"," +
                    "\"telefone\":\"119%08d\",\"email\":\"func%d_%s@oficina.com\"," +
                    "\"usuario\":\"func%d_%s\",\"senha\":\"senha123\"}",
                    i, cpf, i, i, cpf.substring(0,4), i, cpf.substring(0,4)
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/funcionarios"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

                Instant start = Instant.now();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        long latency = Duration.between(start, Instant.now()).toMillis();
                        latencias.add(latency);
                        statusCodes.add(response.statusCode());
                    });
            })
            .toList();

        // Aguarda todas completarem (timeout 30s)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        // Asserts
        long p95 = latencias.stream().sorted().skip((long)(latencias.size() * 0.95)).findFirst().orElse(0L);
        long max = latencias.stream().max(Long::compare).orElse(0L);
        double avg = latencias.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.printf("[PERFORMANCE] /funcionarios — avg=%.1fms, p95=%dms, max=%dms%n", avg, p95, max);

        assertThat(p95).as("p95 deve ser menor que 5000ms").isLessThan(5000);
        assertThat(statusCodes.stream().filter(c -> c == 200 || c == 201 || c == 409).count())
            .as("Maioria deve retornar sucesso ou conflito (usuario duplicado)")
            .isGreaterThan((long)(requests * 0.8));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Carga em endpoint autenticado (GET /funcionarios)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /funcionarios — 100 requisicoes concorrentes autenticadas")
    void listarFuncionariosDeveSuportarCarga() throws Exception {
        int requests = 100;
        List<Long> latencias = new CopyOnWriteArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/funcionarios"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        List<CompletableFuture<Void>> futures = IntStream.range(0, requests)
            .mapToObj(i -> {
                Instant start = Instant.now();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        long latency = Duration.between(start, Instant.now()).toMillis();
                        latencias.add(latency);
                        assertThat(response.statusCode()).isEqualTo(200);
                    });
            })
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        long p95 = latencias.stream().sorted().skip((long)(latencias.size() * 0.95)).findFirst().orElse(0L);
        double avg = latencias.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.printf("[PERFORMANCE] GET /funcionarios — avg=%.1fms, p95=%dms%n", avg, p95);

        assertThat(p95).as("p95 deve ser menor que 2000ms").isLessThan(2000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Throughput baseline
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Login /auth/login — throughput baseline (100 reqs)")
    void loginDeveTerThroughputAceitavel() throws Exception {
        int requests = 100;
        List<Long> latencias = new CopyOnWriteArrayList<>();

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsuario(loadUsuario);
        loginReq.setSenha("senha123");
        String json = objectMapper.writeValueAsString(loginReq);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        Instant inicio = Instant.now();

        List<CompletableFuture<Void>> futures = IntStream.range(0, requests)
            .mapToObj(i -> {
                Instant start = Instant.now();
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        long latency = Duration.between(start, Instant.now()).toMillis();
                        latencias.add(latency);
                        assertThat(response.statusCode()).isIn(200, 401);
                    });
            })
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        long duracaoTotal = Duration.between(inicio, Instant.now()).toMillis();
        double throughput = (double) requests / (duracaoTotal / 1000.0);
        double avg = latencias.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = latencias.stream().sorted().skip((long)(latencias.size() * 0.95)).findFirst().orElse(0L);

        System.out.printf("[PERFORMANCE] /auth/login — throughput=%.1f req/s, avg=%.1fms, p95=%dms%n",
            throughput, avg, p95);

        assertThat((long) throughput).as("Throughput deve ser maior que 10 req/s").isGreaterThan(10L);
        assertThat(p95).as("p95 deve ser menor que 3000ms").isLessThan(3000);
    }
}
