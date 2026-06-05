# Deploy — OrbitaSafe API (Render)

Guia de publicação da API Java (Quarkus, modo JVM) no **Render**. O banco Oracle é fornecido pela
FIAP; o front-end vai para a Vercel (projeto separado).

---

## 1. Visão geral

- **Plataforma:** Render (Web Service, plano gratuito suporta Quarkus em modo JVM).
- **Artefato:** JAR Quarkus (`target/quarkus-app/quarkus-run.jar`).
- **Porta:** a API lê a variável `PORT` (o Render injeta automaticamente). Já configurado em
  `application.properties` via `quarkus.http.port=${PORT:8080}`.
- **Banco:** Oracle da FIAP (precisa estar acessível remotamente a partir do Render).
- **CORS:** a origem liberada vem de `FRONTEND_URL` — defina a URL da Vercel em produção.

---

## 2. Pré-requisitos

- Repositório no GitHub com o código da API.
- Conta no Render conectada ao GitHub.
- Credenciais do Oracle da FIAP (URL JDBC, usuário, senha) válidas e com acesso remoto.
- (Opcional) URL do endpoint da Flask de IA, se for rodar com `IA_MODO=REAL`.

---

## 3. Build e comando de start

A API roda em **modo JVM** do Quarkus.

**Build Command:**

```bash
./mvnw package -DskipTests
```

Isso gera `target/quarkus-app/` (com `quarkus-run.jar` e a pasta `lib/`).

**Start Command:**

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

> Não use `java -jar target/*.jar` apontando para o JAR "fininho" da raiz de `target/` — o artefato
> executável do Quarkus é `target/quarkus-app/quarkus-run.jar`.

---

## 4. Variáveis de ambiente no Render

Configure em **Environment** do Web Service:

| Variável | Obrigatória | Valor |
|---|---|---|
| `ORACLE_URL` | sim | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl` |
| `ORACLE_USER` | sim | seu RM |
| `ORACLE_PASSWORD` | sim | sua senha do Oracle |
| `FRONTEND_URL` | recomendada | URL do front na Vercel (ex.: `https://orbitasafe.vercel.app`) |
| `IA_MODO` | não | `MOCK` (default) ou `REAL` |
| `IA_URL` | só se `REAL` | URL completa do endpoint de predição da Flask |
| `FONTE_CLIMA` | não | `SIMULADO` (default) ou `OPEN_METEO` |
| `CENARIO_FIXO` | não | id de cenário (1–6) para travar a demo |

> **`PORT` não precisa ser definida** — o Render injeta automaticamente e a API a respeita.
> Nunca comite credenciais: o `.env` está no `.gitignore`; em produção tudo vem do painel do Render.

---

## 5. Passo a passo no Render

1. **New → Web Service** e conecte o repositório do GitHub.
2. **Environment:** selecione um ambiente com Java/Maven (Docker não é necessário). Garanta Java 21
   (se preciso, defina `JAVA_VERSION=21` nas env vars do Render).
3. **Build Command:** `./mvnw package -DskipTests`.
4. **Start Command:** `java -jar target/quarkus-app/quarkus-run.jar`.
5. **Environment Variables:** adicione as variáveis da Seção 4.
6. **Create Web Service** e acompanhe o log de build/deploy.
7. Anote a URL pública gerada (ex.: `https://api-orbitasafe.onrender.com`).

---

## 6. Pós-deploy

- **Teste rápido:** `GET https://<sua-url>/subprefeituras` deve retornar as 32 subprefeituras.
- **Teste do fluxo:** cadastre um usuário (`POST /auth/cadastro`), uma região (`POST /regioes`) e
  consulte `GET /regioes/{id}/alertas`.
- **CORS:** confirme que `FRONTEND_URL` aponta para a URL real da Vercel — senão o navegador bloqueia
  o front em produção.
- **Atualize as referências de URL de produção** (placeholders) em `README.md` e em
  `docs/contrato-api.md` (Seções 1 e 10).

---

## 7. Troubleshooting

| Sintoma | Causa provável | Ação |
|---|---|---|
| App sobe mas Render marca como "unhealthy" | porta fixa | confirme `quarkus.http.port=${PORT:8080}` e **não** defina `PORT` manualmente |
| `ORA-12541` / timeout na conexão | Oracle FIAP inacessível do Render | verifique `ORACLE_URL` e se o banco aceita conexão remota |
| `Variaveis de ambiente ORACLE_* nao configuradas` | env vars ausentes | preencha `ORACLE_URL/USER/PASSWORD` no painel |
| Front em produção: erro de CORS | `FRONTEND_URL` errada/ausente | defina `FRONTEND_URL` com a URL exata da Vercel e redeploy |
| `500` em `POST /regioes/{id}/analisar` com `IA_MODO=REAL` | Flask fora do ar ou `IA_URL` ausente | use `IA_MODO=MOCK` ou corrija `IA_URL` |
| Build falha por versão de Java | Render usando Java < 21 | defina `JAVA_VERSION=21` |

---

*OrbitaSafe · Global Solution 2026/1 · FIAP · 1TDS Agosto*
