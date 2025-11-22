package br.com.oficina.oficina.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Sistema Oficina Mecânica")
                        .version("1.0.0")
                        .description("API REST para gerenciamento de oficina mecânica - " +
                                "Gerenciamento de clientes, veículos e atendimentos")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("contato@oficina.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento")
                ));
    }
}