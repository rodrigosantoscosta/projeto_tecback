package br.com.oficina.oficina.config;

import br.com.oficina.oficina.dto.CriarFuncionarioDTO;
import br.com.oficina.oficina.dto.FuncionarioDTO;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    @Bean
    public FuncionarioMapper funcionarioMapper() {
        return Mappers.getMapper(FuncionarioMapper.class);
    }

}