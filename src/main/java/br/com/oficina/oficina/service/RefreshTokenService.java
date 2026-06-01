package br.com.oficina.oficina.service;

import br.com.oficina.oficina.exception.ResourceNotFoundException;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.model.RefreshToken;
import br.com.oficina.oficina.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /** Duração do refresh token em ms (padrão: 7 dias). */
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    /**
     * Cria um novo refresh token para o funcionário.
     * Revoga todos os tokens anteriores do mesmo funcionário antes de emitir o novo
     * (one-active-token-per-user — evita acúmulo de tokens válidos).
     */
    @Transactional
    public RefreshToken criar(Funcionario funcionario) {
        refreshTokenRepository.revogarTodosPorFuncionario(funcionario);

        RefreshToken rt = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .funcionario(funcionario)
                .expiraEm(Instant.now().plusMillis(refreshExpirationMs))
                .revogado(false)
                .build();

        return refreshTokenRepository.save(rt);
    }

    /**
     * Valida o token recebido do cliente.
     *
     * @throws ResourceNotFoundException se o token não existir
     * @throws IllegalStateException     se o token estiver revogado ou expirado
     */
    @Transactional(readOnly = true)
    public RefreshToken validar(String tokenStr) {
        RefreshToken rt = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token não encontrado"));

        if (rt.isRevogado()) {
            log.warn("Tentativa de uso de refresh token revogado — funcionário: {}",
                    rt.getFuncionario().getUsuario());
            throw new IllegalStateException("Refresh token revogado");
        }

        if (rt.isExpirado()) {
            log.warn("Refresh token expirado — funcionário: {}", rt.getFuncionario().getUsuario());
            throw new IllegalStateException("Refresh token expirado. Faça login novamente.");
        }

        return rt;
    }

    /** Revoga explicitamente um token (logout). */
    @Transactional
    public void revogar(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(rt -> {
            rt.setRevogado(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revogado — funcionário: {}", rt.getFuncionario().getUsuario());
        });
    }

    /**
     * Revoga o token atual e emite um novo numa única transação (token rotation).
     * Garante que nunca haja janela onde o token antigo foi revogado mas o novo
     * ainda não foi criado — o que causaria perda de sessão em falha entre as duas ops.
     *
     * @param tokenAntigo refresh token recebido do cliente
     * @return novo RefreshToken pronto para ser enviado ao cliente
     */
    @Transactional
    public RefreshToken rotacionar(String tokenAntigo) {
        RefreshToken rt = validar(tokenAntigo);
        rt.setRevogado(true);
        refreshTokenRepository.save(rt);
        log.info("Refresh token rotacionado — funcionário: {}", rt.getFuncionario().getUsuario());
        return criar(rt.getFuncionario());
    }

    /** Limpeza automática diária de tokens expirados ou revogados. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limparTokensAntigos() {
        refreshTokenRepository.deletarExpiradosERevogados();
        log.info("Limpeza de refresh tokens antigos concluída");
    }
}
