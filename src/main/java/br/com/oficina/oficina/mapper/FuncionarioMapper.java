package br.com.oficina.oficina.mapper;

import br.com.oficina.oficina.dto.funcionario.CriarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.model.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FuncionarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senhaHash", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    @Mapping(target = "atendimentos", ignore = true)
    Funcionario toEntity(CriarFuncionarioDTO dto);

    FuncionarioDTO toDTO(Funcionario funcionario);
}
