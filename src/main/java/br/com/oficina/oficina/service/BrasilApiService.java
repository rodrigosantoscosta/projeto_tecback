package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.FeriadoNacionalResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BrasilApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BASE_URL = "https://brasilapi.com.br/api/feriados/v1/";


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

}
