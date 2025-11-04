package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.FeriadoNacionalResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
public class BrasilApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BASE_URL = "https://brasilapi.com.br/api/feriados/v1/";


    public List<FeriadoNacionalResponse> buscarFeriadosPorAno(int ano) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + ano)
                .toUriString();

        FeriadoNacionalResponse[] feriados = restTemplate.getForObject(url, FeriadoNacionalResponse[].class);
        return Arrays.asList(feriados);

}
