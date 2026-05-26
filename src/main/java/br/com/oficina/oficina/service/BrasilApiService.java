package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.response.BrasilApiCepResponse;
import br.com.oficina.oficina.dto.response.FeriadoNacionalResponse;
import br.com.oficina.oficina.dto.response.ViaCepResponse;
import br.com.oficina.oficina.exception.CepNaoEncontradoException;
import br.com.oficina.oficina.model.Endereco;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BrasilApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BASE_URL = "https://brasilapi.com.br/api";


    public List<FeriadoNacionalResponse> buscarFeriadosPorAno(int ano) {
        List<FeriadoNacionalResponse> list = new ArrayList<>();

        try {
            String url = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("brasilapi.com.br")
                    .path("/api/feriados/v1/{ano}")
                    .buildAndExpand(ano)
                    .toUriString();

            // Realiza a consulta na API Brasil API
            FeriadoNacionalResponse[] feriados = restTemplate.getForObject(url, FeriadoNacionalResponse[].class);

            // Converte o array para uma lista
            list = Arrays.asList(feriados);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar feriados nacionais: " + e.getMessage(), e);
        }

        return list;
    }

    public BrasilApiCepResponse consultarCep(String cep) {
        // URL da API BrasilApi com o CEP a ser consultado
        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("brasilapi.com.br")
                .path("/api/cep/v1/{cep}")
                .buildAndExpand(cep)
                .toUriString();
        // Consulta o CEP na API ViaCEP
        BrasilApiCepResponse response = restTemplate.getForObject(url, BrasilApiCepResponse.class);

        return response;
    }

    public Endereco buscarEConstruirEndereco(String cep, String numero, String complemento) {
        try {
            // Remove qualquer formatação do CEP (hífens, pontos, etc.)
            String cepLimpo = cep.replaceAll("[^0-9]", "");
            
            // Valida se o CEP tem 8 dígitos
            if (cepLimpo.length() != 8) {
                throw new IllegalArgumentException("CEP deve conter 8 dígitos");
            }
            
            BrasilApiCepResponse response = consultarCep(cepLimpo);

            if (response == null || response.getCep() == null) {
                throw new CepNaoEncontradoException(cep);
            }

            // Formata o CEP para o padrão 00000-000
            String cepFormatado = response.getCep().replaceAll("(\\d{5})(\\d{3})", "$1-$2");
            
            Endereco endereco = new Endereco();
            endereco.setCep(cepFormatado);
            endereco.setLogradouro(response.getLogradouro() != null ? response.getLogradouro() : "");
            endereco.setNumero(numero);
            endereco.setComplemento(complemento);
            endereco.setBairro(response.getBairro() != null ? response.getBairro() : "");
            endereco.setLocalidade(response.getLocalidade());
            endereco.setUf(response.getUf());

            return endereco;
        } catch (CepNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar CEP: " + e.getMessage(), e);
        }
    }

}
