package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.FeriadoNacionalResponse;
import br.com.oficina.oficina.service.BrasilApiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feriados")

public class BrasilApiController {

    private final BrasilApiService brasilApiService;

    public BrasilApiService(BrasilApiService brasilApiService) {
        this.brasilApiService = brasilApiService;
    }

    @GetMapping("/{ano}")
    public List<FeriadoNacionalResponse> listarFeriados(@PathVariable int ano) {
        return brasilApiService.buscarFeriadosPorAno(ano);
    }
}
