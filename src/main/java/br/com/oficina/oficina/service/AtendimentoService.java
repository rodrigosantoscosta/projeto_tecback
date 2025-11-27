package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.atendimento.AtendimentoDTO;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class AtendimentoService {

    public final AtendimentoRepository atendimentoRepository;
    public final ClienteRepository clienteRepository;
    public final FuncionarioRepository funcionarioRepository;
    public final VeiculoRepository veiculoRepository;


    @Transactional
    public AtendimentoDTO cadastrarAtendimento(CadastrarAtendimentoDTO atendimentoDto){
        log.info("Cadastrando atendimento - descrição: {}", atendimentoDto.getDescricaoServico());

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
        atendimento.getStatus();
        atendimento.setCliente(cliente);
        atendimento.setVeiculo(veiculo);
        atendimento.setFuncionario(funcionario);

        // As datas serão preenchidas pelo @PrePersist
        Atendimento atendimentoSalvo = atendimentoRepository.save(atendimento);

        //Criando Variaveis para setar no DTO
        UUID clienteAtendimentoId = atendimentoSalvo.getCliente().getId();
        UUID funcionarioAtendimentoId = atendimentoSalvo.getFuncionario().getId();
        String veiculoAtendimentoPlaca = atendimentoSalvo.getVeiculo().getPlaca();


        //Montando o DTO de resposta do atendimento
        AtendimentoDTO atendimentoDTO = new AtendimentoDTO(atendimentoSalvo);
        atendimentoDTO.setId(atendimentoSalvo.getId());
        atendimentoDTO.setDescricaoServico(atendimento.getDescricaoServico());
        atendimentoDTO.setStatus(atendimento.getStatus());
        atendimentoDTO.setDataCadastro(atendimento.getDataCadastro());
        atendimentoDTO.setDataEntrada(atendimento.getDataEntrada());
        atendimentoDTO.setDataConclusao(atendimento.getDataConclusao());
        atendimentoDTO.setCliente(clienteAtendimentoId);
        atendimentoDTO.setFuncionario(funcionarioAtendimentoId);
        atendimentoDTO.setVeiculo(veiculoAtendimentoPlaca);




        log.info("Atendimento cadastrado com sucesso - ID: {}, Cliente: {}, Veículo: {}, Funcionário: {}",
                atendimentoSalvo.getId(),
                atendimentoSalvo.getCliente().getNomeCompleto(),
                atendimentoSalvo.getVeiculo().getPlaca(),
                atendimentoSalvo.getFuncionario().getNome());

        return atendimentoDTO;
    }

    public AtendimentoDTO buscarAtendimentoPorId(UUID id) {
        log.info("Buscando atendimento por ID: {}", id);
        return atendimentoRepository.findById(id)
                .map(AtendimentoDTO::new)
                .orElseThrow(() -> new AtendimentoNaoEncontrado("Atendimento não encontrado"));
    }


    public List<AtendimentoDTO> listarAtendimentosPorClienteID(UUID clienteId) {
        log.info("Listando atendimentos por Cliente ID: {}", clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", clienteId);
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + clienteId
                    );
                });
        UUID clienteidEncontrado = cliente.getId();


        return atendimentoRepository.findByClienteId(clienteidEncontrado)
                .stream()
                .map(AtendimentoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AtendimentoDTO atualizarAtendimento(UUID id, CadastrarAtendimentoDTO atendimentoDto){
        Atendimento atendimentoExistente = atendimentoRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("Atendimento não encontrado: {}", id);
                    return new RuntimeException(
                            "Atendimento não encontrado com ID: " + id
                    );
                });

       StatusAtendimento statusAtualizado = atendimentoDto.getStatusAtendimento();


       if(statusAtualizado == StatusAtendimento.CONCLUIDO || statusAtualizado == StatusAtendimento.CANCELADO){
            atendimentoDto.setDataConclusao(LocalDateTime.now());

            log.info("Data de conclusão do atendimento atualizada para: {}", atendimentoExistente.getDataConclusao());
       }

        if(statusAtualizado != null){
             atendimentoExistente.setStatus(statusAtualizado);
            log.info("Status do atendimento atualizado para: {}", atendimentoDto.getStatusAtendimento());
        }

        Cliente cliente = clienteRepository.findById(atendimentoDto.getClienteId())
                .orElseThrow(() -> {
                    log.error("Cliente não foi encontrado: {}", atendimentoDto.getClienteId());
                    return new ClienteNaoEncontradoException(
                            "Cliente não  encontrado com ID: " + atendimentoDto.getClienteId()
                    );
                });
        log.info("Cliente encontrado: {} - {}", cliente.getId(), cliente.getNomeCompleto());

        // Buscar veículo
        Veiculo veiculo = veiculoRepository.findByPlaca(atendimentoDto.getVeiculoPlaca())
                .orElseThrow(() -> {
                    log.error("Placa do veículo não foi encontrada: {}", atendimentoDto.getVeiculoPlaca());
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com a placa: " + atendimentoDto.getVeiculoPlaca()
                    );
                });
        log.info("Veículo encontrado: {} - {}", veiculo.getId(), veiculo.getPlaca());

        // Buscar funcionário
        Funcionario funcionario = funcionarioRepository.findById(atendimentoDto.getFuncionarioId())
                .orElseThrow(() -> {
                    log.error("Funcionário não foi encontrado: {}", atendimentoDto.getFuncionarioId());
                    return new RuntimeException("Funcionário não encontrado com ID: " + atendimentoDto.getFuncionarioId());
                });
        log.info("Funcionário encontrado: {} - {}", funcionario.getId(), funcionario.getNome());

        LocalDateTime previsaoConclusao = atendimentoDto.getDataConclusao();


        atendimentoExistente.setDescricaoServico(atendimentoDto.getDescricaoServico());
        atendimentoExistente.setStatus(statusAtualizado);
        atendimentoExistente.setDataConclusao(previsaoConclusao);
        atendimentoExistente.setCliente(cliente);
        atendimentoExistente.setVeiculo(veiculo);
        atendimentoExistente.setFuncionario(funcionario);

        Atendimento atendimentoAtualizado = atendimentoRepository.save(atendimentoExistente);
        log.info("Atendimento atualizado com sucesso - ID: {}", atendimentoAtualizado.getId());

        AtendimentoDTO atendimentoDTO = new AtendimentoDTO(atendimentoExistente);
        atendimentoDTO.setId(atendimentoExistente.getId());
        atendimentoDTO.setDescricaoServico(atendimentoExistente.getDescricaoServico());
        atendimentoDTO.setStatus(atendimentoExistente.getStatus());
        atendimentoDTO.setDataCadastro(atendimentoExistente.getDataCadastro());
        atendimentoDTO.setDataEntrada(atendimentoExistente.getDataEntrada());
        atendimentoDTO.setDataConclusao(atendimentoExistente.getDataConclusao());
        atendimentoDTO.setCliente(atendimentoExistente.getCliente().getId());
        atendimentoDTO.setFuncionario(atendimentoExistente.getFuncionario().getId());
        atendimentoDTO.setVeiculo(atendimentoExistente.getVeiculo().getPlaca());

        return atendimentoDTO;
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

    public List<AtendimentoDTO> listarTodosAtendimentos() {
        log.info("Listando todos os atendimentos");

        List<Atendimento> atendimentos = atendimentoRepository.findAll();
        log.info("Total de atendimentos encontrados: {}", atendimentos.size());
        return atendimentos.stream()
                .map(AtendimentoDTO::new)
                .collect(Collectors.toList());

    }
}
