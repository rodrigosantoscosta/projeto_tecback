-- enderecos
INSERT INTO enderecos (cep, logradouro, numero, complemento, bairro, cidade, estado)
VALUES
    ('58000000', 'Rua das Flores',       '100',  NULL,       'Centro',  'Joao Pessoa', 'PB'),
    ('58010000', 'Av. Epitacio Pessoa',  '2000', 'Apto 301', 'Manaira', 'Joao Pessoa', 'PB'),
    ('58020000', 'Rua Cardoso Vieira',   '45',   NULL,       'Tambau',  'Joao Pessoa', 'PB');

-- clientes
INSERT INTO clientes (id, nome_completo, cpf_cnpj, telefone, email, data_cadastro, endereco_id)
VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Carlos Souza',   '52998224725', '(83) 98765-4321', 'carlos.souza@email.com',   NOW(), 1),
    ('a1000000-0000-0000-0000-000000000002', 'Ana Lima',       '66168841090', '(83) 91234-5678', 'ana.lima@email.com',       NOW(), 2),
    ('a1000000-0000-0000-0000-000000000003', 'Pedro Oliveira', '11144477735', '(83) 99999-1111', 'pedro.oliveira@email.com', NOW(), 3);

-- funcionarios
-- senha: senha123 | hash BCrypt 10 rounds
INSERT INTO funcionarios (id, nome, cpf_cnpj, usuario, senha_hash, cargo, telefone, email, data_cadastro)
VALUES
    ('b2000000-0000-0000-0000-000000000001', 'Admin Admin',     '47593836071', 'admin',           '$2a$10$7cTMxhkkIyh8nczbGuaPSuxGjEmWnigEqEyT2.JZdk2ZBBlu.JUhC', 'Gerente',   '(83) 98000-0001', 'admin@oficina.com',   NOW()),
    ('b2000000-0000-0000-0000-000000000002', 'Joao Mecanico',   '14567903069', 'joao.mecanico',   '$2a$10$7cTMxhkkIyh8nczbGuaPSuxGjEmWnigEqEyT2.JZdk2ZBBlu.JUhC', 'Mecanico',  '(83) 98000-0002', 'joao@oficina.com',    NOW()),
    ('b2000000-0000-0000-0000-000000000003', 'Maria Atendente', '07562078069', 'maria.atendente', '$2a$10$7cTMxhkkIyh8nczbGuaPSuxGjEmWnigEqEyT2.JZdk2ZBBlu.JUhC', 'Atendente', '(83) 98000-0003', 'maria@oficina.com',   NOW());

-- veiculos
INSERT INTO veiculos (id, placa, modelo, marca, ano, cor, quilometragem, data_cadastro, cliente_id)
VALUES
    ('c3000000-0000-0000-0000-000000000001', 'ABC1D23', 'Civic', 'Honda',       2021, 'Preto',  45000.0,  NOW(), 'a1000000-0000-0000-0000-000000000001'),
    ('c3000000-0000-0000-0000-000000000002', 'XYZ9A87', 'Gol',   'Volkswagen', 2019, 'Branco', 82000.0,  NOW(), 'a1000000-0000-0000-0000-000000000002'),
    ('c3000000-0000-0000-0000-000000000003', 'DEF5G67', 'Onix',  'Chevrolet',  2022, 'Prata',  15000.0,  NOW(), 'a1000000-0000-0000-0000-000000000003'),
    ('c3000000-0000-0000-0000-000000000004', 'HIJ3K45', 'Fusca', 'Volkswagen', 1978, 'Azul',  210000.0,  NOW(), 'a1000000-0000-0000-0000-000000000001');

-- atendimentos
-- status válidos: AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO
INSERT INTO atendimentos (id, descricao_servico, status, data_entrada, data_conclusao, data_cadastro, cliente_id, veiculo_id, funcionario_id)
VALUES
    ('d4000000-0000-0000-0000-000000000001', 'Troca de oleo e filtro',         'CONCLUIDO',  '2026-05-01 09:00:00', '2026-05-01 11:00:00', '2026-05-01 09:00:00', 'a1000000-0000-0000-0000-000000000001', 'c3000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000002'),
    ('d4000000-0000-0000-0000-000000000002', 'Revisao de freios dianteiros',   'ANDAMENTO',  '2026-05-27 08:00:00', NULL,                  '2026-05-27 08:00:00', 'a1000000-0000-0000-0000-000000000002', 'c3000000-0000-0000-0000-000000000002', 'b2000000-0000-0000-0000-000000000002'),
    ('d4000000-0000-0000-0000-000000000003', 'Alinhamento e balanceamento',    'AGUARDANDO', '2026-05-28 14:00:00', NULL,                  '2026-05-27 10:00:00', 'a1000000-0000-0000-0000-000000000003', 'c3000000-0000-0000-0000-000000000003', 'b2000000-0000-0000-0000-000000000001'),
    ('d4000000-0000-0000-0000-000000000004', 'Substituicao de correia dentada','AGUARDANDO', '2026-05-27 12:00:00', NULL,                  '2026-05-27 12:00:00', 'a1000000-0000-0000-0000-000000000001', 'c3000000-0000-0000-0000-000000000004', 'b2000000-0000-0000-0000-000000000003');
