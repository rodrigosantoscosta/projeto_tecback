package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.ViaCepResponse;
import br.com.oficina.oficina.model.Endereco;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ViaCepService {
    private final RestTemplate restTemplate;

    public ViaCepService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ViaCepResponse consultarCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        return restTemplate.getForObject(url, ViaCepResponse.class);
    }

    public Endereco buscarEConstruirEndereco(String cep, String numero, String complemento) {
        ViaCepResponse response = consultarCep(cep);

        Endereco endereco = new Endereco();
        endereco.setCep(response.getCep());
        endereco.setLogradouro(response.getLogradouro());
        endereco.setNumero(numero);
        endereco.setComplemento(complemento);
        endereco.setBairro(response.getBairro());
        endereco.setLocalidade(response.getLocalidade());
        endereco.setUf(response.getUf());

        return endereco;
    }
}
