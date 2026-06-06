# OrbitaSafe — API (Back-end Java)

Plataforma de monitoramento e alerta de riscos climáticos para a cidade de São Paulo.
Projeto da **Global Solution 2026/1 — FIAP** (turmas 1TDS de Agosto), tema **Economia Espacial**.
Startup fictícia: **Orbit Analytics**.

> Este repositório contém a **API Java (Quarkus)** — o núcleo orquestrador do sistema. O front-end
> (React) e o serviço de IA (Flask) são projetos separados que consomem/são consumidos por esta API.

---

## 1. Sobre o projeto

O cidadão cadastra as regiões que deseja monitorar (escolhendo entre as 32 subprefeituras de São
Paulo). O sistema busca os dados climáticos dessas regiões e os submete a dois modelos de IA
(classificação de risco + regressão de score de alagamento). Quando um risco relevante é
identificado, o sistema gera **alertas** (histórico) e **notificações** (caixa de entrada do
usuário) para ação preventiva.

A API Java é o núcleo: recebe as requisições do front, persiste no Oracle, trata os dados
climáticos, chama o serviço de IA e expõe alertas e notificações. O front não acessa banco nem IA
diretamente.

---

## 2. Arquitetura e stack

| Componente | Tecnologia |
|---|---|
| Back-end (este repo) | Java 21 + Quarkus 3.34.6 |
| Banco de dados | Oracle Database (JDBC manual, sem JPA) |
| Serviço de IA | Python + Flask (REST, projeto separado) |
| Fonte de clima | Open-Meteo (real) + `TB_CENARIO_CLIMATICO` (simulado) |
| Front-end | React + Vite + TypeScript (projeto separado) |

**Fluxo de camadas (uma direção):** `Resource → BO → DAO → ConexaoFactory`.

- **Resource** (`resources/`): entrada HTTP (JAX-RS), sem regra de negócio.
- **BO** (`bo/`): validações e orquestração.
- **DAO** (`dao/`): SQL com `PreparedStatement`.
- **ConexaoFactory** (`conexoes/`): abre a conexão JDBC com o Oracle (credenciais via env var).
- **Services** (`services/`): integrações externas (Open-Meteo, Flask) com fonte intercambiável.

---

## 3. Funcionalidades

- Cadastro e login de usuários (senha em hash SHA-256; nunca em texto puro).
- CRUD de usuários e de regiões monitoradas.
- **Análise de risco automática** ao cadastrar uma região (1ª análise) e **sob demanda** (botão
  atualizar): clima → tratamento → IA → alerta → notificação (se risco MÉDIO/ALTO).
- Histórico de alertas por região (inclusive risco BAIXO).
- Caixa de entrada de notificações por usuário (com estado LIDA / NAO_LIDA).
- Listagem das 32 subprefeituras de São Paulo (para o formulário do front).
- Fonte de clima e modo de IA **intercambiáveis por variável de ambiente** (demonstração confiável
  sem depender de rede).

---

## 4. Modelo de dados (9 tabelas Oracle)

| Tabela | Descrição |
|---|---|
| `TB_USUARIO` | Usuários (senha em hash SHA-256). |
| `TB_SUBPREFEITURA` | 32 subprefeituras de SP (domínio); código `cd_subpref`, centroide lat/long. |
| `TB_REGIAO` | Região monitorada (FK usuário + subprefeitura). |
| `TB_LEITURA_CLIMATICA` | Leitura climática de uma região. |
| `TB_CENARIO_CLIMATICO` | Cenários pré-definidos para a fonte simulada. |
| `TB_MODELO_IA` | Registro dos dois modelos de IA. |
| `TB_ALERTA` | Alertas de risco (nível BAIXO/MEDIO/ALTO). |
| `TB_ALERTA_MODELO` | Associativa N:N entre alerta e modelo. |
| `TB_NOTIFICACAO` | Notificações ao usuário (estado LIDA/NAO_LIDA). |

> Script completo (DDL + DML + consultas + relatórios) em [`docs/database/script-oracle.sql`](docs/database/script-oracle.sql).
> Detalhe de cada campo em [`docs/contrato-api.md`](docs/contrato-api.md) (Seção 5).

---

## 5. Endpoints da API (19)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/cadastro` | Cadastrar usuário |
| POST | `/auth/login` | Login (retorna usuário sem senha) |
| GET | `/usuarios` | Listar usuários (sem senha) |
| GET | `/usuarios/{id}` | Buscar usuário por id |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Remover usuário |
| GET | `/usuarios/{id}/regioes` | Regiões de um usuário |
| GET | `/usuarios/{id}/notificacoes` | Notificações de um usuário |
| GET | `/usuarios/{id}/notificacoes/nao-lidas` | Notificações não lidas |
| POST | `/regioes` | Cadastrar região (dispara 1ª análise) |
| GET | `/regioes` | Listar regiões |
| GET | `/regioes/{id}` | Buscar região por id |
| PUT | `/regioes/{id}` | Atualizar região |
| DELETE | `/regioes/{id}` | Remover região |
| POST | `/regioes/{id}/analisar` | Análise sob demanda |
| GET | `/regioes/{id}/alertas` | Histórico de alertas da região |
| PUT | `/notificacoes/{id}/marcar-lida` | Marcar notificação como lida |
| GET | `/subprefeituras` | Listar as 32 subprefeituras |
| GET | `/subprefeituras/{id}` | Buscar subprefeitura por id |

> Referência completa (bodies, respostas, erros, exemplos `fetch`) em
> [`docs/contrato-api.md`](docs/contrato-api.md). Coleção testável em
> [`docs/insomnia-collection.yaml`](docs/insomnia-collection.yaml) e testes rápidos em
> [`src/main/resources/requests.http`](src/main/resources/requests.http).

---

## 6. Como executar localmente

**Pré-requisitos:** Java 21, Maven (via `mvnw` incluso) e acesso ao banco Oracle da FIAP.

1. Copie `.env.example` para `.env` e preencha as credenciais do Oracle:

   ```
   ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl
   ORACLE_USER=seu_rm
   ORACLE_PASSWORD=sua_senha
   ```

2. (Opcional) Execute o script `docs/database/script-oracle.sql` no Oracle SQL Developer para criar
   e popular as tabelas.

3. Suba a API em modo dev:

   - **Windows (PowerShell):** `./run-dev.ps1` — carrega o `.env` no ambiente e sobe o Quarkus.
   - **Outros:** exporte as variáveis do `.env` e rode `./mvnw quarkus:dev`.

A API sobe em `http://localhost:8080`. Teste com `requests.http` ou a coleção do Insomnia.

> Por padrão a API roda com `IA_MODO=MOCK` e `FONTE_CLIMA=SIMULADO` — **não precisa** da Flask nem
> de rede para demonstrar o fluxo completo de análise.

---

## 7. Variáveis de ambiente

| Variável | Obrigatória | Default | Descrição |
|---|---|---|---|
| `ORACLE_URL` | sim | — | URL JDBC do Oracle |
| `ORACLE_USER` | sim | — | Usuário do banco |
| `ORACLE_PASSWORD` | sim | — | Senha do banco |
| `IA_MODO` | não | `MOCK` | `MOCK` (avaliador interno) ou `REAL` (chama a Flask) |
| `IA_URL` | só se `IA_MODO=REAL` | — | URL completa do endpoint de predição da Flask |
| `FONTE_CLIMA` | não | `SIMULADO` | `SIMULADO` (cenários do banco) ou `OPEN_METEO` (API real) |
| `CENARIO_FIXO` | não | — | Fixa um cenário (id 1–6) no modo SIMULADO; sem isso, rotaciona |
| `PORT` | não | `8080` | Porta HTTP; em produção o Render injeta automaticamente |
| `FRONTEND_URL` | não | `http://localhost:5173` | Origem liberada no CORS (URL do front) |

> Detalhe do contrato Java ↔ IA em [`docs/contratos-backend-ia.md`](docs/contratos-backend-ia.md).

---

## 8. Decisões técnicas e aderência acadêmica

O projeto segue o **padrão das aulas** (1TDS): JDBC manual com `PreparedStatement` (sem JPA/Hibernate),
instanciação com `new` (sem `@Inject`/CDI), autenticação simples por hash **SHA-256** nativo (sem
JWT, sem BCrypt), **PK manual** nos INSERTs (sem SEQUENCE/TRIGGER) e SQL Oracle puro.

**Extensões conscientes** (fora do material das aulas, adicionadas por necessidade técnica):

- `exceptions/` + `OrbitaSafeExceptionMapper` — tratamento de exceções exigido pelo edital (mapeia
  400/401/404/409/500 com corpo `{ "erro": "..." }`).
- `dto/UsuarioRespostaDto` — para **nunca** expor o hash de senha em respostas.
- `CorsFilter` com **origem explícita** (sem `*`), lida de `FRONTEND_URL` com fallback local.
- **Conexão aberta/fechada por método** no DAO (não mantida em atributo) — evita vazar sessões
  Oracle (ORA-02391) num servidor de vida longa.
- Fonte de clima (`ServicoClima`) e IA (`IaService`) **intercambiáveis** via Factory + env var.
- `PORT` dinâmico no `application.properties` para deploy em PaaS (Render).
- **Deploy via Docker** (`Dockerfile` multi-stage na raiz) — o Render não tem runtime nativo de Java;
  o estágio 1 builda o JAR Quarkus com Maven e o estágio 2 roda só com JRE 21. Fixa a versão do Java
  na imagem. Detalhes em [`docs/deploy.md`](docs/deploy.md).

**Limitações conhecidas** (assumidas pelo escopo acadêmico):

- SHA-256 **sem salt** — vulnerável a rainbow table; em produção real seria BCrypt + salt.
- Autenticação **stateless** sem JWT — o front guarda o id do usuário para simular a sessão.
- **Sem transação explícita** no fluxo de análise (fora do escopo das aulas).
- `DELETE /regioes/{id}` de uma região **com alertas** resulta em erro de FK (ORA): o caminho
  esperado é remover os dependentes antes. Decisão consciente para manter o DAO no padrão simples.

---

## 9. Equipe, deploy e documentação

**Equipe (1TDS Agosto):**

| Nome | RM |
|---|---|
| _(preencher)_ | _(preencher)_ |
| _(preencher)_ | _(preencher)_ |
| _(preencher)_ | _(preencher)_ |

**Links:**

- Repositório GitHub: _(preencher)_
- API em produção (Render): _(preencher após deploy)_
- Front-end (Vercel): _(preencher quando publicado)_

**Documentação relacionada:**

- [`docs/contrato-api.md`](docs/contrato-api.md) — referência completa da API para o front-end.
- [`docs/contratos-backend-ia.md`](docs/contratos-backend-ia.md) — contrato Java ↔ Flask (Contrato 1).
- [`docs/deploy.md`](docs/deploy.md) — passo a passo do deploy no Render (via Docker).
- [`docs/insomnia-collection.yaml`](docs/insomnia-collection.yaml) — coleção testável (29 requisições).
- [`docs/database/script-oracle.sql`](docs/database/script-oracle.sql) — DDL, DML, consultas e relatórios.

---

*OrbitaSafe · Global Solution 2026/1 · FIAP · 1TDS Agosto · Startup: Orbit Analytics*
