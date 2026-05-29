-- =====================================================================================
--  OrbitaSafe  ·  Global Solution 2026/1  ·  FIAP  ·  1TDS Agosto
--  FASE 2 — IMPLEMENTACAO SQL  (Oracle Database)
--  Script unico e re-executavel:  DDL + DML + 10 Consultas + 5 Relatorios com JOIN
--
--  Estilo: padrao das aulas de Banco de Dados (cliente/produto/pedido/funcionario).
--  PK numerica cravada manualmente no INSERT. Sem SEQUENCE / TRIGGER / IDENTITY.
--  Tipos: varchar(n), number(p,s), date, char(n).  Constraints sempre nomeadas.
--
--  OBS: Na PRIMEIRA execucao, os 9 DROPs iniciais reportam ORA-00942 (tabela inexistente).
--       Isso e esperado e nao impede a continuacao. A partir da segunda execucao, somem.
-- =====================================================================================


-- =====================================================================================
--  SECAO 1 — DDL  (Data Definition Language)
-- =====================================================================================

-- ----- DROP na ORDEM INVERSA de dependencia (filhas/associativas primeiro) -----------
drop table tb_notificacao cascade constraints;
drop table tb_alerta_modelo cascade constraints;
drop table tb_alerta cascade constraints;
drop table tb_leitura_climatica cascade constraints;
drop table tb_regiao cascade constraints;
drop table tb_cenario_climatico cascade constraints;
drop table tb_modelo_ia cascade constraints;
drop table tb_subprefeitura cascade constraints;
drop table tb_usuario cascade constraints;


-- ----- CREATE na ORDEM DIRETA (pais primeiro, associativas por ultimo) ---------------

-- Tabela de usuarios da plataforma. Senha guarda o hash SHA-256 (64 hex), nunca texto puro.
create table tb_usuario (
                            id_usu        number(5)   constraint usu_id_pk primary key,
                            nm_usu        varchar(80) constraint usu_nm_nn not null,
                            email_usu     varchar(120) constraint usu_email_nn not null
                                constraint usu_email_uk unique,
                            senha_usu     char(64)    constraint usu_senha_nn not null,
                            tp_usu        varchar(20) constraint usu_tp_nn not null,
                            dt_cadastro   date        constraint usu_dtcad_nn not null
);

-- As 32 subprefeituras de Sao Paulo. Tabela de dominio, sem FK.
-- Guarda codigo numerico estavel (usado pela IA), centroide e historico de alagamentos.
create table tb_subprefeitura (
                                  id_subpref       number(5)   constraint subpref_id_pk primary key,
                                  cd_subpref       number(5)   constraint subpref_cd_nn not null
                                 constraint subpref_cd_uk unique,
                                  nm_subpref       varchar(60) constraint subpref_nm_nn not null,
                                  latitude_subpref  number(9,6),
                                  longitude_subpref number(9,6),
                                  qt_alagamento    number(6)   constraint subpref_qtalag_nn not null
);

-- Registro dos modelos de IA: o classificador e o regressor.
create table tb_modelo_ia (
                              id_modelo   number(5)   constraint modelo_id_pk primary key,
                              nm_modelo   varchar(60) constraint modelo_nm_nn not null,
                              tp_modelo   varchar(30) constraint modelo_tp_nn not null,
                              versao_modelo varchar(15) constraint modelo_versao_nn not null
);

-- Cenarios de clima pre-definidos. Alimenta a fonte simulada do Servico de Clima.
-- Mesma estrutura de variaveis climaticas. Sem FK.
create table tb_cenario_climatico (
                                      id_cenario     number(5)    constraint cenario_id_pk primary key,
                                      nm_cenario     varchar(60)  constraint cenario_nm_nn not null,
                                      precipitacao_cenario number(6,2),
                                      umidade_cenario      number(5,2),
                                      pressao_cenario      number(7,2),
                                      vento_cenario        number(6,2),
                                      temperatura_cenario  number(5,2),
                                      umid_solo_cenario    number(5,2),
                                      nivel_cenario  varchar(5)   constraint cenario_nivel_nn not null
                                          constraint cenario_nivel_ck check (nivel_cenario in ('BAIXO','MEDIO','ALTO'))
);

-- Regioes monitoradas. FK para usuario e para subprefeitura.
create table tb_regiao (
                           id_reg        number(6)   constraint reg_id_pk primary key,
                           nm_reg        varchar(80) constraint reg_nm_nn not null,
                           dt_cadastro   date        constraint reg_dtcad_nn not null,
                           fk_usuario_id_usu        number(5) constraint reg_usu_nn not null
                                       constraint reg_usu_fk references tb_usuario,
                           fk_subprefeitura_id_subpref number(5) constraint reg_subpref_nn not null
                                       constraint reg_subpref_fk references tb_subprefeitura
);

-- Leituras climaticas de uma regiao. FK para regiao. Variaveis do Contrato 1.
create table tb_leitura_climatica (
                                      id_leitura    number(7)  constraint leitura_id_pk primary key,
                                      precipitacao_leitura number(6,2) constraint leitura_precip_nn not null,
                                      umidade_leitura      number(5,2),
                                      pressao_leitura      number(7,2),
                                      vento_leitura        number(6,2),
                                      temperatura_leitura  number(5,2),
                                      umid_solo_leitura    number(5,2),
                                      dt_leitura    date       constraint leitura_dt_nn not null,
                                      dia_previsao  number(1)  constraint leitura_diaprev_nn not null
                             constraint leitura_diaprev_ck check (dia_previsao between 0 and 3),
                                      fk_regiao_id_reg  number(6) constraint leitura_reg_nn not null
                                constraint leitura_reg_fk references tb_regiao
);

-- Alertas de risco gerados. FK para regiao e para leitura climatica.
create table tb_alerta (
                           id_alerta     number(7)   constraint alerta_id_pk primary key,
                           nivel_alerta  varchar(5)  constraint alerta_nivel_nn not null
                               constraint alerta_nivel_ck check (nivel_alerta in ('BAIXO','MEDIO','ALTO')),
                           tp_evento     varchar(40) constraint alerta_tpev_nn not null,
                           ds_alerta     varchar(200) constraint alerta_ds_nn not null,
                           dt_alerta     date        constraint alerta_dt_nn not null,
                           fk_regiao_id_reg      number(6) constraint alerta_reg_nn not null
                                    constraint alerta_reg_fk references tb_regiao,
                           fk_leitura_id_leitura number(7) constraint alerta_leitura_nn not null
                                    constraint alerta_leitura_fk references tb_leitura_climatica
);

-- Associativa N:N entre alerta e modelo de IA. PK composta pelas duas FKs.
create table tb_alerta_modelo (
                                  fk_alerta_id_alerta number(7) constraint am_alerta_fk references tb_alerta,
                                  fk_modelo_id_modelo number(5) constraint am_modelo_fk references tb_modelo_ia,
                                  score_modelo  number(5,2),
                                  constraint am_id_pk primary key (fk_alerta_id_alerta, fk_modelo_id_modelo)
);

-- Notificacoes aos usuarios. FK para usuario e para alerta. Estado lida/nao-lida.
create table tb_notificacao (
                                id_notif      number(7)   constraint notif_id_pk primary key,
                                ds_notif      varchar(200) constraint notif_ds_nn not null,
                                dt_notif      date        constraint notif_dt_nn not null,
                                estado_notif  varchar(10) constraint notif_estado_nn not null
                                    constraint notif_estado_ck check (estado_notif in ('LIDA','NAO_LIDA')),
                                fk_usuario_id_usu   number(5) constraint notif_usu_nn not null
                                  constraint notif_usu_fk references tb_usuario,
                                fk_alerta_id_alerta number(7) constraint notif_alerta_nn not null
                                  constraint notif_alerta_fk references tb_alerta
);


-- =====================================================================================
--  SECAO 2 — DML  (Data Manipulation Language)  ·  povoamento  ·  PK manual
--  Insercao na ordem de dependencia (pais antes de filhos).
-- =====================================================================================

-- -------------------------------------------------------------------------------------
--  TB_SUBPREFEITURA  —  as 32 subprefeituras de Sao Paulo
--  ATENCAO: substituir os cd_subpref pelos codigos OFICIAIS do arquivo da equipe.
--           Os codigos abaixo sao PROVISORIOS (sequenciais 101..132).
-- -------------------------------------------------------------------------------------
insert into tb_subprefeitura values (1,  101, 'Perus',                  -23.402500, -46.742500, 12);
insert into tb_subprefeitura values (2,  102, 'Pirituba/Jaragua',       -23.480800, -46.726900, 18);
insert into tb_subprefeitura values (3,  103, 'Freguesia/Brasilandia',  -23.455400, -46.692500, 25);
insert into tb_subprefeitura values (4,  104, 'Casa Verde/Cachoeirinha',-23.490200, -46.661600, 30);
insert into tb_subprefeitura values (5,  105, 'Santana/Tucuruvi',       -23.466000, -46.625600, 22);
insert into tb_subprefeitura values (6,  106, 'Jacana/Triboada',        -23.460900, -46.585300, 15);
insert into tb_subprefeitura values (7,  107, 'Vila Maria/Vila Guilherme',-23.510400,-46.589700, 28);
insert into tb_subprefeitura values (8,  108, 'Lapa',                   -23.527800, -46.705600, 20);
insert into tb_subprefeitura values (9,  109, 'Se',                     -23.550500, -46.633300, 45);
insert into tb_subprefeitura values (10, 110, 'Butanta',                -23.571100, -46.708900, 17);
insert into tb_subprefeitura values (11, 111, 'Pinheiros',              -23.567000, -46.701900, 19);
insert into tb_subprefeitura values (12, 112, 'Vila Mariana',           -23.589800, -46.634400, 23);
insert into tb_subprefeitura values (13, 113, 'Ipiranga',               -23.591800, -46.610700, 33);
insert into tb_subprefeitura values (14, 114, 'Mooca',                  -23.557900, -46.600200, 27);
insert into tb_subprefeitura values (15, 115, 'Aricanduva/Formosa/Carrao',-23.564400,-46.512200, 21);
insert into tb_subprefeitura values (16, 116, 'Itaquera',               -23.540300, -46.456600, 35);
insert into tb_subprefeitura values (17, 117, 'Guaianases',             -23.540700, -46.413900, 29);
insert into tb_subprefeitura values (18, 118, 'Vila Prudente',          -23.582700, -46.566400, 24);
insert into tb_subprefeitura values (19, 119, 'Sapopemba',              -23.598900, -46.515400, 31);
insert into tb_subprefeitura values (20, 120, 'Sao Mateus',             -23.609300, -46.475100, 38);
insert into tb_subprefeitura values (21, 121, 'Cidade Tiradentes',      -23.595700, -46.403300, 26);
insert into tb_subprefeitura values (22, 122, 'Itaim Paulista',         -23.501400, -46.397700, 34);
insert into tb_subprefeitura values (23, 123, 'Sao Miguel Paulista',    -23.498600, -46.444200, 32);
insert into tb_subprefeitura values (24, 124, 'Ermelino Matarazzo',     -23.500600, -46.479700, 20);
insert into tb_subprefeitura values (25, 125, 'Penha',                  -23.524600, -46.539400, 28);
insert into tb_subprefeitura values (26, 126, 'Vila Mariana Sul',       -23.620100, -46.640800, 16);
insert into tb_subprefeitura values (27, 127, 'Santo Amaro',            -23.654800, -46.708900, 40);
insert into tb_subprefeitura values (28, 128, 'Cidade Ademar',          -23.682200, -46.659200, 37);
insert into tb_subprefeitura values (29, 129, 'Capela do Socorro',      -23.715600, -46.703900, 42);
insert into tb_subprefeitura values (30, 130, 'Parelheiros',            -23.825700, -46.728600, 39);
insert into tb_subprefeitura values (31, 131, 'M Boi Mirim',            -23.690400, -46.755800, 44);
insert into tb_subprefeitura values (32, 132, 'Campo Limpo',            -23.647500, -46.758900, 36);

-- -------------------------------------------------------------------------------------
--  TB_USUARIO  —  senhas como hash SHA-256 (64 chars hex, EXATAMENTE).
--  Hashes correspondem as senhas '1', '2', '3', '4', '5', '6' (apenas teste).
-- -------------------------------------------------------------------------------------
insert into tb_usuario values (1, 'Ana Souza',     'ana@orbita.com',
                               '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', 'COMUM', date '2026-03-01');
insert into tb_usuario values (2, 'Bruno Lima',    'bruno@orbita.com',
                               'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'COMUM', date '2026-03-02');
insert into tb_usuario values (3, 'Carla Mendes',  'carla@orbita.com',
                               '4e07408562bedb8b60ce05c1decfe3ad16b72230967de01f640b7e4729b49fce', 'ADMIN', date '2026-03-03');
insert into tb_usuario values (4, 'Diego Alves',   'diego@orbita.com',
                               '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a', 'COMUM', date '2026-03-04');
insert into tb_usuario values (5, 'Elaine Castro', 'elaine@orbita.com',
                               'ef2d127de37b942baad06145e54b0c619a1f22327b2ebbcfbec78f5564afe39d', 'COMUM', date '2026-03-05');
insert into tb_usuario values (6, 'Fabio Rocha',   'fabio@orbita.com',
                               'e7f6c011776e8db7cd330b54174fd76f7d0216b612387a5ffcfb81e6f0919683', 'COMUM', date '2026-03-06');

-- -------------------------------------------------------------------------------------
--  TB_MODELO_IA  —  classificador e regressor
-- -------------------------------------------------------------------------------------
insert into tb_modelo_ia values (1, 'Classificador de Risco Climatico', 'CLASSIFICACAO', 'v1.0');
insert into tb_modelo_ia values (2, 'Regressor de Risco de Alagamento', 'REGRESSAO',     'v1.0');

-- -------------------------------------------------------------------------------------
--  TB_CENARIO_CLIMATICO  —  cobrindo risco baixo / medio / alto
-- -------------------------------------------------------------------------------------
insert into tb_cenario_climatico values (1, 'Tempo Seco Estavel',     2.50,  45.00, 1018.00, 10.00, 24.50, 20.00, 'BAIXO');
insert into tb_cenario_climatico values (2, 'Chuva Leve',            12.00,  70.00, 1012.00, 18.00, 22.00, 45.00, 'BAIXO');
insert into tb_cenario_climatico values (3, 'Chuva Moderada',        38.00,  82.00, 1008.00, 28.00, 20.50, 65.00, 'MEDIO');
insert into tb_cenario_climatico values (4, 'Frente Fria Ativa',     55.00,  88.00, 1004.00, 35.00, 18.00, 78.00, 'MEDIO');
insert into tb_cenario_climatico values (5, 'Temporal Intenso',      95.00,  95.00,  998.00, 52.00, 19.00, 92.00, 'ALTO');
insert into tb_cenario_climatico values (6, 'Tempestade Severa',    130.00,  97.00,  994.00, 65.00, 18.50, 96.00, 'ALTO');

-- -------------------------------------------------------------------------------------
--  TB_REGIAO  —  vinculadas a usuarios e subprefeituras
-- -------------------------------------------------------------------------------------
insert into tb_regiao values (1,  'Casa - Vila Mariana',   date '2026-03-10', 1, 12);
insert into tb_regiao values (2,  'Trabalho - Se',         date '2026-03-10', 1, 9);
insert into tb_regiao values (3,  'Apartamento - Santana', date '2026-03-11', 2, 5);
insert into tb_regiao values (4,  'Casa dos Pais - Lapa',  date '2026-03-12', 3, 8);
insert into tb_regiao values (5,  'Escritorio - Pinheiros',date '2026-03-12', 3, 11);
insert into tb_regiao values (6,  'Casa - Itaquera',       date '2026-03-13', 4, 16);
insert into tb_regiao values (7,  'Sitio - Parelheiros',   date '2026-03-14', 5, 30);
insert into tb_regiao values (8,  'Casa - Santo Amaro',    date '2026-03-15', 5, 27);
insert into tb_regiao values (9,  'Loja - Mooca',          date '2026-03-16', 6, 14);
insert into tb_regiao values (10, 'Casa - Campo Limpo',    date '2026-03-17', 6, 32);

-- -------------------------------------------------------------------------------------
--  TB_LEITURA_CLIMATICA  —  variando valores e dias de previsao (0=hoje, 1..3)
-- -------------------------------------------------------------------------------------
insert into tb_leitura_climatica values (1,   3.20, 48.00, 1017.00, 11.00, 25.00, 22.00, date '2026-04-01', 0, 1);
insert into tb_leitura_climatica values (2,  14.50, 72.00, 1011.00, 19.00, 21.50, 50.00, date '2026-04-01', 1, 1);
insert into tb_leitura_climatica values (3,  40.00, 83.00, 1007.00, 29.00, 20.00, 67.00, date '2026-04-02', 0, 2);
insert into tb_leitura_climatica values (4,  98.00, 96.00,  997.00, 54.00, 19.00, 93.00, date '2026-04-02', 0, 3);
insert into tb_leitura_climatica values (5,   1.00, 40.00, 1019.00,  9.00, 26.00, 18.00, date '2026-04-03', 0, 4);
insert into tb_leitura_climatica values (6,  56.00, 89.00, 1003.00, 36.00, 18.50, 80.00, date '2026-04-03', 1, 5);
insert into tb_leitura_climatica values (7, 120.00, 97.00,  995.00, 60.00, 18.00, 95.00, date '2026-04-04', 0, 6);
insert into tb_leitura_climatica values (8,  10.00, 68.00, 1013.00, 16.00, 22.50, 42.00, date '2026-04-04', 1, 7);
insert into tb_leitura_climatica values (9,  35.00, 80.00, 1009.00, 26.00, 20.50, 63.00, date '2026-04-05', 0, 8);
insert into tb_leitura_climatica values (10,  5.50, 52.00, 1016.00, 13.00, 24.00, 28.00, date '2026-04-05', 0, 1);
insert into tb_leitura_climatica values (11, 72.00, 92.00, 1001.00, 44.00, 18.50, 85.00, date '2026-04-06', 0, 2);
insert into tb_leitura_climatica values (12, 22.00, 76.00, 1010.00, 22.00, 21.00, 55.00, date '2026-04-06', 1, 3);
insert into tb_leitura_climatica values (13,  2.00, 43.00, 1018.00, 10.00, 25.50, 20.00, date '2026-04-07', 0, 6);
insert into tb_leitura_climatica values (14, 88.00, 94.00,  999.00, 50.00, 19.00, 90.00, date '2026-04-07', 0, 7);
insert into tb_leitura_climatica values (15, 18.00, 74.00, 1012.00, 20.00, 21.50, 48.00, date '2026-04-08', 0, 9);
insert into tb_leitura_climatica values (16, 45.00, 85.00, 1006.00, 31.00, 20.00, 70.00, date '2026-04-08', 1, 10);
insert into tb_leitura_climatica values (17, 60.00, 90.00, 1002.00, 38.00, 19.50, 82.00, date '2026-04-09', 0, 8);
insert into tb_leitura_climatica values (18,  8.00, 60.00, 1014.00, 15.00, 23.00, 38.00, date '2026-04-09', 0, 4);

-- -------------------------------------------------------------------------------------
--  TB_ALERTA  —  cobrindo os 3 niveis. Cada alerta aponta para regiao + leitura.
-- -------------------------------------------------------------------------------------
insert into tb_alerta values (1,  'BAIXO', 'Chuva Fraca',    'Risco baixo: chuva leve sem impacto previsto.',       date '2026-04-01', 1, 1);
insert into tb_alerta values (2,  'BAIXO', 'Chuva Fraca',    'Risco baixo: umidade moderada, sem alerta.',          date '2026-04-01', 1, 2);
insert into tb_alerta values (3,  'MEDIO', 'Alagamento',     'Risco medio: possivel acumulo de agua em vias.',      date '2026-04-02', 2, 3);
insert into tb_alerta values (4,  'ALTO',  'Temporal',       'Risco alto: temporal intenso, evite deslocamentos.',  date '2026-04-02', 2, 4);
insert into tb_alerta values (5,  'BAIXO', 'Tempo Estavel',  'Risco baixo: tempo seco e estavel.',                  date '2026-04-03', 3, 5);
insert into tb_alerta values (6,  'ALTO',  'Vendaval',       'Risco alto: ventos fortes e queda de arvores.',       date '2026-04-03', 4, 6);
insert into tb_alerta values (7,  'ALTO',  'Alagamento',     'Risco alto: alagamento severo previsto na regiao.',   date '2026-04-04', 4, 7);
insert into tb_alerta values (8,  'BAIXO', 'Chuva Fraca',    'Risco baixo: chuva passageira.',                      date '2026-04-04', 5, 8);
insert into tb_alerta values (9,  'MEDIO', 'Alagamento',     'Risco medio: solo encharcado, atencao redobrada.',    date '2026-04-05', 6, 9);
insert into tb_alerta values (10, 'BAIXO', 'Tempo Estavel',  'Risco baixo: condicoes normais.',                     date '2026-04-05', 7, 10);
insert into tb_alerta values (11, 'ALTO',  'Temporal',       'Risco alto: tempestade com raios e granizo.',         date '2026-04-06', 8, 11);
insert into tb_alerta values (12, 'MEDIO', 'Chuva Moderada', 'Risco medio: chuva continua ao longo do dia.',        date '2026-04-06', 9, 12);
insert into tb_alerta values (13, 'ALTO',  'Alagamento',     'Risco alto: ponto critico de alagamento ativo.',      date '2026-04-07', 10, 14);
insert into tb_alerta values (14, 'MEDIO', 'Chuva Moderada', 'Risco medio: precipitacao acumulada relevante.',      date '2026-04-09', 8, 17);

-- -------------------------------------------------------------------------------------
--  TB_ALERTA_MODELO  —  associacoes N:N (quais modelos participaram de cada alerta)
-- -------------------------------------------------------------------------------------
insert into tb_alerta_modelo values (1,  1, 0.15);
insert into tb_alerta_modelo values (2,  1, 0.20);
insert into tb_alerta_modelo values (3,  1, 0.55);
insert into tb_alerta_modelo values (3,  2, 0.60);
insert into tb_alerta_modelo values (4,  1, 0.88);
insert into tb_alerta_modelo values (4,  2, 0.91);
insert into tb_alerta_modelo values (6,  1, 0.85);
insert into tb_alerta_modelo values (7,  1, 0.92);
insert into tb_alerta_modelo values (7,  2, 0.95);
insert into tb_alerta_modelo values (9,  2, 0.58);
insert into tb_alerta_modelo values (11, 1, 0.87);
insert into tb_alerta_modelo values (11, 2, 0.83);
insert into tb_alerta_modelo values (13, 2, 0.94);

-- -------------------------------------------------------------------------------------
--  TB_NOTIFICACAO  —  algumas lidas, outras nao. So risco MEDIO/ALTO gera notificacao.
-- -------------------------------------------------------------------------------------
insert into tb_notificacao values (1, 'Alerta MEDIO em Trabalho - Se: acumulo de agua.',     date '2026-04-02', 'LIDA',     1, 3);
insert into tb_notificacao values (2, 'Alerta ALTO em Trabalho - Se: temporal intenso.',     date '2026-04-02', 'NAO_LIDA', 1, 4);
insert into tb_notificacao values (3, 'Alerta ALTO em Casa dos Pais - Lapa: vendaval.',      date '2026-04-03', 'NAO_LIDA', 3, 6);
insert into tb_notificacao values (4, 'Alerta ALTO em Casa dos Pais - Lapa: alagamento.',    date '2026-04-04', 'LIDA',     3, 7);
insert into tb_notificacao values (5, 'Alerta MEDIO em Casa - Itaquera: solo encharcado.',   date '2026-04-05', 'NAO_LIDA', 4, 9);
insert into tb_notificacao values (6, 'Alerta ALTO em Casa - Santo Amaro: tempestade.',      date '2026-04-06', 'NAO_LIDA', 5, 11);
insert into tb_notificacao values (7, 'Alerta MEDIO em Loja - Mooca: chuva continua.',       date '2026-04-06', 'LIDA',     6, 12);
insert into tb_notificacao values (8, 'Alerta ALTO em Casa - Campo Limpo: ponto critico.',   date '2026-04-07', 'NAO_LIDA', 6, 13);

commit;


-- =====================================================================================
--  SECAO 3 — AS 10 CONSULTAS SQL
-- =====================================================================================

-- 01) Quantidade de alertas por regiao (COUNT + GROUP BY).
select fk_regiao_id_reg, count(id_alerta) qt_alertas
from tb_alerta
group by fk_regiao_id_reg
order by 2 desc;

-- 02) Media do score por modelo de IA (AVG + GROUP BY).
select fk_modelo_id_modelo, round(avg(score_modelo),2) media_score
from tb_alerta_modelo
group by fk_modelo_id_modelo;

-- 03) Regioes com mais de 1 alerta, ordenadas (HAVING + ORDER BY).
select fk_regiao_id_reg, count(id_alerta) qt_alertas
from tb_alerta
group by fk_regiao_id_reg
having count(id_alerta) > 1
order by 2 desc;

-- 04) Quantidade de regioes que cada usuario monitora (COUNT + GROUP BY).
select fk_usuario_id_usu, count(id_reg) qt_regioes
from tb_regiao
group by fk_usuario_id_usu
order by 1;

-- 05) Subprefeitura com o maior historico de alagamentos (MAX via subquery).
select nm_subpref, qt_alagamento
from tb_subprefeitura
where qt_alagamento in (select max(qt_alagamento) from tb_subprefeitura);

-- 06) Total de notificacoes nao lidas por usuario (COUNT + WHERE).
select fk_usuario_id_usu, count(id_notif) qt_nao_lidas
from tb_notificacao
where estado_notif = 'NAO_LIDA'
group by fk_usuario_id_usu
order by 2 desc;

-- 07) Leituras climaticas com precipitacao acima de 50 mm (WHERE + ORDER BY).
select id_leitura, precipitacao_leitura, dt_leitura
from tb_leitura_climatica
where precipitacao_leitura > 50
order by 2 desc;

-- 08) Alertas gerados em um intervalo de datas (BETWEEN).
select id_alerta, nivel_alerta, tp_evento, dt_alerta
from tb_alerta
where dt_alerta between date '2026-04-02' and date '2026-04-05'
order by dt_alerta;

-- 09) Calculo derivado: percentual de alertas de risco ALTO sobre o total.
select count(case when nivel_alerta = 'ALTO' then 1 end) qt_alto,
       count(id_alerta) qt_total,
       round(count(case when nivel_alerta = 'ALTO' then 1 end) * 100 / count(id_alerta), 2) perc_alto
from tb_alerta;

-- 10) Resumo das variaveis de precipitacao das leituras (SUM, AVG, MAX, MIN).
select sum(precipitacao_leitura) soma_precip,
       round(avg(precipitacao_leitura),2) media_precip,
       max(precipitacao_leitura) maior_precip,
       min(precipitacao_leitura) menor_precip
from tb_leitura_climatica;


-- =====================================================================================
--  SECAO 4 — OS 5 RELATORIOS COM JOIN
--  Distribuicao: 2 INNER  +  2 LEFT "somente diferenca"  +  1 RIGHT "somente diferenca"
-- =====================================================================================

-- R1) INNER JOIN — usuarios e as regioes que eles monitoram (so com correspondencia).
select nm_usu, nm_reg from tb_regiao
                               inner join tb_usuario on tb_usuario.id_usu = tb_regiao.fk_usuario_id_usu
order by nm_usu;

-- R2) INNER JOIN — alertas com a regiao e a subprefeitura de cada um (3 tabelas).
select id_alerta, nivel_alerta, nm_reg, nm_subpref from tb_alerta
                                                            inner join tb_regiao on tb_regiao.id_reg = tb_alerta.fk_regiao_id_reg
                                                            inner join tb_subprefeitura on tb_subprefeitura.id_subpref = tb_regiao.fk_subprefeitura_id_subpref
order by id_alerta;

-- R3) LEFT JOIN "somente diferenca" — regioes que NAO possuem nenhuma leitura climatica.
select nm_reg, id_leitura from tb_regiao
                                   left join tb_leitura_climatica on tb_regiao.id_reg = tb_leitura_climatica.fk_regiao_id_reg
where tb_leitura_climatica.id_leitura is null;

-- R4) LEFT JOIN "somente diferenca" — alertas que NAO geraram notificacao (risco baixo).
select id_alerta, nivel_alerta, id_notif from tb_alerta
                                                  left join tb_notificacao on tb_alerta.id_alerta = tb_notificacao.fk_alerta_id_alerta
where tb_notificacao.id_notif is null;

-- R5) RIGHT JOIN "somente diferenca" — subprefeituras SEM nenhuma regiao cadastrada.
select nm_reg, nm_subpref from tb_regiao
                                   right join tb_subprefeitura on tb_subprefeitura.id_subpref = tb_regiao.fk_subprefeitura_id_subpref
where tb_regiao.id_reg is null;