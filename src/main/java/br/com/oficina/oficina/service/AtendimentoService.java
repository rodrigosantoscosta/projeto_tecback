package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.atendimento.CadastrarAtendimentoDTO;
import br.com.oficina.oficina.exception.AtendimentoNaoEncontrado;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.VeiculoNaoEncontradoException;
import br.com.oficina.oficina.model.Atendimento;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.model.StatusAtendimento;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.AtendimentoRepository;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.FuncionarioRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class AtendimentoService {

    public final AtendimentoRepository atendimentoRepository;

    public final ClienteRepository clienteRepository;

    public final FuncionarioRepository funcionarioRepository;

    public final VeiculoRepository veiculoRepository;

    public void cadastrarAtendimentoSemDados(Atendimento atendimento) {
        atendimentoRepository.save(atendimento);
    }

    @Transactional
    public Atendimento cadastrarAtendimento(CadastrarAtendimentoDTO atendimentoDto) {
        log.info("Cadastrando atendimento - descrição: {}", atendimentoDto.getDescricaoServico());
        
        // Buscar cliente
        Cliente cliente = clienteRepository.findById(atendimentoDto.getClienteId())
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", atendimentoDto.getClienteId());
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + atendimentoDto.getClienteId()
                    );
                });
        log.info("Cliente encontrado: {} - {}", cliente.getId(), cliente.getNomeCompleto());

        // Buscar veículo
        Veiculo veiculo = veiculoRepository.findByPlaca(atendimentoDto.getVeiculoPlaca())
                .orElseThrow(() -> {
                    log.error("Placa do veículo não encontrada: {}", atendimentoDto.getVeiculoPlaca());
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com a placa: " + atendimentoDto.getVeiculoPlaca()
                    );
                });
        log.info("Veículo encontrado: {} - {}", veiculo.getId(), veiculo.getPlaca());

        // Buscar funcionário
        Funcionario funcionario = funcionarioRepository.findById(atendimentoDto.getFuncionarioId())
                .orElseThrow(() -> {
                    log.error("Funcionário não encontrado: {}", atendimentoDto.getFuncionarioId());
                    return new RuntimeException("Funcionário não encontrado com ID: " + atendimentoDto.getFuncionarioId());
                });
        log.info("Funcionário encontrado: {} - {}", funcionario.getId(), funcionario.getNome());

        // Criar e salvar atendimento
        Atendimento atendimento = new Atendimento();
        atendimento.setDescricaoServico(atendimentoDto.getDescricaoServico());
        atendimento.setStatus(StatusAtendimento.AGUARDANDO);
        atendimento.setCliente(cliente);
        atendimento.setVeiculo(veiculo);
        atendimento.setFuncionario(funcionario);
        
        // As datas serão preenchidas pelo @PrePersist
        Atendimento atendimentoSalvo = atendimentoRepository.save(atendimento);
        
        log.info("Atendimento cadastrado com sucesso - ID: {}, Cliente: {}, Veículo: {}, Funcionário: {}", 
                atendimentoSalvo.getId(), 
                atendimentoSalvo.getCliente().getNomeCompleto(),
                atendimentoSalvo.getVeiculo().getPlaca(),
                atendimentoSalvo.getFuncionario().getNome());

        return atendimentoSalvo;
    }

    public Atendimento buscarAtendimentoPorId(UUID id) {
        log.info("Buscando atendimento por ID: {}", id);
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Atendimento não encontrado: {}", id);
                    return new RuntimeException(
                            "Atendimento não encontrado com ID: " + id
                    );
                });
    }

    public List<Atendimento> listarAtendimentosPorClienteID(UUID clienteId) {
        log.info("Listando atendimentos do cliente: {}", clienteId);
        return atendimentoRepository.findByClienteId(clienteId);

    }
    @Transactional
    public Atendimento atualizarAtendimento(UUID id, CadastrarAtendimentoDTO atendimentoDTO){
        Atendimento atendimentoExistente = atendimentoRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("Atendimento não encontrado: {}", id);
                    return new RuntimeException(
                            "Atendimento não encontrado com ID: " + id
                    );
                });

       StatusAtendimento statusAtualizado = atendimentoDTO.getStatusAtendimento();

        if(statusAtualizado != null){
             atendimentoExistente.setStatus(statusAtualizado);
            log.info("Status do atendimento atualizado para: {}", atendimentoDTO.getStatusAtendimento());
        }

        atendimentoExistente.setDescricaoServico(atendimentoDTO.getDescricaoServico());
        atendimentoExistente.setStatus(statusAtualizado);


        Atendimento atendimentoAtualizado = atendimentoRepository.save(atendimentoExistente);
        log.info("Atendimento atualizado com sucesso - ID: {}", atendimentoAtualizado.getId());

        return atendimentoAtualizado;
    }


    @Transactional
    public void deletarAtendimentoPorId(UUID id) {
        log.info("Deletando Atendimento: {}", id);

       Atendimento atendimento = atendimentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Atendimento não encontrado para deleção: {}", id);
                    return new AtendimentoNaoEncontrado(
                            "Atendimento não encontrado com ID: " + id
                    );
                });

        atendimentoRepository.delete(atendimento);
        log.info("Veículo deletado com sucesso: {}", id);
    }

    public List<Atendimento> listarTodosAtendimentos() {
        return  atendimentoRepository.findAll();
    }
}
