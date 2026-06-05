# Contrato Backend (Java) ↔ IA (Flask) — OrbitaSafe

> Contrato de integração entre a **API Java (Quarkus)** e o **serviço de IA (Flask)**.
> **Fonte de verdade: o código.** Extraído de `AnaliseRiscoBo.montarVariaveisIa(...)`,
> `services/RespostaIa.java` e `services/IaServiceReal.java`. Se algo divergir, o código vence
> e este documento deve ser corrigido.

A API Java é o núcleo orquestrador: ela trata os dados climáticos e envia ao Flask apenas as
variáveis já no formato esperado. O Flask expõe dois modelos (classificação de risco + regressão
de score de alagamento) e devolve a avaliação. Baixo acoplamento: o Java não conhece os modelos.

---

## 1. Endpoint

| Item | Valor |
|---|---|
| Método | `POST` |
| URL | definida na variável de ambiente **`IA_URL`** (deve incluir o caminho completo, ex.: `https://.../prever`) |
| Header | `Content-Type: application/json` |
| Seleção de modo | variável `IA_MODO` = `REAL` (chama o Flask) \| `MOCK` (avaliador interno, default) |

> Enquanto a Flask da equipe de IA não estiver publicada, mantenha `IA_MODO=MOCK` (default). O
> `MOCK` aplica as mesmas regras de faixa e devolve o mesmo formato de resposta.

---

## 2. Contrato 1 — Variáveis enviadas (Java → Flask)

O corpo é um objeto JSON com **exatamente estas 8 chaves** (nomes e unidades fixos). Qualquer
divergência de nome faz o Flask responder `400` (`campos_faltantes`) ou prever errado.

| Chave | Unidade | Origem |
|---|---|---|
| `precipitacao_6h` | mm (acumulado em 6 h) | Open-Meteo → tratado pelo Java (janela de 6 h) |
| `umidade` | % | Open-Meteo, hora atual |
| `pressao` | hPa | Open-Meteo, hora atual |
| `vento` | km/h | Open-Meteo, hora atual |
| `temperatura` | °C | Open-Meteo, hora atual |
| `umidade_solo` | m³/m³ | Open-Meteo, hora atual |
| `mes` | 1 a 12 | calculado pelo Java (`LocalDate.now().getMonthValue()`) |
| `subpref_cod` | número | `TB_SUBPREFEITURA.cd_subpref` da região analisada |

**Exemplo de requisição:**

```json
{
  "precipitacao_6h": 42.0,
  "umidade": 95.0,
  "pressao": 1000.0,
  "vento": 70.0,
  "temperatura": 19.0,
  "umidade_solo": 0.6,
  "mes": 6,
  "subpref_cod": 112
}
```

> ⚠️ **`subpref_cod`:** os códigos `cd_subpref` (101 a 132) precisam ser **idênticos** no banco e no
> dataset de treino da IA. Qualquer inconsistência quebra a predição silenciosamente.

---

## 3. Resposta — avaliação de risco (Flask → Java)

**Sucesso — HTTP 200:**

```json
{
  "status": "sucesso",
  "risco_geral": "Alto",
  "score_alagamento": 0.85
}
```

| Campo | Tipo | Valores | Observação |
|---|---|---|---|
| `status` | string | `"sucesso"` | — |
| `risco_geral` | string | `"Baixo"` \| `"Medio"` \| `"Alto"` | classificação (Modelo 1) |
| `score_alagamento` | float | `0.0` a `1.0` | regressão (Modelo 2) |

O POJO `RespostaIa` espelha esses três campos em snake_case. O Java **normaliza** `risco_geral`
para maiúsculas (`BAIXO`/`MEDIO`/`ALTO`) antes de gravar — o `CHECK` de `TB_ALERTA` exige maiúsculas.

**Erro — HTTP 400/500:**

```json
{
  "status": "erro",
  "mensagem": "descrição do problema",
  "campos_faltantes": ["..."]
}
```

`IaServiceReal` checa o status HTTP: se for diferente de `200`, lança exceção com a `mensagem`
(e `campos_faltantes`, quando presente) em vez de tentar interpretar a resposta — assim um erro do
Flask não vira `risco_geral` nulo mais adiante.

---

## 4. O que o Java faz com a resposta (resumo do fluxo)

1. Normaliza `risco_geral` → `BAIXO`/`MEDIO`/`ALTO`.
2. Grava um **Alerta** em `TB_ALERTA` (sempre, mesmo risco BAIXO).
3. Registra a associação em `TB_ALERTA_MODELO` (com `score_alagamento`).
4. Se o risco for **MEDIO** ou **ALTO**, cria uma **Notificação** em `TB_NOTIFICACAO`.
   Se for **BAIXO**, o alerta fica no histórico mas **não** gera notificação.

---

## 5. Variáveis de ambiente relacionadas

| Variável | Valores | Default | Efeito |
|---|---|---|---|
| `IA_MODO` | `REAL` \| `MOCK` | `MOCK` | `MOCK` usa o avaliador interno; `REAL` chama o Flask |
| `IA_URL` | URL completa do endpoint Flask | (não setada) | **obrigatória quando `IA_MODO=REAL`** — sem ela, a análise falha |

---

## 6. Mudanças no contrato

Se qualquer nome/unidade de variável do Contrato 1 ou campo da resposta mudar, comunique a equipe
**imediatamente** e atualize: este documento, `AnaliseRiscoBo.montarVariaveisIa`, `RespostaIa` e o
lado da IA. O contrato é fixo justamente para a predição não falhar em silêncio.
