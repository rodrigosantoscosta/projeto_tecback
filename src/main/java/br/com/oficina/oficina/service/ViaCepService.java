package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.response.ViaCepResponse;
import br.com.oficina.oficina.exception.CepNaoEncontradoException;
import br.com.oficina.oficina.model.Endereco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ViaCepService {
    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://viacep.com.br/ws";

    public ViaCepResponse consultarCep(String cep) {
        // URL da API ViaCEP com o CEP a ser consultado
        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("viacep.com.br")
                .path("/ws/{cep}/json/")
                .buildAndExpand(cep)
                .toUriString();

        // Consulta o CEP na API ViaCEP
        ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);

        return response;
    }


    public Endereco buscarEConstruirEndereco(String cep, String numero, String complemento) {
        try {
            ViaCepResponse response = consultarCep(cep);

            if (response == null || response.isErro()) {
                throw new CepNaoEncontradoException(cep);
            }

            Endereco endereco = new Endereco();
            endereco.setCep(response.getCep());
            endereco.setLogradouro(response.getLogradouro());
            endereco.setNumero(numero);
            endereco.setComplemento(complemento);
            endereco.setBairro(response.getBairro());
            endereco.setLocalidade(response.getLocalidade());
            endereco.setUf(response.getUf());

            return endereco;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar CEP: " + e.getMessage(), e);
        }
    }
}