package br.com.oficina.oficina.mapper;

import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.model.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper responsável por converter entre a entidade Funcionario e seus respectivos DTOs.
 * 
 * Utiliza o padrão de projeto <b>Mapper</b> (Object Mapper) para realizar a conversão
 * entre objetos de diferentes camadas da aplicação, mantendo um baixo acoplamento
 * entre as camadas de apresentação e domínio.</p>
 * 
 * O MapStruct gera automaticamente a implementação desta interface em tempo de compilação,
 * criando um código otimizado para as operações de mapeamento.</p>
 * 
 * @see <a href="https://mapstruct.org/">MapStruct Documentation</a>
 */
@Mapper(componentModel = "spring") // Indica que o MapStruct deve gerar uma implementação Spring
public interface FuncionarioMapper {

    /**
     * Converte um DTO de cadastro para a entidade Funcionario.
     * 
     * Este método é usado quando um novo funcionário está sendo cadastrado no sistema.
     * Os campos anotados com @Mapping(target = "...", ignore = true) indicam que esses
     * atributos não devem ser mapeados do DTO, pois serão definidos posteriormente.
     *
     * @param dto DTO contendo os dados do funcionário a ser cadastrado
     * @return Entidade Funcionario pronta para persistência
     */
    @Mapping(target = "id", ignore = true)          // O ID é gerado automaticamente pelo banco de dados
    @Mapping(target = "senhaHash", ignore = true)    // A senha é tratada separadamente na service
    @Mapping(target = "dataCadastro", ignore = true) // Definido automaticamente na entidade com @PrePersist
    @Mapping(target = "atendimentos", ignore = true) // Relacionamento que será gerenciado separadamente
    Funcionario toEntity(CadastrarFuncionarioDTO dto);

    /**
     * Converte uma entidade Funcionario para DTO de visualização.
     * 
     * Este método é usado para expor os dados do funcionário na API, garantindo que apenas
     * as informações relevantes sejam retornadas ao cliente.
     *
     * @param funcionario Entidade do domínio a ser convertida
     * @return DTO contendo apenas os dados que devem ser expostos pela API
     */
    FuncionarioDTO toDTO(Funcionario funcionario);


}
