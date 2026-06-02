# Contrato da API — OrbitaSafe

> Documentação de consumo da API REST do OrbitaSafe.
> **Fonte de verdade: o código.** Tudo aqui foi extraído dos `Resources`, `entities`,
> `exceptions` e `services` reais. Se algum comportamento divergir deste documento,
> o código vence — e este documento deve ser corrigido.

Público-alvo: dev de front-end (React + Vite + TS) e qualquer integrante novo da equipe.

---

## Seção 1 — Visão geral

API REST que recebe regiões monitoradas, dispara análise de risco climático (via modelos de IA) e expõe os alertas e notificações resultantes para o front-end.

**Stack:**

| Item | Tecnologia |
|---|---|
| Framework | Quarkus 3.34.6 |
| Linguagem | Java 21 |
| Banco | Oracle Database (JDBC manual, sem JPA) |
| Serialização | JSON via `quarkus-rest-jackson` |

**URL base:**

- Local: `http://localhost:8080`
- Produção: `(placeholder — preencher após deploy da Fase 7)`

**Formato:**

- Todo corpo de requisição e resposta é **JSON, UTF-8**.
- Requisições com body **devem** enviar o header `Content-Type: application/json`.
- Datas trafegam como **string ISO** (`"2026-05-29"`) — JSON não tem tipo nativo de data.

**CORS** (lido de `CorsFilter.java` — esta é a configuração real, não a planejada):

| Header | Valor configurado |
|---|---|
| `Access-Control-Allow-Origin` | `http://localhost:5173` |
| `Access-Control-Allow-Methods` | `GET, POST, PUT, DELETE, OPTIONS, HEAD` |
| `Access-Control-Allow-Headers` | `origin, content-type, accept, authorization` |

> ⚠️ Apenas a origem `http://localhost:5173` (Vite dev) está liberada hoje. **Não há**
> `Access-Control-Allow-Credentials`. Quando o front for publicado na Vercel, a URL de
> produção precisa ser adicionada no `CorsFilter` (há um `TODO` no código nesse sentido).

---

## Seção 2 — Índice de endpoints

| Método | Path | O que faz | Seção |
|---|---|---|---|
| POST | `/auth/cadastro` | Cadastra usuário | §4.1 |
| POST | `/auth/login` | Autentica e retorna o usuário (sem senha) | §4.1 |
| GET | `/usuarios` | Lista todos os usuários (sem senha) | §4.2 |
| GET | `/usuarios/{id}` | Busca usuário por id | §4.2 |
| PUT | `/usuarios/{id}` | Atualiza usuário | §4.2 |
| DELETE | `/usuarios/{id}` | Remove usuário | §4.2 |
| POST | `/regioes` | Cadastra região **e dispara a 1ª análise** | §4.3 |
| GET | `/regioes` | Lista todas as regiões | §4.3 |
| GET | `/regioes/{id}` | Busca região por id | §4.3 |
| PUT | `/regioes/{id}` | Atualiza região | §4.3 |
| DELETE | `/regioes/{id}` | Remove região | §4.3 |
| POST | `/regioes/{id}/analisar` | Dispara análise de risco sob demanda | §4.3 |
| GET | `/usuarios/{id}/regioes` | Lista regiões de um usuário | §4.3 |
| GET | `/regioes/{id}/alertas` | Histórico de alertas de uma região | §4.5 |
| GET | `/usuarios/{id}/notificacoes` | Todas as notificações de um usuário | §4.6 |
| GET | `/usuarios/{id}/notificacoes/nao-lidas` | Notificações não lidas | §4.6 |
| PUT | `/notificacoes/{id}/marcar-lida` | Marca notificação como lida | §4.6 |
| GET | `/subprefeituras` | Lista as 32 subprefeituras | §4.4 |
| GET | `/subprefeituras/{id}` | Busca subprefeitura por id | §4.4 |

**Total: 19 endpoints.**

---

## Seção 3 — Autenticação e fluxo geral

A API é **stateless**. Não há JWT, não há cookies, não há sessão server-side.

**Como funciona o login:**

1. O front envia `POST /auth/login` com `emailUsu` e `senhaUsu`.
2. A API gera o hash SHA-256 da senha enviada e compara com o hash armazenado.
3. Se confere, retorna `200` com o usuário **sem o campo de senha** (`UsuarioRespostaDto`).
4. Se não confere (ou e-mail inexistente), retorna `401`.

**Responsabilidade do front:**

- Guardar o usuário logado (tipicamente `localStorage` ou um Context do React).
- Enviar o `idUsu` no corpo/parâmetro das chamadas que precisam saber de quem é a ação
  (ex.: cadastrar região com `fkUsuarioIdUsu`, listar notificações por usuário).

**Não há proteção de rota no back-end nesta versão acadêmica.** Qualquer endpoint pode ser
chamado sem autenticação. O controle de acesso é simulado no front.

> ⚠️ **PK manual:** O banco não usa sequence/auto-increment (padrão das aulas). Isso significa
> que **o `id` é enviado pelo front no corpo do POST** (`idUsu` no cadastro, `idReg` na região).
> Não é o servidor que gera o id. Veja os detalhes em §4.

---

## Seção 4 — Endpoints detalhados

### §4.1 Autenticação

---

#### POST /auth/cadastro

**O que faz:** cria um novo usuário. A senha é convertida em hash SHA-256 antes de persistir.

**Request — Body JSON:**

```json
{
  "idUsu": 100,
  "nmUsu": "Ana Souza",
  "emailUsu": "ana@orbitasafe.com",
  "senhaUsu": "senha123",
  "tpUsu": "USER",
  "dtCadastro": "2026-05-29"
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `idUsu` | number | sim | PK manual — definido pelo front |
| `nmUsu` | string | sim | não pode ser vazio/branco |
| `emailUsu` | string | sim | deve conter `@`; deve ser único |
| `senhaUsu` | string | sim | mínimo 6 caracteres |
| `tpUsu` | string | sim | texto livre (não validado no back — ver §4 nota e FAQ) |
| `dtCadastro` | string (ISO) | não | se ausente, o back usa a data atual |

**Response — 201 Created:**

- Header `Location`: `http://localhost:8080/usuarios/{idUsu}`
- Body: o usuário criado, **sem a senha** (`UsuarioRespostaDto`).

```json
{
  "idUsu": 100,
  "nmUsu": "Ana Souza",
  "emailUsu": "ana@orbitasafe.com",
  "tpUsu": "USER",
  "dtCadastro": "2026-05-29"
}
```

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 400 | nome vazio, e-mail sem `@`, senha < 6 | `{ "erro": "Senha deve ter no minimo 6 caracteres." }` |
| 409 | e-mail já cadastrado | `{ "erro": "E-mail ja cadastrado." }` |

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/auth/cadastro', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    idUsu: 100,
    nmUsu: 'Ana Souza',
    emailUsu: 'ana@orbitasafe.com',
    senhaUsu: 'senha123',
    tpUsu: 'USER',
    dtCadastro: '2026-05-29'
  })
});
const usuario = await resp.json();
```

---

#### POST /auth/login

**O que faz:** autentica por e-mail + senha. Retorna o usuário sem a senha.

**Request — Body JSON:**

```json
{
  "emailUsu": "ana@orbitasafe.com",
  "senhaUsu": "senha123"
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `emailUsu` | string | sim | — |
| `senhaUsu` | string | sim | — |

> O endpoint aceita um objeto `Usuario` completo, mas só usa `emailUsu` e `senhaUsu`.
> Enviar apenas esses dois campos é suficiente.

**Response — 200 OK:**

```json
{
  "idUsu": 1,
  "nmUsu": "Ana Souza",
  "emailUsu": "ana@orbitasafe.com",
  "tpUsu": "USER",
  "dtCadastro": "2026-05-29"
}
```

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 401 | e-mail inexistente OU senha errada | `{ "erro": "E-mail ou senha invalidos." }` |

> Por segurança, e-mail inexistente e senha errada retornam **o mesmo** 401 (não vaza qual dos dois falhou).

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ emailUsu: 'ana@orbitasafe.com', senhaUsu: 'senha123' })
});
if (!resp.ok) {
  const { erro } = await resp.json();
  throw new Error(erro);
}
const usuario = await resp.json();
localStorage.setItem('usuario', JSON.stringify(usuario));
```

---

### §4.2 Usuários

---

#### GET /usuarios

**O que faz:** lista todos os usuários, sem o campo de senha.

**Response — 200 OK:** array de `UsuarioRespostaDto`.

```json
[
  { "idUsu": 1, "nmUsu": "Ana Souza", "emailUsu": "ana@orbitasafe.com", "tpUsu": "USER", "dtCadastro": "2026-05-29" },
  { "idUsu": 2, "nmUsu": "Bruno Lima", "emailUsu": "bruno@orbitasafe.com", "tpUsu": "USER", "dtCadastro": "2026-05-30" }
]
```

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/usuarios');
const usuarios = await resp.json();
```

---

#### GET /usuarios/{id}

**O que faz:** busca um usuário por id.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id do usuário (`idUsu`) |

**Response — 200 OK:** um `UsuarioRespostaDto` (sem senha).

```json
{ "idUsu": 1, "nmUsu": "Ana Souza", "emailUsu": "ana@orbitasafe.com", "tpUsu": "USER", "dtCadastro": "2026-05-29" }
```

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | id inexistente | `{ "erro": "Usuario nao encontrado: id=9999" }` |

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/usuarios/1');
if (resp.status === 404) throw new Error('Usuário não encontrado');
const usuario = await resp.json();
```

---

#### PUT /usuarios/{id}

**O que faz:** atualiza os dados de um usuário. O `id` do path sobrescreve o do corpo.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id do usuário a atualizar |

**Request — Body JSON:**

```json
{
  "nmUsu": "Ana Souza Atualizada",
  "emailUsu": "ana@orbitasafe.com",
  "tpUsu": "USER"
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `nmUsu` | string | sim | não pode ser vazio/branco |
| `emailUsu` | string | sim | deve conter `@` |
| `senhaUsu` | string | não | se enviada e não-vazia, é re-hasheada; se ausente/vazia, mantém a senha atual |
| `tpUsu` | string | não | texto livre |
| `dtCadastro` | string (ISO) | não | se ausente, preserva a data já gravada |

**Response — 200 OK:** o usuário atualizado, **sem a senha** (`UsuarioRespostaDto`).

```json
{ "idUsu": 1, "nmUsu": "Ana Souza Atualizada", "emailUsu": "ana@orbitasafe.com", "tpUsu": "USER", "dtCadastro": "2026-05-29" }
```

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 400 | nome vazio ou e-mail sem `@` | `{ "erro": "E-mail invalido." }` |
| 404 | id inexistente | `{ "erro": "Usuario nao encontrado: id=9999" }` |

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/usuarios/1', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ nmUsu: 'Ana Souza Atualizada', emailUsu: 'ana@orbitasafe.com', tpUsu: 'USER' })
});
const usuario = await resp.json();
```

---

#### DELETE /usuarios/{id}

**O que faz:** remove um usuário.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id do usuário a remover |

**Response — 204 No Content:** sem corpo.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | id inexistente | `{ "erro": "Usuario nao encontrado: id=9999" }` |

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/usuarios/1', { method: 'DELETE' });
if (resp.status === 204) console.log('removido');
```

---

### §4.3 Regiões

---

#### POST /regioes

**O que faz:** cadastra uma região monitorada **e dispara automaticamente a 1ª análise de
risco** (busca clima → grava leitura → chama IA → grava alerta → cria notificação se risco
MEDIO/ALTO). Veja o fluxo completo em §8.

**Request — Body JSON:**

```json
{
  "idReg": 200,
  "nmReg": "Centro - Casa da Ana",
  "dtCadastro": "2026-05-29",
  "fkUsuarioIdUsu": 1,
  "fkSubprefeituraIdSubpref": 1
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `idReg` | number | sim | PK manual — definido pelo front |
| `nmReg` | string | sim | não pode ser vazio/branco |
| `dtCadastro` | string (ISO) | não | se ausente, usa a data atual |
| `fkUsuarioIdUsu` | number | sim | o usuário precisa existir |
| `fkSubprefeituraIdSubpref` | number | sim | a subprefeitura precisa existir |

**Response — 201 Created:**

- Header `Location`: `http://localhost:8080/regioes/{idReg}`
- Body: a **região criada** (entidade `Regiao`), **não** o alerta gerado.

```json
{
  "idReg": 200,
  "nmReg": "Centro - Casa da Ana",
  "dtCadastro": "2026-05-29",
  "fkUsuarioIdUsu": 1,
  "fkSubprefeituraIdSubpref": 1
}
```

> ⚠️ **Importante para o front:** a resposta do POST traz a região, **não** o resultado da
> análise. Para ver o alerta gerado, chame em seguida `GET /regioes/{id}/alertas`; para ver
> se gerou notificação, `GET /usuarios/{id}/notificacoes`.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 400 | nome vazio, usuário inexistente ou subprefeitura inexistente | `{ "erro": "Subprefeitura informada nao existe." }` |
| 500 | falha na análise (ver FAQ sobre IA fora do ar) | `{ "erro": "Erro interno no servidor" }` |

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/regioes', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    idReg: 200,
    nmReg: 'Centro - Casa da Ana',
    fkUsuarioIdUsu: 1,
    fkSubprefeituraIdSubpref: 1
  })
});
const regiao = await resp.json();
// busca o alerta gerado pela análise automática:
const alertas = await (await fetch(`http://localhost:8080/regioes/${regiao.idReg}/alertas`)).json();
```

---

#### GET /regioes

**O que faz:** lista todas as regiões.

**Response — 200 OK:** array de `Regiao`.

```json
[
  { "idReg": 1, "nmReg": "Sé", "dtCadastro": "2026-05-29", "fkUsuarioIdUsu": 1, "fkSubprefeituraIdSubpref": 1 }
]
```

---

#### GET /regioes/{id}

**O que faz:** busca uma região por id.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id da região (`idReg`) |

**Response — 200 OK:** uma `Regiao`.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | id inexistente | `{ "erro": "Regiao nao encontrada: id=9999" }` |

---

#### PUT /regioes/{id}

**O que faz:** atualiza uma região. O `id` do path sobrescreve o do corpo. **Não dispara análise.**

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id da região a atualizar |

**Request — Body JSON:**

```json
{
  "nmReg": "Centro Atualizado",
  "fkUsuarioIdUsu": 1,
  "fkSubprefeituraIdSubpref": 1
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `nmReg` | string | sim | não pode ser vazio/branco |
| `dtCadastro` | string (ISO) | não | se ausente, preserva a data de criação já gravada |
| `fkUsuarioIdUsu` | number | sim | — |
| `fkSubprefeituraIdSubpref` | number | sim | — |

**Response — 200 OK:** a `Regiao` atualizada.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 400 | nome vazio | `{ "erro": "Nome da regiao e obrigatorio." }` |
| 404 | id inexistente | `{ "erro": "Regiao nao encontrada: id=9999" }` |

---

#### DELETE /regioes/{id}

**O que faz:** remove uma região.

**Response — 204 No Content:** sem corpo.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | id inexistente | `{ "erro": "Regiao nao encontrada: id=9999" }` |

---

#### POST /regioes/{id}/analisar

**O que faz:** dispara uma análise de risco sob demanda (o "botão atualizar" do front). Executa o
mesmo fluxo da análise automática do cadastro.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id da região a analisar |

**Request:** sem body.

**Response — 200 OK:** o `Alerta` gerado por esta análise.

```json
{
  "idAlerta": 1234567,
  "nivelAlerta": "ALTO",
  "tpEvento": "TEMPESTADE",
  "dsAlerta": "Risco alto de tempestade severa",
  "dtAlerta": "2026-06-02",
  "fkRegiaoIdReg": 1,
  "fkLeituraIdLeitura": 1234566
}
```

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | região inexistente | `{ "erro": "Regiao nao encontrada: id=9999" }` |
| 500 | falha na IA em modo REAL (ver FAQ) | `{ "erro": "Erro interno no servidor" }` |

**Exemplo (fetch):**

```javascript
const resp = await fetch('http://localhost:8080/regioes/1/analisar', { method: 'POST' });
const alerta = await resp.json();
if (alerta.nivelAlerta !== 'BAIXO') {
  // houve notificação; atualize a caixa de entrada do usuário
}
```

---

#### GET /usuarios/{id}/regioes

**O que faz:** lista as regiões cadastradas por um usuário específico.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id do usuário |

**Response — 200 OK:** array de `Regiao` (vazio se o usuário não tiver regiões).

```javascript
const regioes = await (await fetch('http://localhost:8080/usuarios/1/regioes')).json();
```

---

### §4.4 Subprefeituras

---

#### GET /subprefeituras

**O que faz:** lista as 32 subprefeituras de São Paulo. Usado para popular o `<select>` do
formulário de cadastro de região.

**Response — 200 OK:** array de `Subprefeitura`.

```json
[
  { "idSubpref": 1, "cdSubpref": 101, "nmSubpref": "Sé", "latitudeSubpref": -23.55, "longitudeSubpref": -46.63, "qtAlagamento": 12 }
]
```

**Exemplo (fetch):**

```javascript
const subprefeituras = await (await fetch('http://localhost:8080/subprefeituras')).json();
```

---

#### GET /subprefeituras/{id}

**O que faz:** busca uma subprefeitura por id.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id da subprefeitura (`idSubpref`) |

**Response — 200 OK:** uma `Subprefeitura`.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | id inexistente | `{ "erro": "Subprefeitura nao encontrada: id=9999" }` |

---

### §4.5 Alertas

---

#### GET /regioes/{id}/alertas

**O que faz:** retorna o histórico de alertas de uma região (todas as análises já feitas,
inclusive risco BAIXO).

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id da região |

**Response — 200 OK:** array de `Alerta` (vazio se a região nunca foi analisada).

```json
[
  {
    "idAlerta": 1234567,
    "nivelAlerta": "MEDIO",
    "tpEvento": "ALAGAMENTO",
    "dsAlerta": "Risco moderado de alagamento",
    "dtAlerta": "2026-06-02",
    "fkRegiaoIdReg": 1,
    "fkLeituraIdLeitura": 1234566
  }
]
```

**Exemplo (fetch):**

```javascript
const alertas = await (await fetch('http://localhost:8080/regioes/1/alertas')).json();
```

---

### §4.6 Notificações

---

#### GET /usuarios/{id}/notificacoes

**O que faz:** lista todas as notificações de um usuário (lidas e não lidas).

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id do usuário |

**Response — 200 OK:** array de `Notificacao`.

```json
[
  {
    "idNotif": 1234568,
    "dsNotif": "Risco ALTO de tempestade na sua região Centro",
    "dtNotif": "2026-06-02",
    "estadoNotif": "NAO_LIDA",
    "fkUsuarioIdUsu": 1,
    "fkAlertaIdAlerta": 1234567
  }
]
```

---

#### GET /usuarios/{id}/notificacoes/nao-lidas

**O que faz:** lista apenas as notificações com `estadoNotif = "NAO_LIDA"`. Útil para o badge
de contador no front.

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id do usuário |

**Response — 200 OK:** array de `Notificacao` (apenas não lidas).

```javascript
const naoLidas = await (await fetch('http://localhost:8080/usuarios/1/notificacoes/nao-lidas')).json();
const badge = naoLidas.length;
```

---

#### PUT /notificacoes/{id}/marcar-lida

**O que faz:** marca uma notificação como lida (`estadoNotif` → `"LIDA"`).

**Path params:**

| Nome | Tipo | Descrição |
|---|---|---|
| `id` | number | id da notificação (`idNotif`) |

**Request:** sem body.

**Response — 200 OK:** sem corpo.

**Possíveis erros:**

| Status | Quando | Body |
|---|---|---|
| 404 | id inexistente | `{ "erro": "Notificacao nao encontrada: 9999" }` |

**Exemplo (fetch):**

```javascript
await fetch('http://localhost:8080/notificacoes/1234568/marcar-lida', { method: 'PUT' });
```

---

## Seção 5 — Modelos de dados (referência)

Convenção: os nomes dos campos JSON espelham os atributos Java, que por sua vez espelham as
colunas do banco. Datas (`LocalDate`) são serializadas como string ISO `"YYYY-MM-DD"`.

### Usuario (resposta — `UsuarioRespostaDto`, sem senha)

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idUsu` | int | number | não | PK |
| `nmUsu` | String | string | não | nome |
| `emailUsu` | String | string | não | e-mail único |
| `tpUsu` | String | string | não | tipo do usuário (texto livre) |
| `dtCadastro` | LocalDate | string | sim | data de cadastro (ISO) |

> O campo `senhaUsu` existe na entidade `Usuario` mas **nunca** é retornado por nenhum endpoint.

```json
{ "idUsu": 1, "nmUsu": "Ana Souza", "emailUsu": "ana@orbitasafe.com", "tpUsu": "USER", "dtCadastro": "2026-05-29" }
```

### Regiao

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idReg` | int | number | não | PK |
| `nmReg` | String | string | não | nome dado pelo usuário |
| `dtCadastro` | LocalDate | string | sim | data de cadastro (ISO) |
| `fkUsuarioIdUsu` | int | number | não | FK → Usuario |
| `fkSubprefeituraIdSubpref` | int | number | não | FK → Subprefeitura |

```json
{ "idReg": 1, "nmReg": "Sé", "dtCadastro": "2026-05-29", "fkUsuarioIdUsu": 1, "fkSubprefeituraIdSubpref": 1 }
```

### Subprefeitura

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idSubpref` | int | number | não | PK |
| `cdSubpref` | int | number | não | código estável usado pela IA |
| `nmSubpref` | String | string | não | nome |
| `latitudeSubpref` | double | number | não | latitude do centroide |
| `longitudeSubpref` | double | number | não | longitude do centroide |
| `qtAlagamento` | int | number | não | histórico de alagamentos |

```json
{ "idSubpref": 1, "cdSubpref": 101, "nmSubpref": "Sé", "latitudeSubpref": -23.55, "longitudeSubpref": -46.63, "qtAlagamento": 12 }
```

### LeituraClimatica

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idLeitura` | int | number | não | PK |
| `precipitacaoLeitura` | double | number | não | precipitação (mm) |
| `umidadeLeitura` | double | number | não | umidade relativa (%) |
| `pressaoLeitura` | double | number | não | pressão (hPa) |
| `ventoLeitura` | double | number | não | vento (km/h) |
| `temperaturaLeitura` | double | number | não | temperatura (°C) |
| `umidSoloLeitura` | double | number | não | umidade do solo (m³/m³) |
| `dtLeitura` | LocalDate | string | sim | data da leitura (ISO) |
| `diaPrevisao` | int | number | não | dia da previsão |
| `fkRegiaoIdReg` | int | number | não | FK → Regiao |

```json
{ "idLeitura": 1, "precipitacaoLeitura": 12.4, "umidadeLeitura": 88.0, "pressaoLeitura": 1008.0, "ventoLeitura": 30.0, "temperaturaLeitura": 22.5, "umidSoloLeitura": 0.42, "dtLeitura": "2026-06-02", "diaPrevisao": 0, "fkRegiaoIdReg": 1 }
```

### Alerta

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idAlerta` | int | number | não | PK |
| `nivelAlerta` | String | `'BAIXO' \| 'MEDIO' \| 'ALTO'` | não | nível de risco |
| `tpEvento` | String | string | não | tipo de evento (tempestade, vento, calor, alagamento) |
| `dsAlerta` | String | string | não | descrição |
| `dtAlerta` | LocalDate | string | sim | data do alerta (ISO) |
| `fkRegiaoIdReg` | int | number | não | FK → Regiao |
| `fkLeituraIdLeitura` | int | number | não | FK → LeituraClimatica |

```json
{ "idAlerta": 1234567, "nivelAlerta": "ALTO", "tpEvento": "TEMPESTADE", "dsAlerta": "Risco alto de tempestade severa", "dtAlerta": "2026-06-02", "fkRegiaoIdReg": 1, "fkLeituraIdLeitura": 1234566 }
```

### Notificacao

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idNotif` | int | number | não | PK |
| `dsNotif` | String | string | não | texto da notificação |
| `dtNotif` | LocalDate | string | sim | data (ISO) |
| `estadoNotif` | String | `'LIDA' \| 'NAO_LIDA'` | não | estado de leitura |
| `fkUsuarioIdUsu` | int | number | não | FK → Usuario |
| `fkAlertaIdAlerta` | int | number | não | FK → Alerta |

```json
{ "idNotif": 1234568, "dsNotif": "Risco ALTO na sua região Centro", "dtNotif": "2026-06-02", "estadoNotif": "NAO_LIDA", "fkUsuarioIdUsu": 1, "fkAlertaIdAlerta": 1234567 }
```

### ModeloIa

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idModelo` | int | number | não | PK |
| `nmModelo` | String | string | não | nome do modelo |
| `tpModelo` | String | string | não | tipo (classificador / regressor) |
| `versaoModelo` | String | string | não | versão |

```json
{ "idModelo": 1, "nmModelo": "Classificador de Risco", "tpModelo": "CLASSIFICACAO", "versaoModelo": "1.0" }
```

### CenarioClimatico

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `idCenario` | int | number | não | PK |
| `nmCenario` | String | string | não | nome do cenário |
| `precipitacaoCenario` | double | number | não | precipitação (mm) |
| `umidadeCenario` | double | number | não | umidade (%) |
| `pressaoCenario` | double | number | não | pressão (hPa) |
| `ventoCenario` | double | number | não | vento (km/h) |
| `temperaturaCenario` | double | number | não | temperatura (°C) |
| `umidSoloCenario` | double | number | não | umidade do solo (m³/m³) |
| `nivelCenario` | String | `'BAIXO' \| 'MEDIO' \| 'ALTO'` | não | nível de risco do cenário |

```json
{ "idCenario": 5, "nmCenario": "Temporal Intenso", "precipitacaoCenario": 60.0, "umidadeCenario": 95.0, "pressaoCenario": 1000.0, "ventoCenario": 70.0, "temperaturaCenario": 19.0, "umidSoloCenario": 0.6, "nivelCenario": "ALTO" }
```

### AlertaModelo (associativa N:N)

| Campo | Java | TypeScript | Nullable | Descrição |
|---|---|---|---|---|
| `fkAlertaIdAlerta` | int | number | não | FK → Alerta (parte da PK composta) |
| `fkModeloIdModelo` | int | number | não | FK → ModeloIa (parte da PK composta) |
| `scoreModelo` | double | number | não | score atribuído pelo modelo |

```json
{ "fkAlertaIdAlerta": 1234567, "fkModeloIdModelo": 1, "scoreModelo": 0.87 }
```

> `ModeloIa`, `CenarioClimatico` e `AlertaModelo` não têm endpoint REST dedicado nesta versão —
> são modelos internos/de apoio. Estão documentados aqui para referência completa do domínio.

### Interfaces TypeScript (copiar e colar)

```typescript
// Usuario retornado pela API (sem senha) — /usuarios, /auth/*
export interface UsuarioResposta {
  idUsu: number;
  nmUsu: string;
  emailUsu: string;
  tpUsu: string;        // texto livre no back; o seed usa "USER"
  dtCadastro: string;   // ISO date "YYYY-MM-DD"
}

// Corpo de cadastro de usuário (POST /auth/cadastro)
export interface UsuarioCadastro {
  idUsu: number;        // PK manual — você define
  nmUsu: string;
  emailUsu: string;     // deve conter "@", único
  senhaUsu: string;     // mínimo 6 caracteres
  tpUsu: string;
  dtCadastro?: string;  // opcional; back usa hoje se ausente
}

// Região
export interface Regiao {
  idReg: number;        // PK manual no POST
  nmReg: string;
  dtCadastro?: string;  // ISO date
  fkUsuarioIdUsu: number;
  fkSubprefeituraIdSubpref: number;
}

// Subprefeitura (domínio — usada no select do formulário)
export interface Subprefeitura {
  idSubpref: number;
  cdSubpref: number;
  nmSubpref: string;
  latitudeSubpref: number;
  longitudeSubpref: number;
  qtAlagamento: number;
}

// Leitura climática
export interface LeituraClimatica {
  idLeitura: number;
  precipitacaoLeitura: number;
  umidadeLeitura: number;
  pressaoLeitura: number;
  ventoLeitura: number;
  temperaturaLeitura: number;
  umidSoloLeitura: number;
  dtLeitura: string;    // ISO date
  diaPrevisao: number;
  fkRegiaoIdReg: number;
}

// Alerta (histórico de risco)
export interface Alerta {
  idAlerta: number;
  nivelAlerta: 'BAIXO' | 'MEDIO' | 'ALTO';
  tpEvento: string;
  dsAlerta: string;
  dtAlerta: string;     // ISO date
  fkRegiaoIdReg: number;
  fkLeituraIdLeitura: number;
}

// Notificação (caixa de entrada do usuário)
export interface Notificacao {
  idNotif: number;
  dsNotif: string;
  dtNotif: string;      // ISO date
  estadoNotif: 'LIDA' | 'NAO_LIDA';
  fkUsuarioIdUsu: number;
  fkAlertaIdAlerta: number;
}

// Modelo de IA (referência interna)
export interface ModeloIa {
  idModelo: number;
  nmModelo: string;
  tpModelo: string;
  versaoModelo: string;
}

// Cenário climático simulado (referência interna)
export interface CenarioClimatico {
  idCenario: number;
  nmCenario: string;
  precipitacaoCenario: number;
  umidadeCenario: number;
  pressaoCenario: number;
  ventoCenario: number;
  temperaturaCenario: number;
  umidSoloCenario: number;
  nivelCenario: 'BAIXO' | 'MEDIO' | 'ALTO';
}

// Associativa Alerta x Modelo (referência interna)
export interface AlertaModelo {
  fkAlertaIdAlerta: number;
  fkModeloIdModelo: number;
  scoreModelo: number;
}

// Formato padrão de erro de qualquer endpoint
export interface ErroApi {
  erro: string;
}
```

---

## Seção 6 — Caminho de erro padrão

Todo erro tratado retorna o **mesmo formato** de corpo:

```json
{ "erro": "mensagem descritiva aqui" }
```

Mapeamento real (de `OrbitaSafeExceptionMapper.java`):

| Status | Exceção | Quando acontece |
|---|---|---|
| 400 | `ValidacaoException` | dado inválido na entrada (nome vazio, e-mail sem `@`, senha < 6, FK inexistente no cadastro de região) |
| 401 | `CredenciaisInvalidasException` | e-mail ou senha incorretos no login |
| 404 | `RegistroNaoEncontradoException` | busca/atualização/remoção de id inexistente |
| 409 | `EmailJaCadastradoException` | tentativa de cadastrar e-mail já existente |
| 500 | (qualquer outra `RuntimeException`) | erro interno; corpo fixo `{ "erro": "Erro interno no servidor" }` |

**Como o front deve tratar:** sempre checar `response.ok` e, em caso de falha, ler `body.erro`
para exibir ao usuário.

```javascript
async function chamarApi(url, options) {
  const resp = await fetch(url, options);
  if (!resp.ok) {
    const { erro } = await resp.json().catch(() => ({ erro: 'Erro desconhecido' }));
    throw new Error(erro);
  }
  return resp.status === 204 ? null : resp.json();
}
```

---

## Seção 7 — Variáveis de ambiente

### Banco de dados (obrigatórias)

| Variável | Conteúdo |
|---|---|
| `ORACLE_URL` | URL JDBC do Oracle (ex.: `jdbc:oracle:thin:@//host:porta/service`) |
| `ORACLE_USER` | usuário do banco |
| `ORACLE_PASSWORD` | senha do banco |

### Fonte de clima

| Variável | Valores | Default | Efeito |
|---|---|---|---|
| `FONTE_CLIMA` | `OPEN_METEO` \| `SIMULADO` | `SIMULADO` | escolhe entre a API Open-Meteo real e os cenários do banco |
| `CENARIO_FIXO` | número (id de cenário) | (não setada) | quando setada, ignora a rotação e usa sempre o cenário com esse id (só vale para `SIMULADO`) |

No modo `SIMULADO` sem `CENARIO_FIXO`, os cenários são percorridos em **rodízio** (round-robin)
a cada análise. Cenários disponíveis:

| id | Nível | Cenário |
|---|---|---|
| 1 | BAIXO | Tempo Seco |
| 2 | BAIXO | Chuva Leve |
| 3 | MEDIO | Chuva Moderada |
| 4 | MEDIO | Frente Fria |
| 5 | ALTO | Temporal Intenso |
| 6 | ALTO | Tempestade Severa |

> Se `CENARIO_FIXO` apontar para um id que não existe no banco, a análise falha com erro
> (resulta em 500 no endpoint).

### IA

| Variável | Valores | Default | Efeito |
|---|---|---|---|
| `IA_MODO` | `REAL` \| `MOCK` | `MOCK` | `MOCK` usa um avaliador interno; `REAL` chama a Flask da equipe de IA |
| `IA_URL` | URL da API Flask | (não setada) | **obrigatória quando `IA_MODO=REAL`** — sem ela, a análise falha |

> Enquanto a Flask da equipe de IA não estiver pronta, mantenha `IA_MODO=MOCK` (default).

---

## Seção 8 — Roteiro de testes / demo

### Subir a API local

```bash
mvn quarkus:dev
```

A API sobe em `http://localhost:8080`. Garanta que as variáveis do Oracle estão setadas no `.env`.

### Cadastro + login (sequência de 2 chamadas)

```bash
# 1. Cadastro
curl -X POST http://localhost:8080/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"idUsu":100,"nmUsu":"Ana Souza","emailUsu":"ana@orbitasafe.com","senhaUsu":"senha123","tpUsu":"USER"}'

# 2. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailUsu":"ana@orbitasafe.com","senhaUsu":"senha123"}'
```

### Criar região e ver a análise automática (1 chamada dispara o fluxo completo)

```bash
curl -X POST http://localhost:8080/regioes \
  -H "Content-Type: application/json" \
  -d '{"idReg":200,"nmReg":"Centro - Casa da Ana","fkUsuarioIdUsu":100,"fkSubprefeituraIdSubpref":1}'

# Ver o alerta gerado:
curl http://localhost:8080/regioes/200/alertas
# Ver se gerou notificação:
curl http://localhost:8080/usuarios/100/notificacoes
```

### Demonstrar caminho ALTO vs BAIXO

O nível de risco depende do cenário climático. Para controlar a demo, fixe o cenário antes de
subir a API:

```bash
# Caminho ALTO — Temporal Intenso: gera alerta ALTO + notificação
CENARIO_FIXO=5 mvn quarkus:dev

# Caminho BAIXO — Tempo Seco: gera alerta BAIXO e NÃO gera notificação (por design)
CENARIO_FIXO=1 mvn quarkus:dev
```

> **Design importante:** risco **BAIXO sempre gera o alerta** (fica no histórico em
> `/regioes/{id}/alertas`), mas **não gera notificação**. Só MEDIO e ALTO criam notificação na
> caixa de entrada do usuário. Mostre os dois caminhos na apresentação para evidenciar a regra.

No Windows PowerShell, setar a variável antes de subir:

```powershell
$env:CENARIO_FIXO = "5"; mvn quarkus:dev
```

---

## Seção 9 — FAQ

**Por que não tem JWT?**
Projeto acadêmico com autenticação stateless. O front guarda o usuário logado (após o login) e
envia o `idUsu` quando necessário. Não há sessão server-side nem token.

**Por que algumas datas vêm como string?**
JSON não tem tipo `Date`. As datas (`LocalDate` no Java) trafegam em formato ISO `"2026-05-29"`.
No front, parseie com `new Date(string)` quando precisar de objeto Date.

**Por que `idUsu` e não `id`?**
A nomenclatura espelha as colunas do banco (padrão das aulas: `id_usu`, `nm_usu`, etc.). Isso
vale para todos os modelos (`idReg`, `idAlerta`, `fkUsuarioIdUsu`...).

**Preciso mesmo enviar o `id` no POST?**
Sim. O banco usa PK manual (sem sequence/auto-increment). No `POST /auth/cadastro` você envia
`idUsu`; no `POST /regioes` você envia `idReg`. O servidor não gera o id.

**`POST /regioes` dispara análise automaticamente?**
Sim. No ato do cadastro a 1ª análise de risco roda (clima → IA → alerta → notificação se
MEDIO/ALTO). A resposta do POST traz a região; para ver o resultado da análise, consulte
`/regioes/{id}/alertas` e `/usuarios/{id}/notificacoes`.

**O que acontece se a IA estiver fora do ar?**
Em `IA_MODO=MOCK` (default) não há dependência externa — não é problema. Em `IA_MODO=REAL`, se a
Flask estiver indisponível ou `IA_URL` não estiver configurada, a chamada falha e o endpoint
retorna 500.

**CORS vai quebrar meu front em produção?**
Hoje só `http://localhost:5173` está liberado (ver Seção 1). Quando o front for publicado, é
preciso adicionar a URL de produção no `CorsFilter`. Até lá, o front em produção será bloqueado
pelo navegador.

---

## Seção 10 — Contato e referências

- **Repositório:** `(placeholder — preencher com o link do GitHub)`
- **Equipe back-end:** `(placeholder — preencher com nomes e RMs)`
- **URL de produção da API:** `(placeholder — preencher após deploy)`

**Mudanças no contrato da API:** se você (front ou IA) precisar de um campo novo, um endpoint
diferente ou uma mudança de formato, **abra uma issue no repositório** descrevendo o caso. Assim
a mudança fica registrada e versionada junto do código. Mensagem solta no WhatsApp se perde e
ninguém consegue rastrear depois — a issue é o caminho que mantém todo mundo na mesma página. 🙂
