const API_BASE_URL = 'http://localhost:8080';


// Funções de validação CPF/CNPJ
function validarCPF(cpf) {
    const cpfLimpo = cpf.replace(/\D/g, '');
    
    if (cpfLimpo.length !== 11) return false;
    if (cpfLimpo.match(/(\d)\1{10}/)) return false;
    
    let soma = 0;
    for (let i = 0; i < 9; i++) {
        soma += parseInt(cpfLimpo.charAt(i)) * (10 - i);
    }
    let resto = soma % 11;
    let primeiroDigito = (resto < 2) ? 0 : 11 - resto;
    
    soma = 0;
    for (let i = 0; i < 10; i++) {
        soma += parseInt(cpfLimpo.charAt(i)) * (11 - i);
    }
    resto = soma % 11;
    let segundoDigito = (resto < 2) ? 0 : 11 - resto;
    
    return (primeiroDigito === parseInt(cpfLimpo.charAt(9))) && 
           (segundoDigito === parseInt(cpfLimpo.charAt(10)));
}

function validarCNPJ(cnpj) {
    const cnpjLimpo = cnpj.replace(/\D/g, '');
    
    if (cnpjLimpo.length !== 14) return false;
    if (cnpjLimpo.match(/(\d)\1{13}/)) return false;
    
    let soma = 0;
    let peso = 5;
    for (let i = 0; i < 12; i++) {
        soma += parseInt(cnpjLimpo.charAt(i)) * peso;
        peso = (peso === 2) ? 9 : peso - 1;
    }
    let resto = soma % 11;
    let primeiroDigito = (resto < 2) ? 0 : 11 - resto;
    
    soma = 0;
    peso = 6;
    for (let i = 0; i < 13; i++) {
        soma += parseInt(cnpjLimpo.charAt(i)) * peso;
        peso = (peso === 2) ? 9 : peso - 1;
    }
    resto = soma % 11;
    let segundoDigito = (resto < 2) ? 0 : 11 - resto;
    
    return (primeiroDigito === parseInt(cnpjLimpo.charAt(12))) && 
           (segundoDigito === parseInt(cnpjLimpo.charAt(13)));
}

function validarCPFouCNPJ(documento) {
    const documentoLimpo = documento.replace(/\D/g, '');
    
    if (documentoLimpo.length === 11) {
        return validarCPF(documento);
    } else if (documentoLimpo.length === 14) {
        return validarCNPJ(documento);
    }
    
    return false;
}

// Funções para aplicar estilos visuais
function marcarCampoComoInvalido(campo) {
    campo.classList.add('invalid');
    
    // Remove a classe após a animação para poder aplicar novamente
    setTimeout(() => {
        campo.classList.remove('invalid');
    }, 3000);
}

// Elementos do DOM
const form = document.getElementById('clienteForm');
const messageDiv = document.getElementById('message');
const buscarCepBtn = document.getElementById('buscarCep');
const limparFormBtn = document.getElementById('limparForm');

// Máscaras para os campos
function aplicarMascaras() {
    // Máscara CPF/CNPJ
    const cpfCnpjInput = document.getElementById('cpfCNPJ');
    cpfCnpjInput.addEventListener('input', function(e) {
        // pega só os dígitos e limita a 14 dígitos
        let digits = e.target.value.replace(/\D/g, '').slice(0, 14);

        let value = digits;
        if (digits.length <= 11) {
            // CPF
            value = digits.replace(/(\d{3})(\d)/, '$1.$2')
                          .replace(/(\d{3})(\d)/, '$1.$2')
                          .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
        } else {
            // CNPJ (formata os até 14 dígitos)
            value = digits.replace(/^(\d{2})(\d)/, '$1.$2')
                          .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
                          .replace(/\.(\d{3})(\d)/, '.$1/$2')
                          .replace(/(\d{4})(\d)/, '$1-$2');
        }

        e.target.value = value;
    });

    // Máscara telefone
    const telefoneInput = document.getElementById('telefone');
    telefoneInput.addEventListener('input', function(e) {
        // pega só os dígitos e limita a 11 dígitos (incluindo celular)
        const digits = e.target.value.replace(/\D/g, '').slice(0, 11);
        let formatted = digits;

        if (digits.length <= 2) {
            formatted = digits;
        } else if (digits.length <= 6) {
            // (AA) NNNN
            formatted = digits.replace(/(\d{2})(\d+)/, '($1) $2');
        } else if (digits.length <= 10) {
            // (AA) NNNN-NNNN
            formatted = digits.replace(/(\d{2})(\d{4})(\d+)/, '($1) $2-$3');
        } else {
            // (AA) 9NNNN-NNNN (celular)
            formatted = digits.replace(/(\d{2})(\d{5})(\d+)/, '($1) $2-$3');
        }

        e.target.value = formatted;
    });

    // Máscara CEP
    const cepInput = document.getElementById('cep');
    cepInput.addEventListener('input', function(e) {
        // pega só os dígitos e limita a 8 dígitos
        const digits = e.target.value.replace(/\D/g, '').slice(0, 8);
        // formata como 00000-000 quando tiver ao menos 6 caracteres
        const formatted = digits.replace(/^(\d{5})(\d)/, '$1-$2');
        e.target.value = formatted;
    });
}

// Buscar endereço por CEP
async function buscarEnderecoPorCEP(cep) {
    const cepLimpo = cep.replace(/\D/g, '');
    
    if (cepLimpo.length !== 8) {
        showMessage('CEP deve ter 8 dígitos', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/viacep/endereco/${cepLimpo}`);
        
        if (!response.ok) {
            throw new Error('CEP não encontrado');
        }
        
        const endereco = await response.json();
        
        // Preencher campos de endereço
        document.getElementById('logradouro').value = endereco.logradouro || '';
        document.getElementById('bairro').value = endereco.bairro || '';
        document.getElementById('cidade').value = endereco.localidade || '';
        document.getElementById('estado').value = endereco.uf || '';
        
        showMessage('Endereço encontrado!', 'success');
        
    } catch (error) {
        showMessage('Erro ao buscar CEP: ' + error.message, 'error');
        limparCamposEndereco();
    }
}

function limparCamposEndereco() {
    document.getElementById('logradouro').value = '';
    document.getElementById('bairro').value = '';
    document.getElementById('cidade').value = '';
    document.getElementById('estado').value = '';
}

// Mostrar mensagens
function showMessage(text, type) {
    messageDiv.textContent = text;
    messageDiv.className = `message ${type}`;
    messageDiv.style.display = 'block';
    
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

// Cadastrar cliente
async function cadastrarCliente(clienteData) {
    try {
        console.log('Enviando dados:', clienteData); // log para debug

        const response = await fetch(`${API_BASE_URL}/clientes`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                nomeCompleto: clienteData.nomeCompleto,
                cpfCNPJ: clienteData.cpfCNPJ.replace(/\D/g, ''), // Remove formatação para validação
                telefone: clienteData.telefone, // manter formatado conforme exemplo
                email: clienteData.email,
                cep: clienteData.cep.replace(/\D/g, ''), // apenas dígitos para CEP
                numero: clienteData.numero,
                complemento: clienteData.complemento || ''
            })
        });

        console.log('Status:', response.status); // log para debug

        if (!response.ok) {
            const errorText = await response.text();
            console.log('Erro retornado:', errorText); // log para debug
            throw new Error(errorText || 'Erro ao cadastrar cliente');
        }

        const responseData = await response.text();
        return responseData;
    } catch (error) {
        console.error('Erro completo:', error); // log para debug
        throw new Error(error.message);
    }
}

// Limpar formulário
function limparFormulario() {
    form.reset();
    limparCamposEndereco();
    showMessage('Formulário limpo!', 'success');
}

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    aplicarMascaras();

    // Buscar CEP
    buscarCepBtn.addEventListener('click', function() {
        const cep = document.getElementById('cep').value;
        if (cep) {
            buscarEnderecoPorCEP(cep);
        } else {
            showMessage('Digite um CEP primeiro', 'error');
        }
    });

    // Auto-buscar CEP quando digitar 8 caracteres
    document.getElementById('cep').addEventListener('blur', function() {
        const cep = this.value.replace(/\D/g, '');
        if (cep.length === 8) {
            buscarEnderecoPorCEP(cep);
        }
    });

    // Limpar formulário (proteção caso botão não exista)
    if (limparFormBtn) {
        limparFormBtn.addEventListener('click', limparFormulario);
    }

    // Submeter formulário
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = new FormData(form);
        const clienteData = {
            nomeCompleto: formData.get('nomeCompleto'),
            cpfCNPJ: document.getElementById('cpfCNPJ').value, // pegar valor formatado
            telefone: document.getElementById('telefone').value, // pegar valor formatado
            email: formData.get('email'),
            cep: formData.get('cep'),
            numero: formData.get('numero'),
            complemento: formData.get('complemento') || ''
        };

        // Validação do CPF/CNPJ antes de enviar
        if (clienteData.cpfCNPJ && clienteData.cpfCNPJ.trim().length > 0) {
            if (!validarCPFouCNPJ(clienteData.cpfCNPJ)) {
                const documentoLimpo = clienteData.cpfCNPJ.replace(/\D/g, '');
                const tipo = documentoLimpo.length === 11 ? 'CPF' : 'CNPJ';
                const cpfCnpjField = document.getElementById('cpfCNPJ');
                
                // Aplicar highlight visual
                marcarCampoComoInvalido(cpfCnpjField);
                showMessage(`${tipo} inválido! Verifique os números digitados.`, 'error');
                cpfCnpjField.focus();
                return;
            }
        }

        try {
            const resultado = await cadastrarCliente(clienteData);
            showMessage('Cliente cadastrado com sucesso! ' + resultado, 'success');
            limparFormulario();
        } catch (error) {
            showMessage(error.message, 'error');
        }
    });
});
