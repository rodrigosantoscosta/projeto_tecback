package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.EnderecoBasicoResponse;
import br.com.oficina.oficina.dto.ViaCepResponse;
import br.com.oficina.oficina.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viacep")
@CrossOrigin(origins = "*")
public class ViaCEPController {

    @Autowired
    private ViaCepService viaCepService;

    @GetMapping("/endereco/{cep}")
    public ResponseEntity<EnderecoBasicoResponse> buscarEndereco(@PathVariable String cep) {
        try {
            // Remove caracteres não numéricos do CEP
            String cepLimpo = cep.replaceAll("[^0-9]", "");
            
            // Valida se o CEP tem 8 dígitos
            if (cepLimpo.length() != 8) {
                return ResponseEntity.badRequest().build();
            }
            
            // Consulta o CEP na API ViaCEP
            ViaCepResponse viaCepResponse = viaCepService.consultarCep(cepLimpo);
            
            // Verifica se houve erro na consulta
            if (viaCepResponse == null || viaCepResponse.isErro()) {
                return ResponseEntity.notFound().build();
            }
            
            // Cria a resposta com apenas os campos solicitados
            EnderecoBasicoResponse response = new EnderecoBasicoResponse(
                viaCepResponse.getLogradouro(),
                viaCepResponse.getBairro(),
                viaCepResponse.getLocalidade(),
                viaCepResponse.getUf()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
