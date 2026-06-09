# OrbitaSafe — Documento Mestre (referência técnica de apresentação)

> Uso privado (Diego). Referência densa para consulta antes e durante a banca.
> Companion do `README.md` (visão pública) e do `docs/contrato-api.md` (referência do front).
> Aqui não se explica o que é DAO/BO/JAX-RS — assume-se domínio. Foco: o que ESTE projeto faz, onde, e por quê.
> Citações no formato `arquivo:linha` apontam o estado atual do código (pós-revisão pré-deploy).

---

## Seção 1 — Resumo executivo

OrbitaSafe é uma API REST de monitoramento e alerta de risco climático para São Paulo, núcleo da Global Solution 2026/1 (FIAP, 1TDS Agosto, tema Economia Espacial; startup fictícia Orbit Analytics). O cidadão cadastra regiões de interesse (uma das 32 subprefeituras de SP); a cada análise o sistema lê o clima daquela região, trata os dados, submete a dois modelos de IA (classificação de risco + regressão de score de alagamento) e materializa o resultado como um alerta no histórico — gerando notificação ao usuário quando o risco é MEDIO ou ALTO.

A API Java (Quarkus) é o orquestrador único: o front não toca banco nem IA. Toda a regra de transformação do dado bruto de clima para o formato dos modelos (o "Contrato 1", 8 variáveis) vive no Java, antes da chamada HTTP à Flask. Fonte de clima e implementação de IA são intercambiáveis por variável de ambiente, o que garante demonstração offline confiável sem depender de rede.

Stack: Java 21 · Quarkus 3.34.6 · Oracle Database (JDBC manual) · Apache HttpClient 4.5.14 · Gson 2.13.2 · serialização JSON via quarkus-rest-jackson.

Números do projeto:
- 51 classes Java (9 entities, 9 DAOs, 4 BOs, 11 services, 6 Resources, 1 DTO, 5 exceptions, 1 ConexaoFactory, 4 mains de teste, 1 CorsFilter).
- 19 endpoints REST em 6 Resources.
- 9 tabelas Oracle, ~109 registros de seed (acima do mínimo de 50 do edital).
- Banco: 10 consultas + 5 relatórios com JOIN (2 INNER, 2 LEFT só-diferença, 1 RIGHT só-diferença).
- 29 requisições na coleção Insomnia (8 pastas); 26 no `requests.http`.
- 9 variáveis de ambiente (3 obrigatórias, 6 opcionais com default).

Cinco decisões de maior peso:
1. JDBC manual com PreparedStatement, sem JPA/Hibernate (controle total do SQL, padrão da disciplina).
2. Instanciação com `new`, sem `@Inject`/CDI (padrão das aulas).
3. Autenticação stateless com hash SHA-256 nativo, sem JWT e sem BCrypt.
4. Fonte de clima (`ServicoClima`) e IA (`IaService`) atrás de interface + Factory + env var.
5. Conexão Oracle aberta e fechada por método no DAO (evita vazar sessão em servidor de vida longa).

Duas limitações conhecidas (defensáveis):
1. SHA-256 sem salt — vulnerável a rainbow table; em produção real seria BCrypt+salt.
2. Sem pool e sob Render Free Tier: 1 instância, hibernação após inatividade (cold start ~30s).

Estado atual: API em deploy no Render (modo JVM), rodando no default `IA_MODO=MOCK` + `FONTE_CLIMA=SIMULADO` — fluxo completo funciona sem a Flask. A Flask de IA e o front (Vercel) são projetos separados; a Flask ainda não está publicada (por isso o default MOCK). Localmente, o pipeline inteiro roda offline contra o Oracle da FIAP.

Fora do escopo (decisões de não-fazer, úteis se a banca cobrar): sem JWT/refresh token; sem BCrypt; sem JPA/Hibernate/Panache; sem `@Inject`/CDI ativo; sem Bean Validation (validação por `if` no BO); sem SMS/e-mail/push (notificação é interna ao sistema); sem IA generativa (são dois modelos sklearn, classificação + regressão); cobertura só São Paulo; sem previsão de longo prazo (mínimo: hoje). São limites do Documento Base, não omissões de implementação.

---

## Seção 2 — O problema e a solução

Problema. São Paulo sofre com alagamentos e eventos extremos localizados: a chuva que alaga a Sé pode não tocar Santana. Falta ao cidadão um alerta antecipado e por região de interesse — não um boletim genérico da cidade, mas "a SUA região está sob risco ALTO de alagamento agora". O recorte por subprefeitura (32 unidades administrativas reais) dá granularidade espacial sem cair em geolocalização arbitrária.

Solução. Pipeline determinístico disparado pelo usuário:

```
cadastra/seleciona região (subprefeitura)
      → sistema lê clima da região (Open-Meteo real OU cenário simulado do banco)
      → trata o dado bruto: janela de precipitação de 6h, hora atual, mês, código da subprefeitura
      → monta o Contrato 1 (8 variáveis) e chama a IA (Flask real OU mock interno)
      → IA devolve risco_geral (Baixo/Medio/Alto) + score_alagamento
      → grava SEMPRE um alerta no histórico da região (mesmo risco BAIXO)
      → registra a associação alerta×modelo
      → SE risco MEDIO/ALTO: cria notificação na caixa de entrada do usuário
```

Quem usa. Cidadãos cadastrados monitorando suas regiões (casa, trabalho, casa dos pais). A sessão é simulada: o front guarda o `idUsu` após o login e o envia onde a ação precisa saber de quem é. Há um tipo de usuário (`tp_usu`, ex. COMUM/ADMIN no seed) mas não há controle de acesso por rota nesta versão acadêmica.

Por que cada componente existe:
- API Java: orquestra o fluxo, persiste, trata o dado climático, fala com a IA. Centraliza a regra de negócio.
- IA Flask (serviço isolado): classifica risco e regride score. Baixo acoplamento — o Java não sabe como os modelos funcionam, só o contrato HTTP.
- Oracle: persistência das 9 entidades; é também entrega da disciplina de Banco de Dados.
- Front React: consome só a API Java. Não acessa banco nem IA.

Distinção crítica (cai na banca): alerta é registro permanente de toda análise (inclusive BAIXO), pertence à região, é histórico. Notificação é aviso ao usuário, criada só para MEDIO/ALTO, pertence ao usuário, tem estado LIDA/NAO_LIDA. Materializado em `AnaliseRiscoBo.java:86` (alerta sempre) vs `AnaliseRiscoBo.java:99` (notificação condicional).

---

## Seção 3 — Arquitetura

### 3.1 Visão geral

```
                         .env / Render env vars
                                  |
                                  v
 [Front React/Vercel] --HTTP/JSON--> [ API Java · Quarkus 3.34.6 ]
       (consome só a API)                 |   |   |
                                          |   |   +--JDBC manual--> [ Oracle DB (9 tabelas) ]
                                          |   |
                  FONTE_CLIMA=OPEN_METEO  |   +--HTTP GET--------> [ Open-Meteo API ] (clima real)
                                          |
                  IA_MODO=REAL            +------HTTP POST-------> [ Flask IA ] (predição)

   Defaults de demo (offline):  FONTE_CLIMA=SIMULADO (cenários do banco)  +  IA_MODO=MOCK (avaliador interno)
```

A API é o único ponto que conversa com banco, Open-Meteo e Flask. As duas integrações externas (clima e IA) têm, cada uma, uma implementação real e uma de contorno, selecionadas em runtime por env var — daí o sistema demonstrar de ponta a ponta sem rede.

### 3.2 Camadas do back-end

| Camada | Pacote | Responsabilidade | Não faz | Arqs |
|---|---|---|---|---|
| Entities | `entities/` | POJOs espelhando as tabelas (atributos privados, 2 construtores, getters/setters, toString) | nenhuma anotação JPA; nenhuma lógica | 9 |
| Conexões | `conexoes/` | Abrir conexão JDBC com Oracle a partir de env vars | nenhuma regra; nenhum pool | 1 |
| DAO | `dao/` | SQL + PreparedStatement; mapeia ResultSet por nome de coluna; abre/fecha conexão por método | regra de negócio; validação | 9 |
| BO | `bo/` | Validações, hash, orquestração entre DAOs e services | acesso direto a banco | 4 |
| Services | `services/` | Integração externa (Open-Meteo, Flask) e POJOs de transporte; interfaces + Factory | persistência | 11 |
| Resources | `resources/` | Entrada HTTP (JAX-RS), traduz para chamada de BO, devolve Response/status | regra de negócio | 6 |
| DTO | `dto/` | Moldar saída onde a entity não pode ir crua (esconder senha) | lógica | 1 |
| Exceptions | `exceptions/` | Exceções de domínio + ExceptionMapper central | — | 5 |
| Raiz | (pacote raiz) | `CorsFilter` (ContainerResponseFilter) | — | 1 |

Padrões fixos por camada: Resource instancia BO com `new` em campo de instância, zero `@Inject` (ex. `UsuarioResource.java:21`, `RegiaoResource.java:19-20`). DAO abre `new ConexaoFactory().conexao()` no início de cada método e fecha em `finally` (ex. `UsuarioDao.java:14` e `:29-32`). BO concentra `if`-validações e lança exceções de domínio (ex. `UsuarioBo.java:17-26`).

### 3.3 Comunicação entre camadas

Fluxo em uma direção: `Resource → BO → DAO → ConexaoFactory → Oracle`. Os services são consumidos pelo BO (especificamente o `AnaliseRiscoBo`), nunca pelo Resource. Exceções fluem de baixo para cima sem try/catch nos Resources: BO/DAO lançam, o método do Resource só declara `throws`, e o `OrbitaSafeExceptionMapper` (`exceptions/OrbitaSafeExceptionMapper.java:9`) captura `RuntimeException` e traduz para o status HTTP + corpo `{ "erro": ... }`. Exceção a esse fluxo: as listagens de notificação por usuário, por pragmatismo, chamam o DAO direto do Resource (`UsuarioResource.java:68,78`) — não há `NotificacaoBo` (ver Q&A 16).

---

## Seção 4 — Fluxo completo de uma análise de risco

Esta é a espinha dorsal. Descrição passo a passo de `POST /regioes/{id}/analisar`. O mesmo método roda também na 1ª análise automática do `POST /regioes` (`RegiaoResource.java:26`), então vale para os dois gatilhos.

Ponto de entrada. `RegiaoResource.analisar()` (`RegiaoResource.java:66-71`):

```java
@POST @Path("/{id}/analisar")
public Response analisar(@PathParam("id") int id) throws Exception {
    Alerta alerta = analiseRiscoBo.analisar(id);
    return Response.ok(alerta).build();
}
```

O Quarkus (RESTEasy Reactive) roteia `/regioes/{id}/analisar` para este método. O `@Path` de método combinado ao `@Path("/regioes")` da classe resolve o caminho; `id` é injetado por `@PathParam`. O Resource não tem lógica: delega ao BO e devolve 200 com o alerta serializado. Toda a inteligência está em `AnaliseRiscoBo.analisar(int idRegiao)` (`AnaliseRiscoBo.java:18`).

Passo 1 — Busca a região. `AnaliseRiscoBo.java:19-23`. `RegiaoDao.buscarPorId(idRegiao)`; se `null`, lança `RegistroNaoEncontradoException` → o ExceptionMapper devolve 404. É a única validação de entrada aqui (o id vem da URL).

Passo 2 — Busca a subprefeitura. `AnaliseRiscoBo.java:25-26`. `SubprefeituraDao.buscarPorId(regiao.getFkSubprefeituraIdSubpref())`. Necessária para extrair `cd_subpref` (feature da IA) e, no modo Open-Meteo, lat/long do centroide.

Passo 3 — Obtém o clima (fonte intercambiável). `AnaliseRiscoBo.java:28-36`:

```java
ServicoClima servico = ServicoClimaFactory.criar();   // lê FONTE_CLIMA
try { dados = servico.obterDados(regiao); }
catch (Exception e) { ...; dados = new ClimaSimulado().obterDados(regiao); }  // fallback
```

`ServicoClimaFactory.criar()` (`ServicoClimaFactory.java:8-14`) devolve `ClimaOpenMeteo` se `FONTE_CLIMA=OPEN_METEO`, senão `ClimaSimulado` (default). Há fallback explícito: se a fonte principal falhar (ex. Open-Meteo fora), cai para o simulado — a análise nunca quebra por causa de rede de clima. O retorno é um `DadosClimaticos` já normalizado (precipitação horária em array, mais umidade/pressão/vento/temperatura/umidade do solo da hora atual).

Passo 4 — Persiste a leitura. `AnaliseRiscoBo.java:44-58`. Monta `LeituraClimatica`, gravando a precipitação da hora atual (`precHoraria[0]`, `:48`, com guard de array vazio), `dia_previsao=0` (`:56`) e a FK da região. Persistido por `LeituraClimaticaDao.inserir()` (`:58`). O id vem do esquema de timestamp do passo seguinte.

IDs sem SEQUENCE. `AnaliseRiscoBo.java:39-42`:

```java
long baseTimestamp = System.currentTimeMillis();
int idLeitura = (int)(baseTimestamp % 9_000_000) + 1_000_000;
int idAlerta  = (int)((baseTimestamp + 1) % 9_000_000) + 1_000_000;
int idNotif   = (int)((baseTimestamp + 2) % 9_000_000) + 1_000_000;
```

Leitura, alerta e notificação criados na análise recebem ids derivados do relógio (faixa 1.000.000–9.999.999), pois o banco não tem SEQUENCE. O `+1`/`+2` evita colisão entre as três PKs da mesma análise.

Passo 5 — Monta o Contrato 1. `AnaliseRiscoBo.java:60-61` chama `montarVariaveisIa(dados, cdSubpref)` (`:113-134`). Resultado: um `Map<String,Object>` com exatamente 8 chaves em snake_case (`:124-131`):

```java
variaveis.put("precipitacao_6h", precipAcumulada);  // soma da janela de 6h (:116-119)
variaveis.put("umidade", ...); variaveis.put("pressao", ...); variaveis.put("vento", ...);
variaveis.put("temperatura", ...); variaveis.put("umidade_solo", ...);
variaveis.put("mes", LocalDate.now().getMonthValue()); variaveis.put("subpref_cod", cdSubpref);
```

`precipitacao_6h` é o único valor derivado: soma das primeiras 6 posições do array de precipitação horária (`:116-119`). Os demais são leitura direta da hora atual. `mes` e `subpref_cod` são calculados pelo Java. Detalhe do contrato em `docs/contratos-backend-ia.md`.

Passo 6 — Chama a IA (intercambiável). `AnaliseRiscoBo.java:63-69`. `IaServiceFactory.criar()` (`IaServiceFactory.java:7-13`) devolve `IaServiceReal` se `IA_MODO=REAL`, senão `IaServiceMock` (default). `avaliarRisco(variaveis)` retorna `RespostaIa`. Se a chamada lançar (ex. Flask fora, `IA_URL` ausente), o BO encapsula em `RuntimeException` (`:68`) → 500. No modo REAL, `IaServiceReal` serializa o Map com Gson, faz `HttpPost` para `IA_URL`, e só interpreta o corpo se o status for 200 (`IaServiceReal.java:44-49`); status ≠ 200 vira exceção com a mensagem da Flask. No modo MOCK, `IaServiceMock` aplica regras de faixa sobre `precipitacao_6h` e `vento` (`IaServiceMock.java:17-26`).

Passo 7 — Guard de null + normalização. `AnaliseRiscoBo.java:73-76`:

```java
if (respostaIa == null || respostaIa.getRisco_geral() == null) {
    throw new RuntimeException("Resposta da IA invalida: risco_geral ausente.");
}
String nivel = respostaIa.getRisco_geral().toUpperCase();
```

A Flask devolve "Baixo"/"Medio"/"Alto"; o banco exige maiúsculas (CHECK de `tb_alerta`). O `toUpperCase()` normaliza. O guard (adicionado na revisão pré-deploy) impede NPE caso a IA responda 200 sem o campo.

Passo 8 — Cria e grava o alerta (sempre). `AnaliseRiscoBo.java:77-86`. `Alerta` com `nivel`, `tp_evento="Analise Automatica"`, descrição incluindo o `score_alagamento`, FK da região e FK da leitura. Persistido por `AlertaDao.inserir()` (`:86`). Gravado independentemente do nível — é o histórico.

Passo 9 — Associação alerta×modelo. `AnaliseRiscoBo.java:88-96`. Lê todos os modelos (`ModeloIaDao.selecionar()`, devolve os 2 do seed) e, para cada, insere um `AlertaModelo` com o `score_alagamento` na associativa. Ou seja, todo alerta é associado a cada `ModeloIa` cadastrado (no seed, 2 linhas por alerta).

Passo 10 — Notificação condicional. `AnaliseRiscoBo.java:99-108`. Só se `nivel` for MEDIO ou ALTO: cria `Notificacao` (estado `NAO_LIDA`, FK do usuário dono da região, FK do alerta) e persiste por `NotificacaoDao.inserir()`. Risco BAIXO não gera notificação — mas o alerta do passo 8 já está no histórico.

Passo 11 — Retorno. `AnaliseRiscoBo.java:110` devolve o `Alerta`. O Resource embrulha em `Response.ok(alerta)` (200) e o Jackson serializa em JSON. O front, ao ver `nivelAlerta != "BAIXO"`, sabe que houve notificação e pode atualizar a caixa de entrada.

Resumo de efeitos colaterais de uma análise: +1 linha em `tb_leitura_climatica`, +1 em `tb_alerta`, +N em `tb_alerta_modelo` (N = nº de modelos = 2), +1 em `tb_notificacao` se MEDIO/ALTO. Sem transação explícita: cada inserção é autocommit independente (ver Q&A 8).

---

## Seção 5 — Inventário detalhado por camada

### 5.1 Entities (9)

| Classe | Tabela | Atributos principais | Relacionamento |
|---|---|---|---|
| `Usuario` | `tb_usuario` | idUsu, nmUsu, emailUsu, senhaUsu (hash), tpUsu, dtCadastro | pai de Regiao e Notificacao |
| `Subprefeitura` | `tb_subprefeitura` | idSubpref, cdSubpref, nmSubpref, latitudeSubpref, longitudeSubpref, qtAlagamento | pai de Regiao (domínio) |
| `Regiao` | `tb_regiao` | idReg, nmReg, dtCadastro, fkUsuarioIdUsu, fkSubprefeituraIdSubpref | filha de Usuario+Subprefeitura; pai de Leitura/Alerta |
| `LeituraClimatica` | `tb_leitura_climatica` | idLeitura, 6 variáveis climáticas, dtLeitura, diaPrevisao, fkRegiaoIdReg | filha de Regiao; pai de Alerta |
| `CenarioClimatico` | `tb_cenario_climatico` | idCenario, nmCenario, 6 variáveis, nivelCenario | sem FK (fonte simulada) |
| `ModeloIa` | `tb_modelo_ia` | idModelo, nmModelo, tpModelo, versaoModelo | N:N com Alerta |
| `Alerta` | `tb_alerta` | idAlerta, nivelAlerta, tpEvento, dsAlerta, dtAlerta, fkRegiaoIdReg, fkLeituraIdLeitura | filha de Regiao+Leitura; pai de Notificacao; N:N com ModeloIa |
| `AlertaModelo` | `tb_alerta_modelo` | fkAlertaIdAlerta, fkModeloIdModelo, scoreModelo | associativa N:N (PK composta) |
| `Notificacao` | `tb_notificacao` | idNotif, dsNotif, dtNotif, estadoNotif, fkUsuarioIdUsu, fkAlertaIdAlerta | filha de Usuario+Alerta |

Convenção: nomes de atributo espelham as colunas (idUsu↔id_usu). `LocalDate` serializa como string ISO. Campos das entities detalhados em `docs/contrato-api.md` Seção 5.

### 5.2 DAOs (9)

| Classe | Métodos públicos | Particularidade |
|---|---|---|
| `UsuarioDao` | inserir, atualizar, deletar, selecionar, buscarPorId, buscarPorEmail | CRUD completo + busca por email (login/unicidade); deletar simples (sem cascade) |
| `RegiaoDao` | inserir, atualizar, deletar, selecionar, buscarPorId, selecionarPorUsuario | CRUD completo + filtro por usuário; PK manual no insert (`:20`); deletar com cascade transacional (5 statements em setAutoCommit/commit) |
| `SubprefeituraDao` | selecionar, buscarPorId | só leitura (tabela de domínio) |
| `LeituraClimaticaDao` | inserir, buscarPorId, selecionarPorRegiao | escrita pelo pipeline; sem update/delete (não há endpoint) |
| `AlertaDao` | inserir, buscarPorId, selecionarPorRegiao | escrita pelo pipeline; leitura no histórico (`GET /regioes/{id}/alertas`) |
| `AlertaModeloDao` | inserir, selecionarPorAlerta | escrita na associativa pelo pipeline |
| `ModeloIaDao` | selecionar, buscarPorId | só leitura (domínio dos 2 modelos) |
| `CenarioClimaticoDao` | selecionar, buscarPorId | só leitura (alimenta o ClimaSimulado) |
| `NotificacaoDao` | inserir, atualizar, selecionarPorUsuario, selecionarNaoLidasPorUsuario | atualizar usado só para marcar-lida; retorna linhas afetadas (base do 404) |

Particularidade comum a todos: cada método abre `new ConexaoFactory().conexao()` no início e fecha conexão (e `ResultSet`/`PreparedStatement`) em `finally`. Todo SQL é PreparedStatement com `?` — zero concatenação. ResultSet mapeado por nome de coluna (ex. `UsuarioDao.java:83-88`).

### 5.3 BOs (4)

`UsuarioBo` (`bo/UsuarioBo.java`). Regras: cadastro valida nome não-vazio, email contendo `@`, senha ≥ 6 caracteres (`:17-22`); checa unicidade de email via `buscarPorEmail` e lança `EmailJaCadastradoException` (`:25-26`); gera hash SHA-256 da senha antes de persistir (`:31`). Login: busca por email, compara hash da senha digitada com o do banco; falha de email OU senha lança a MESMA `CredenciaisInvalidasException` (`:38-39`, não vaza qual falhou). Update preserva senha se vier vazia e preserva `dtCadastro` se ausente (`:55-66`).

`RegiaoBo` (`bo/RegiaoBo.java`). Regras: nome obrigatório; valida existência do usuário (`:20-22`) e da subprefeitura (`:24-26`) antes de inserir — FKs precisam existir, senão `ValidacaoException` (400, não 500 de FK). Update exige a região existir (`:39-42`) e preserva `dtCadastro`. Delete retorna linhas afetadas; 0 → `RegistroNaoEncontradoException` (404).

`AnaliseRiscoBo` (`bo/AnaliseRiscoBo.java`). Orquestra todo o pipeline da Seção 4. Único BO que consome services. Não valida entrada além da existência da região (id vem da URL).

`SenhaUtil` (`bo/SenhaUtil.java`). Utilitário estático: `gerarHash(String)` com `MessageDigest("SHA-256")` nativo, retorna 64 hex chars. Sem biblioteca externa.

### 5.4 Services (11)

- `ServicoClima` (interface): `obterDados(Regiao): DadosClimaticos`.
- `ClimaOpenMeteo`: fonte real. Resolve lat/long da subprefeitura (fallback centro de SP, `ClimaOpenMeteo.java:21-34`), monta URL da Open-Meteo, faz `HttpGet`, parseia com Gson em `RespostaOpenMeteo`, seleciona o índice da hora atual e valida arrays antes de mapear (`:56-78`). Usado só com `FONTE_CLIMA=OPEN_METEO`.
- `ClimaSimulado`: fonte default. Lê `tb_cenario_climatico` via `CenarioClimaticoDao`; se `CENARIO_FIXO` setada, usa aquele id; senão rodízio round-robin com `AtomicInteger` (`ClimaSimulado.java:15,43`). Converte o cenário em `DadosClimaticos` (precipitação vira array de 1 elemento, `:54`).
- `ServicoClimaFactory`: switch por `FONTE_CLIMA` (`:8-14`).
- `IaService` (interface): `avaliarRisco(Map<String,Object>): RespostaIa`.
- `IaServiceReal`: POST para `IA_URL` (Apache HttpClient + Gson), valida status 200, extrai mensagem de erro da Flask se ≠ 200 (`IaServiceReal.java:44-49,53-67`). Usado só com `IA_MODO=REAL`.
- `IaServiceMock`: default. Regras de faixa sobre `precipitacao_6h` e `vento` devolvendo Baixo/Medio/Alto + score (`IaServiceMock.java:17-26`). Sem HTTP.
- `IaServiceFactory`: switch por `IA_MODO` (`:7-13`).
- `DadosClimaticos`: POJO de transporte do clima já tratado (precipitacaoHoraria[], umidadeRelativa, pressao, velocidadeVento, temperatura, umidadeSolo).
- `RespostaOpenMeteo`: POJO espelhando o JSON da Open-Meteo (campo `hourly` com arrays em snake_case), sem `@SerializedName`.
- `RespostaIa`: POJO espelhando o JSON da Flask: `risco_geral`, `score_alagamento`, `status` (`RespostaIa.java:7-9`).

### 5.5 Resources (6) — 19 endpoints

| Resource | Endpoints | Observação |
|---|---|---|
| `AuthResource` | POST `/auth/cadastro`, POST `/auth/login` | cadastro devolve 201 + Location `/usuarios/{id}` e DTO; login devolve DTO (sem senha) |
| `UsuarioResource` | GET `/usuarios`, GET `/usuarios/{id}`, GET `/usuarios/{id}/regioes`, GET `/usuarios/{id}/notificacoes`, GET `/usuarios/{id}/notificacoes/nao-lidas`, PUT `/usuarios/{id}`, DELETE `/usuarios/{id}` | concentra todos os sub-recursos de `/usuarios` (consolidados na revisão para evitar ambiguidade de @Path); GET lista DTO sem senha |
| `RegiaoResource` | POST `/regioes`, GET `/regioes`, GET `/regioes/{id}`, PUT `/regioes/{id}`, DELETE `/regioes/{id}`, POST `/regioes/{id}/analisar` | POST dispara 1ª análise (`:26`); PUT não dispara análise |
| `AlertaResource` | GET `/regioes/{id}/alertas` | classe com `@Path("/regioes")` coexiste com RegiaoResource (RESTEasy mescla por mesmo prefixo) |
| `NotificacaoResource` | PUT `/notificacoes/{id}/marcar-lida` | só a ação sobre a própria notificação; 404 se 0 linhas afetadas |
| `SubprefeituraResource` | GET `/subprefeituras`, GET `/subprefeituras/{id}` | tabela de domínio para o formulário do front |

Verbos/status: POST→201 (created+Location) ou 200 (analisar); GET→200/404; PUT→200/404; DELETE→204/404. Referência completa de bodies/erros em `docs/contrato-api.md`.

### 5.6 DTOs (1)

`UsuarioRespostaDto` (`dto/UsuarioRespostaDto.java`). Existe por um motivo: nunca expor `senhaUsu` (o hash). Espelha `Usuario` sem o campo de senha; conversão pela factory estática `de(Usuario)` (`:26-31`). Usado em todo GET de usuário e nas respostas de cadastro/login (`AuthResource.java:28,37`). É o único DTO — as demais entities vão cruas no JSON (não há campo sensível nelas).

### 5.7 Exceções (5)

| Classe | Quando lançada | Status |
|---|---|---|
| `ValidacaoException` | dado inválido na entrada (nome vazio, email sem @, senha curta, FK inexistente no cadastro de região) | 400 |
| `CredenciaisInvalidasException` | email ou senha incorretos no login | 401 |
| `RegistroNaoEncontradoException` | busca/update/delete de id inexistente | 404 |
| `EmailJaCadastradoException` | cadastro com email já existente | 409 |
| `OrbitaSafeExceptionMapper` | (não é exceção) `@Provider` que mapeia as acima + fallback 500 | — |

Mapeamento em `OrbitaSafeExceptionMapper.java:13-32`. O fallback 500 imprime stacktrace no stderr (`:29-30`) e devolve corpo fixo `{ "erro": "Erro interno no servidor" }`.

### 5.8 Conexões (1)

`ConexaoFactory` (`conexoes/ConexaoFactory.java`). Método `conexao()` lê `ORACLE_URL`/`ORACLE_USER`/`ORACLE_PASSWORD` de env vars (`:10-12`); se faltar qualquer uma, lança `RuntimeException` clara (`:14-17`); `Class.forName("oracle.jdbc.driver.OracleDriver")` e `DriverManager.getConnection`. Sem pool, sem `@ApplicationScoped`. Instanciada com `new` dentro de cada método de DAO.

Observação: `application.properties` também declara um datasource Quarkus (`quarkus.datasource.*`) apontando para as mesmas env vars, mas a conexão real do app é a manual via `ConexaoFactory`. O datasource declarado é, na prática, redundante (lazy; ninguém injeta Agroal).

### 5.9 Classes Teste* (4) — em `main/`, rodam via método `main`

| Classe | O que exercita | Como rodar |
|---|---|---|
| `TesteConexao` | `SELECT 1 FROM DUAL` — valida credenciais/conectividade Oracle | run main com env Oracle setadas |
| `TesteUsuarioCadastroLogin` | cadastro (com hash), login OK e login inválido (try/catch de CredenciaisInvalidasException, ajustado na revisão) | run main |
| `TesteRegiaoCrud` | CRUD de região via `RegiaoBo` (insere id 999, busca, atualiza, lista por usuário, deleta) | run main |
| `TesteAnaliseRiscoCompleta` | dispara `AnaliseRiscoBo.analisar(1)` e confere se gerou notificação | run main com defaults MOCK/SIMULADO |

Não são testes JUnit — são `main`s de verificação manual com `System.out`. Não entram no `mvn test`.

---

## Seção 6 — Banco de dados

### 6.1 Modelo (9 tabelas)

`tb_usuario` (usuários, senha hash) · `tb_subprefeitura` (32 subprefeituras, domínio) · `tb_modelo_ia` (2 modelos) · `tb_cenario_climatico` (6 cenários da fonte simulada) · `tb_regiao` (regiões monitoradas) · `tb_leitura_climatica` (leituras de clima) · `tb_alerta` (alertas de risco) · `tb_alerta_modelo` (associativa N:N) · `tb_notificacao` (caixa de entrada). DDL/DML em `docs/database/script-oracle.sql` (script único re-executável: DROP em ordem inversa + CREATE + INSERTs + 10 consultas + 5 relatórios).

### 6.2 Relacionamentos

- `tb_usuario` 1:N `tb_regiao` (FK `reg_usu_fk`, `script-oracle.sql:85`). Sem ON DELETE CASCADE.
- `tb_subprefeitura` 1:N `tb_regiao` (FK `reg_subpref_fk`, `:87`).
- `tb_regiao` 1:N `tb_leitura_climatica` (FK `leitura_reg_fk`, `:103`).
- `tb_regiao` 1:N `tb_alerta` (FK `alerta_reg_fk`, `:115`).
- `tb_leitura_climatica` 1:N `tb_alerta` (FK `alerta_leitura_fk`, `:117`).
- `tb_alerta` N:N `tb_modelo_ia` via `tb_alerta_modelo` (PK composta `am_id_pk` das duas FKs, `:125`).
- `tb_alerta` 1:N `tb_notificacao` (FK `notif_alerta_fk`, `:138`).
- `tb_usuario` 1:N `tb_notificacao` (FK `notif_usu_fk`, `:136`).

Nenhuma FK tem ON DELETE CASCADE — por isso o DELETE de uma região aplica cascade transacional sobre os registros dependentes antes de removê-la (ver Q&A 9).

### 6.3 Estratégia de PK (sem SEQUENCE)

- `Usuario`, `Regiao`, `Subprefeitura`, `ModeloIa`, `CenarioClimatico`: id enviado pelo cliente/seed (PK manual). Para usuário e região o front manda `idUsu`/`idReg` no corpo do POST.
- `LeituraClimatica`, `Alerta`, `Notificacao`: id gerado no pipeline por `System.currentTimeMillis()` (`AnaliseRiscoBo.java:39-42`), faixa 1.000.000+.
- `AlertaModelo`: sem id próprio — PK composta (fkAlerta, fkModelo).

### 6.4 Decisões sobre o banco

- Sem SEQUENCE/TRIGGER/IDENTITY: padrão da disciplina (PK cravada no INSERT). Cabeçalho do script reforça (`script-oracle.sql:7`).
- Constraints CHECK existem onde o domínio é fechado: `nivel_cenario` e `nivel_alerta` IN (BAIXO,MEDIO,ALTO) (`:76,110`); `estado_notif` IN (LIDA,NAO_LIDA) (`:134`); `dia_previsao` BETWEEN 0 AND 3 (`:101`). Campos de texto livre (`tp_usu`, `tp_modelo`, `tp_evento`) não têm CHECK — daí `tp_usu` aceitar COMUM/ADMIN sem restrição.
- Tipos no padrão das aulas: `varchar(n)` (não `varchar2`), `number(p,s)`, `date`, `char(64)` para o hash. Constraints sempre nomeadas (ex. `usu_id_pk`, `usu_email_uk`).
- Seed (~109 linhas): 32 subprefeituras (cd 101–132), 6 usuários (senhas hash de '1'..'6'), 2 modelos, 6 cenários (1-2 BAIXO, 3-4 MEDIO, 5-6 ALTO), 10 regiões, 18 leituras, 14 alertas, 13 associações, 8 notificações. Inserção em ordem de dependência, `commit` ao final (`:301`).

Atenção (cai na banca): os `cd_subpref` do seed são provisórios sequenciais 101–132 (`:149-151`) e precisam casar com os códigos do dataset de treino da IA — divergência quebra o `subpref_cod` do Contrato 1 silenciosamente.

### 6.5 Consultas e relatórios (entrega da disciplina de Banco)

10 consultas (`script-oracle.sql:308-367`), cada uma exercitando um recurso distinto exigido pelo edital:
1. Alertas por região — COUNT + GROUP BY.
2. Média de score por modelo de IA — AVG + GROUP BY.
3. Regiões com mais de 1 alerta — HAVING + ORDER BY.
4. Regiões monitoradas por usuário — COUNT + GROUP BY.
5. Subprefeitura com maior histórico de alagamentos — MAX via subconsulta (IN).
6. Notificações não lidas por usuário — COUNT + WHERE.
7. Leituras com precipitação > 50 mm — WHERE + ORDER BY.
8. Alertas num intervalo de datas — BETWEEN.
9. Percentual de alertas ALTO sobre o total — CASE + agregação.
10. Resumo de precipitação — SUM, AVG, MAX, MIN.

5 relatórios com JOIN (`script-oracle.sql:375-399`), cobrindo os 3 tipos exigidos: R1 INNER (usuários × regiões); R2 INNER de 3 tabelas (alertas × região × subprefeitura); R3 LEFT "somente diferença" (regiões sem nenhuma leitura, via `IS NULL`); R4 LEFT "somente diferença" (alertas que não geraram notificação = risco baixo); R5 RIGHT "somente diferença" (subprefeituras sem nenhuma região). Os LEFT/RIGHT estão na forma "somente a diferença" (filtro `IS NULL`), que é o que o edital pede.

---

## Seção 7 — Decisões técnicas e defesas (Q&A)

P1. Por que JDBC manual em vez de JPA/Hibernate?
R. Exigência da disciplina (1TDS usa JDBC puro). Dá controle total do SQL e baixo acoplamento — vejo exatamente a query que roda. JPA/Hibernate está fora do escopo acadêmico deste projeto. Materializado em todo `dao/` (ex. `UsuarioDao.java:17-27`).

P2. Por que sem @Inject / sem CDI?
R. Padrão das aulas: instanciação com `new`. O `quarkus-arc` (CDI) está no classpath mas não é usado para injeção. Resources criam BOs com `new` em campo de instância (`RegiaoResource.java:19-20`); BOs criam DAOs com `new` por método. Decisão didática consciente, não desconhecimento de CDI.

P3. Por que SHA-256 sem salt e sem BCrypt?
R. SHA-256 é nativo do Java (`MessageDigest`), sem dependência externa — `SenhaUtil.java`. BCrypt está fora do escopo das aulas. Limitação assumida e documentada: sem salt é vulnerável a rainbow table; em produção real seria BCrypt+salt. O importante para a nota está cumprido: senha nunca em texto puro (hash de 64 hex no banco, `script-oracle.sql:39`).

P4. Por que abrir e fechar conexão por método em vez de pool ou no construtor?
R. Quarkus é servidor de vida longa. Manter a conexão num atributo do DAO (como no exemplo cru das aulas) vazaria sessões Oracle e estouraria ORA-02391 (limite de sessões concorrentes). Abrir no início do método e fechar em `finally` garante devolução determinística (`UsuarioDao.java:14,29-32`). É extensão consciente sobre o padrão didático, defendida como correção de um anti-padrão.

P5. Por que PK manual em vez de SEQUENCE?
R. Padrão da disciplina (PK cravada no INSERT, sem SEQUENCE/TRIGGER/IDENTITY). Para as entidades criadas via API (usuário, região) o id vem no corpo; para as criadas no pipeline (leitura, alerta, notificação) uso `System.currentTimeMillis()` (`AnaliseRiscoBo.java:39-42`). Trade-off conhecido: id por timestamp não é à prova de concorrência alta, aceitável no escopo acadêmico.

P6. Por que AtomicInteger no contador de rotação de cenários?
R. O Quarkus serve requisições concorrentes (multi-thread). O round-robin do `ClimaSimulado` (`:15,43`) usa `AtomicInteger.getAndIncrement()` para o incremento ser atômico — dois requests simultâneos não pegam o mesmo índice nem corrompem o contador. Um `int` comum teria race condition.

P7. Por que Map<String,Object> no payload pra IA em vez de POJO?
R. O Contrato 1 são 8 chaves planas e o Gson serializa o Map direto para o JSON esperado pela Flask (`AnaliseRiscoBo.java:121-131`, `IaServiceReal.java:29`). Um POJO exigiria classe espelho só para a requisição, sem ganho — o contrato é fixo e pequeno. A resposta, sim, é POJO (`RespostaIa`), porque preciso de getters tipados.

P8. Por que sem transação explícita no fluxo de análise?
R. `setAutoCommit`/transação não está no padrão das aulas — fora do escopo. Cada INSERT do pipeline (leitura, alerta, associações, notificação) comita por conta própria. Limitação assumida: uma falha no meio deixa estado parcial (ex. leitura gravada sem alerta). No escopo de demo isso não ocorre porque o caminho default (MOCK) não falha. Em produção real, envolveria o passo 4-10 numa transação única.

P9. Por que DELETE /regioes/{id} faz cascade transacional em vez de um DELETE simples?
R. Originalmente, a opção foi manter o padrão didático das aulas (DELETE simples, sem CASCADE). Em testes de integração com o front-end ficou claro que esta decisão jogaria responsabilidade indevida para a camada de apresentação — o front-end não deveria conhecer dependências de chave estrangeira. O `RegiaoDao.deletar` foi reescrito para fazer cascade manual na ordem correta (alerta_modelo → notificacao → alerta → leitura → regiao), envolto em `setAutoCommit(false)` com commit/rollback. A transação garante atomicidade: se qualquer passo falhar, o rollback é acionado e o banco não fica em estado inconsistente. Esta é uma extensão consciente do padrão das aulas, justificada pela natureza composta da operação.

P10. Por que duas fontes de clima (Open-Meteo + Simulado)?
R. Garantir demonstração confiável sem depender de rede. `ClimaOpenMeteo` é a fonte real (produção); `ClimaSimulado` lê cenários do banco e permite travar um cenário específico (`CENARIO_FIXO`) para a banca. A troca é por env var via `ServicoClimaFactory` (`:8-14`), sem recompilar. Interface `ServicoClima` mantém o resto do código alheio à fonte.

P11. Por que duas implementações de IA (Real + Mock)?
R. A Flask da equipe de IA é projeto separado e pode não estar no ar na hora da demo. `IaServiceMock` (`:17-26`) aplica regras de faixa e devolve o MESMO formato (`RespostaIa`) que a Flask real, permitindo o fluxo completo offline. Troca por `IA_MODO` via `IaServiceFactory`. Default MOCK.

P12. Por que CORS com origem explícita via env var em vez de "*"?
R. `*` é prática insegura e o material das aulas a usa; aqui o `CorsFilter` (`:14-19`) libera uma origem específica, lida de `FRONTEND_URL` com fallback `http://localhost:5173`. Permite configurar a URL da Vercel em produção sem recompilar. Extensão consciente declarada.

P13. Por que sem JWT / autenticação stateful?
R. Escopo acadêmico stateless. Após o login o front guarda o `idUsu` (localStorage) e o envia onde a ação precisa. Sem token, sem sessão server-side, sem proteção de rota. Login só valida credenciais e devolve o usuário sem senha (`AuthResource.java:31-38`). JWT está fora do escopo.

P14. Por que ExceptionMapper central em vez de try/catch nos Resources?
R. Mantém os Resources limpos (só `throws`) e centraliza o mapeamento exceção→status num único ponto (`OrbitaSafeExceptionMapper.java`). Toda exceção de domínio vira o mesmo formato de corpo `{ "erro": ... }`. Atende a exigência de tratamento de exceções do edital com menos repetição.

P15. Por que UsuarioRespostaDto é o único DTO?
R. DTO só onde há necessidade concreta — aqui, esconder o hash de senha (`dto/UsuarioRespostaDto.java`). As outras entities não têm campo sensível, então vão cruas no JSON. Não crio DTO "por boas práticas" — seria código morto.

P16. Por que algumas tabelas têm DAO com CRUD completo e outras só mínimo?
R. O DAO expõe só o que algum caso de uso pede. `Usuario`/`Regiao` têm CRUD completo (endpoints REST de CRUD). Tabelas de domínio (`Subprefeitura`, `ModeloIa`, `Cenario`) só têm leitura. As do pipeline (`Leitura`, `Alerta`, `AlertaModelo`) só têm escrita+leitura — não há endpoint para editar/apagar uma leitura ou alerta. `Notificacao` tem `atualizar` apenas para o marcar-lida. Evita método sem uso.

P17. Por que cada Resource instancia BO via `new` em vez de campo static/singleton?
R. Consistência com o padrão sem CDI (P2). O BO não tem estado mutável compartilhado, então um campo de instância `new` por Resource é seguro e simples (`UsuarioResource.java:21`). Um singleton exigiria gestão de ciclo de vida que o `new` torna desnecessária neste escopo.

P18. Como o sistema evita SQL injection?
R. Todo acesso é PreparedStatement com placeholders `?` e binding por setX — zero concatenação de input em SQL. Ex. `UsuarioDao.buscarPorId` (`:104-107`). É invariante do projeto, não exceção.

P19. Por que Gson + Apache HttpClient e não o REST Client do Quarkus?
R. Padrão das aulas (o `ViaCepService` didático usa HttpClient 4.5 + Gson). Mantém o consumo de API externa idêntico ao material. O `quarkus-rest-client` resolveria com menos código, mas está fora do que foi ensinado. Materializado em `IaServiceReal.java:31-49` e `ClimaOpenMeteo.java:43-53`.

P20. O que foi a ambiguidade de roteamento que vocês corrigiram?
R. Três classes disputavam o prefixo `/usuarios`: UsuarioResource, um RegiaoUsuarioResource (também `@Path("/usuarios")`) e o NotificacaoResource com `@Path("/")` servindo caminhos `/usuarios/*`. O RESTEasy resolvia `/usuarios/{id}/notificacoes` no grupo de prefixo errado e disparava NotFoundException (500/404). Consolidei todos os sub-recursos de `/usuarios` em UsuarioResource (`UsuarioResource.java:51-79`) e movi o marcar-lida para uma classe com `@Path("/notificacoes")`. Regra extraída: nunca declarar um caminho `/recurso/...` numa classe cujo `@Path` de classe não seja `/recurso`.

P21. Por que manter `requests.http` E a coleção Insomnia?
R. `requests.http` (26 reqs) é teste rápido no terminal/IDE, sem dependência. A coleção Insomnia (29 reqs, 8 pastas) tem response chaining (login alimenta `usuarioLogadoId`; criar região alimenta `regiaoCriadaId`) e casos de erro organizados — melhor para a demo guiada. Redundância barata e proposital.

P22. Por que o front guarda o idUsu e não há proteção de rota no back?
R. Escopo acadêmico stateless sem JWT (ver P13). Qualquer endpoint é chamável sem autenticação; o controle de acesso é simulado no front. Limitação assumida — em produção haveria token + filtro de autorização server-side.

P23. Por que dia_previsao existe se a análise sempre grava 0?
R. O modelo previa previsão de até 3 dias (CHECK 0..3, `script-oracle.sql:101`; seed usa 0 e 1). A análise atual grava sempre 0 = hoje (`AnaliseRiscoBo.java:56`). O campo fica preparado para a meta de previsão multi-dia, que está fora do escopo mínimo entregue.

P24. Por que CHECK em nivel/estado mas não em tp_usu/tp_evento?
R. Domínios fechados e usados em lógica (`nivel` BAIXO/MEDIO/ALTO, `estado` LIDA/NAO_LIDA, `dia_previsao` 0..3) têm CHECK para integridade referencial de valor. `tp_usu` e `tp_evento` são texto livre/descritivo, sem conjunto fixo acordado pela equipe — sem CHECK por escolha. Por isso `tp_usu` aceita COMUM e ADMIN no seed sem violar constraint.

---

## Seção 8 — Variáveis de ambiente, configuração e deploy

### 8.1 Lista completa

| Variável | Default | Lida em | Obrigatória | Efeito |
|---|---|---|---|---|
| `ORACLE_URL` | — | `ConexaoFactory.java:10` | sempre | URL JDBC do Oracle |
| `ORACLE_USER` | — | `ConexaoFactory.java:11` | sempre | usuário do banco |
| `ORACLE_PASSWORD` | — | `ConexaoFactory.java:12` | sempre | senha do banco |
| `IA_MODO` | `MOCK` | `IaServiceFactory.java:8` | não | REAL=chama Flask; MOCK=avaliador interno |
| `IA_URL` | — | `IaServiceReal.java:22` | só se IA_MODO=REAL | endpoint completo de predição da Flask |
| `FONTE_CLIMA` | `SIMULADO` | `ServicoClimaFactory.java:9` | não | OPEN_METEO=API real; SIMULADO=cenários do banco |
| `CENARIO_FIXO` | (rotação) | `ClimaSimulado.java:27` | não | trava um cenário (id 1–6) no modo SIMULADO |
| `PORT` | `8080` | `application.properties:1` (`${PORT:8080}`) | não | porta HTTP; Render injeta |
| `FRONTEND_URL` | `http://localhost:5173` | `CorsFilter.java:14` | recomendada | origem liberada no CORS |

### 8.2 Comportamento por combinação

- `SIMULADO` + `MOCK` (default): demo offline total — sem rede, sem Flask, sem Open-Meteo. É o que roda no Render hoje.
- `OPEN_METEO` + `MOCK`: clima real da Open-Meteo, risco calculado pelo mock interno.
- `SIMULADO` + `REAL`: cenário do banco, risco pela Flask real (precisa `IA_URL`).
- `OPEN_METEO` + `REAL`: produção full (clima real + IA real).
- `CENARIO_FIXO=N`: trava o cenário. Os 6: 1 Tempo Seco Estavel (BAIXO), 2 Chuva Leve (BAIXO), 3 Chuva Moderada (MEDIO), 4 Frente Fria Ativa (MEDIO), 5 Temporal Intenso (ALTO), 6 Tempestade Severa (ALTO).

Atenção (verifiquei no código): no modo MOCK, o nível NÃO vem do campo `nivel_cenario` do banco — o `IaServiceMock` recalcula a partir de `precipitacao_6h` e `vento` (`IaServiceMock.java:17-26`). Para os cenários 1 (BAIXO), 5 e 6 (ALTO) o resultado bate com o rótulo. Para 2/3/4 NÃO bate (ex.: cenário 3, precip 38 → mock devolve ALTO, mas o rótulo é MEDIO). Para a demo, use CENARIO_FIXO=5 (dá ALTO + notificação) e =1 (dá BAIXO, sem notificação) — esses são consistentes. Evite 2/3/4 se quiser que o nível mostrado case com o nome do cenário.

| id | cenário | precip | vento | rótulo (nivel_cenario) | nível que o MOCK produz |
|---|---|---|---|---|---|
| 1 | Tempo Seco Estavel | 2.5 | 10 | BAIXO | BAIXO (bate) |
| 2 | Chuva Leve | 12 | 18 | BAIXO | MEDIO (diverge) |
| 3 | Chuva Moderada | 38 | 28 | MEDIO | ALTO (diverge) |
| 4 | Frente Fria Ativa | 55 | 35 | MEDIO | ALTO (diverge) |
| 5 | Temporal Intenso | 95 | 52 | ALTO | ALTO (bate) |
| 6 | Tempestade Severa | 130 | 65 | ALTO | ALTO (bate) |

Regras do MOCK (`IaServiceMock.java:17-26`): `precip>30` ou `vento>50` → Alto (score 0.85); senão `precip>10` ou `vento>25` → Medio (0.60); senão Baixo (0.20). Por isso 5 e 6 batem por precipitação alta, 1 bate por tudo baixo, e 2/3/4 divergem do rótulo do seed.

### 8.3 Deploy

Render via **Docker** (o Render não tem runtime nativo de Java). `Dockerfile` multi-stage na raiz: o estágio 1 builda o JAR Quarkus com Maven (`mvn package -DskipTests`) e o estágio 2 roda só com JRE 21 (`java -jar /deployments/quarkus-run.jar`). A porta vem de `PORT` (não setar manualmente). Passo a passo, tabela de env vars do painel e troubleshooting em `docs/deploy.md`. Status: deploy em andamento; default MOCK+SIMULADO, então só as 3 `ORACLE_*` + `FRONTEND_URL` são realmente necessárias no painel.

---

## Seção 9 — Roteiro de demonstração e troubleshooting ao vivo

### 9.1 Roteiro principal (sequência de cliques)

1. Arquitetura: abrir o `README.md`, ler a seção Stack e o diagrama da Seção 3 deste doc em voz alta.
2. Banco: SQL Developer → mostrar as 9 tabelas → `SELECT * FROM tb_subprefeitura` (32 linhas) e `SELECT nivel_alerta, count(*) FROM tb_alerta GROUP BY nivel_alerta`.
3. Insomnia, Fluxo A (caso de uso feliz): pasta 1 "POST Cadastrar usuário" → "POST Login" (o login alimenta `usuarioLogadoId` por chaining); pasta 3 "POST Criar região" (alimenta `regiaoCriadaId`) → "POST Disparar análise manual" (alimenta `alertaCriadoId`); pasta 5 "GET Alertas da região"; pasta 6 "GET Todas as notificações".
4. Insomnia, Fluxo B (robustez): pasta 7 "POST Login senha errada" → 401; "GET Usuário inexistente" → 404; "PUT Marcar notificação inexistente" → 404. (Equivalem aos itens 2b/4b/20 do `requests.http`.)
5. Insomnia, Fluxo C (regra de negócio): subir a API com `CENARIO_FIXO=5` → pasta 8 "Análise cenário ALTO travado" → mostrar alerta ALTO e a notificação criada ("GET Não lidas" sobe). Reiniciar com `CENARIO_FIXO=1` → "Análise cenário BAIXO travado" → mostrar que o alerta BAIXO foi gravado MAS a contagem de não lidas NÃO muda.

São ~14 cliques cobrindo cadastro, autenticação, pipeline de análise, histórico, caixa de entrada, erros e a regra alerta-vs-notificação.

### 9.2 O que falar em cada passo (frases curtas)

- Cadastro: "A senha não volta na resposta — é hash SHA-256 no banco, e o UsuarioRespostaDto esconde o campo."
- Login: "Stateless: não há token; o front guarda o id do usuário. Email ou senha errados dão o mesmo 401, não vaza qual."
- Criar região: "O POST já dispara a 1ª análise — clima, IA, alerta e notificação num único request."
- Análise: "Aqui o Java orquestra: lê o clima do simulado, soma a janela de 6h, monta o Contrato 1 com 8 variáveis em snake_case, chama a IA, normaliza o nível pra maiúscula e persiste o alerta no Oracle."
- Cenário BAIXO: "Repare: o alerta BAIXO entra no histórico, mas notificação só nasce para MEDIO ou ALTO. Essa é a distinção alerta-vs-notificação."
- Erro 404: "Exceção de domínio sobe do BO, o ExceptionMapper central traduz pra 404 com corpo `{erro}` — os Resources não têm try/catch."

### 9.3 Troubleshooting ao vivo

- Cold start do Render (1ª request ~30s): avisar "é hibernação do Free Tier" e disparar um `GET /subprefeituras` "dummy" antes de começar, para acordar a instância.
- 500 em endpoint de banco: conferir `ORACLE_URL/USER/PASSWORD` no painel do Render e se o IP do Render é aceito pelo Oracle da FIAP. `ConexaoFactory` lança mensagem explícita se faltar env var.
- 404 inesperado na análise: confirmar que a região existe (`regiaoCriadaId` foi preenchido pelo chaining) e, se usar `CENARIO_FIXO`, que o id (1–6) existe no DML.
- Nível mostrado não bate com o cenário: lembrar que o MOCK recalcula por precip/vento (Seção 8.2) — usar só CENARIO_FIXO 1, 5 ou 6 na demo.
- IA timeout (modo REAL): a Flask hibernou? Trocar `IA_MODO=MOCK` no painel do Render acorda o fallback em segundos.
- CORS bloqueando o front: `FRONTEND_URL` no Render tem que ser a URL EXATA da Vercel (sem barra final divergente).

### 9.4 Perguntas-pivô (se perguntarem X, mostre Y)

- "E se a IA cair?" → mostrar `IA_MODO=MOCK` no Render e demonstrar o fluxo rodando — a arquitetura de fallback é o ponto.
- "Como evitam SQL injection?" → abrir `UsuarioDao.java:104-107` (PreparedStatement com `?`).
- "Como evitam vazar conexão?" → abrir o `finally` de qualquer DAO (`UsuarioDao.java:29-32`) e citar o ORA-02391 evitado.
- "Como escala?" → honestidade: projeto acadêmico, não escala além do Render Free Tier (1 instância, hibernação, sem pool). Em produção real: pool de conexões (Agroal/HikariCP), múltiplas instâncias atrás de balanceador, transação no pipeline e BCrypt+salt.
- "Por que o nível do alerta às vezes difere do nome do cenário no simulado?" → explicar que o mock é um classificador por faixa de precip/vento, independente do rótulo do seed (Seção 8.2).

---

## Apêndice — Mapa rápido arquivo:linha (consulta na banca)

- Pipeline de análise: `bo/AnaliseRiscoBo.java:18` (entrada), `:39-42` (ids), `:61` (Contrato 1), `:65` (IA), `:73-76` (guard+normaliza), `:86` (alerta), `:99` (notificação condicional).
- Contrato 1 (8 chaves): `bo/AnaliseRiscoBo.java:124-131`.
- Switch de fonte de clima: `services/ServicoClimaFactory.java:8`; de IA: `services/IaServiceFactory.java:7`.
- Regras do mock de IA: `services/IaServiceMock.java:17-26`.
- Hash de senha: `bo/SenhaUtil.java`; uso no cadastro `bo/UsuarioBo.java:31`; no login `:38`.
- Esconder senha: `dto/UsuarioRespostaDto.java:26`.
- CORS via env: `CorsFilter.java:14-19`.
- Porta dinâmica: `application.properties:1`.
- ExceptionMapper: `exceptions/OrbitaSafeExceptionMapper.java:13-32`.
- Conexão por método: `conexoes/ConexaoFactory.java:9`; uso `dao/UsuarioDao.java:14,29-32`.
- DDL/constraints/seed: `docs/database/script-oracle.sql` (CHECKs em `:76,101,110,134`; FKs em `:85-138`).

---

## Como usar este documento

- 5 minutos antes: Seção 1 (resumo) + Seção 7 (Q&A) + o Apêndice.
- Durante a banca (perguntas técnicas): Seção 7 (defesas), Seção 4 (passo a passo do pipeline), Seção 6 (banco).
- Durante a demo, tela aberta: Seção 9 (roteiro, falas, troubleshooting) — em especial 9.2 e 9.3.
- Se travar algo: Seção 9.3; se perguntarem "e se...": Seção 9.4.
