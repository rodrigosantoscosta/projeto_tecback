package br.com.oficina.oficina.config;

import br.com.oficina.oficina.model.Endereco;
import br.com.oficina.oficina.service.ViaCepService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ViaCepService viaCepService() {
        return new ViaCepService(null) {
            @Override
            public Endereco buscarEConstruirEndereco(String cep, String numero, String complemento) {
                Endereco endereco = new Endereco();
                endereco.setCep(cep);
                endereco.setLogradouro("Rua Teste");
                endereco.setNumero(numero);
                endereco.setComplemento(complemento);
                endereco.setBairro("Bairro Teste");
                endereco.setLocalidade("Cidade Teste");
                endereco.setUf("TT");
                return endereco;
            }
        };
    }
}
