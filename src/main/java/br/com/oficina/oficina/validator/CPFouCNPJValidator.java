package br.com.oficina.oficina.validator;

import br.com.oficina.oficina.validator.annotation.CPFouCNPJ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFouCNPJValidator implements ConstraintValidator<CPFouCNPJ, String> {

    @Override
    public void initialize(CPFouCNPJ constraintAnnotation) {
        // Inicialização se necessário
    }

    @Override
    public boolean isValid(String documento, ConstraintValidatorContext context) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }

        // Remove qualquer caractere não numérico
        String documentoLimpo = documento.replaceAll("\\D", "");

        // Verificar se tem 11 ou 14 digitos
        if (documentoLimpo.length() == 11) {
            return validarCPF(documentoLimpo);
        } else if (documentoLimpo.length() == 14) {
            return validarCNPJ(documentoLimpo);
        } else {
            return false;
        }
    }

    private boolean validarCPF(String cpf) {
        // Verifica CPF com dígitos repetidos (ex: 111.111.111-11)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Cálculo do primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int resto = soma % 11;
            int primeiroDigito = (resto < 2) ? 0 : 11 - resto;

            // Cálculo do segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            resto = soma % 11;
            int segundoDigito = (resto < 2) ? 0 : 11 - resto;

            //Verifica digitos verificadores
            return (primeiroDigito == Character.getNumericValue(cpf.charAt(9))) &&
                    (segundoDigito == Character.getNumericValue(cpf.charAt(10)));

        } catch (Exception e) {
            return false;
        }
    }

    private boolean validarCNPJ(String cnpj) {
        // Verifica CNPJ com dígitos repetidos (ex: 11.111.111/1111-11)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Cálculo do primeiro dígito verificador
            int soma = 0;
            int peso = 5;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso;
                peso = (peso == 2) ? 9 : peso - 1;
            }
            int resto = soma % 11;
            int primeiroDigito = (resto < 2) ? 0 : 11 - resto;

            // Cálculo do segundo dígito verificador
            soma = 0;
            peso = 6;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso;
                peso = (peso == 2) ? 9 : peso - 1;
            }
            resto = soma % 11;
            int segundoDigito = (resto < 2) ? 0 : 11 - resto;

            //Verifica digitos verificadores
            return (primeiroDigito == Character.getNumericValue(cnpj.charAt(12))) &&
                    (segundoDigito == Character.getNumericValue(cnpj.charAt(13)));

        } catch (Exception e) {
            return false;
        }
    }
}
