# Deploy — OrbitaSafe API (Render via Docker)

Guia de publicação da API Java (Quarkus, modo JVM) no **Render** usando **Docker**. O Render não tem
um runtime nativo de Java na lista de linguagens, então o deploy é feito pela opção **Docker**: o
Render lê o `Dockerfile` da raiz do repositório, constrói a imagem e a executa. O banco Oracle é
fornecido pela FIAP; o front-end vai para a Vercel (projeto separado).

---

## 1. Visão geral

- **Plataforma:** Render (Web Service), runtime **Docker** (não há opção nativa de Java no Render).
- **Artefato:** imagem Docker construída a partir do `Dockerfile` na raiz do repositório.
- **Build em 2 estágios (multi-stage):**
  1. Imagem `maven:3.9-eclipse-temurin-21` compila e empacota o JAR Quarkus (`mvn package -DskipTests`).
  2. Imagem final `eclipse-temurin:21-jre` recebe só o artefato e roda — sem Maven, sem código-fonte.
- **Porta:** a API lê a variável `PORT` (o Render injeta automaticamente em runtime). Já configurado em
  `application.properties` via `quarkus.http.port=${PORT:8080}`; o Quarkus já faz bind em `0.0.0.0`.
- **Banco:** Oracle da FIAP (precisa estar acessível remotamente a partir do Render).
- **CORS:** a origem liberada vem de `FRONTEND_URL` — defina a URL da Vercel em produção.

> **Por que Docker e não "Java"?** O Render lista linguagens como Node, Python, Go, Ruby, etc., mas
> não Java. A opção **Docker** resolve isso e ainda fixa a versão exata do Java (21) dentro da imagem,
> eliminando a dependência da versão de Java do builder do Render.

---

## 2. Pré-requisitos

- Repositório no GitHub com o código da API **e o `Dockerfile` na raiz** (já versionado).
- Conta no Render conectada ao GitHub.
- Credenciais do Oracle da FIAP (URL JDBC, usuário, senha) válidas e com acesso remoto.
- (Opcional) URL do endpoint da Flask de IA, se for rodar com `IA_MODO=REAL`.
- (Opcional, só para teste local) Docker instalado.

---

## 3. O `Dockerfile` (como o Render constrói)

O `Dockerfile` na raiz do projeto descreve todo o build e a execução. **No deploy via Docker o Render
ignora "Build Command" e "Start Command"** — quem manda é o `Dockerfile`. Não há nada a digitar nesses
campos.

Resumo do que o `Dockerfile` faz:

```dockerfile
# Estágio 1 — builda o JAR (Maven + JDK 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests        # gera target/quarkus-app/

# Estágio 2 — só JRE 21 + artefato (imagem final enxuta)
FROM eclipse-temurin:21-jre
WORKDIR /deployments
COPY --from=build /app/target/quarkus-app/lib/     ./lib/
COPY --from=build /app/target/quarkus-app/*.jar    ./
COPY --from=build /app/target/quarkus-app/app/     ./app/
COPY --from=build /app/target/quarkus-app/quarkus/ ./quarkus/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
```

O arquivo `.dockerignore` mantém o contexto de build enxuto e — importante — **impede que o `.env`
(credenciais) entre na imagem**.

### 3.1. Testar a imagem localmente (opcional, mas recomendado antes do deploy)

```bash
# Build da imagem
docker build -t orbitasafe-api .

# Rodar local (passando as variáveis e mapeando a porta)
docker run --rm -p 8080:8080 \
  -e ORACLE_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl" \
  -e ORACLE_USER="seu_rm" \
  -e ORACLE_PASSWORD="sua_senha" \
  orbitasafe-api
```

> No PowerShell (Windows), use crase (`` ` ``) no lugar da barra invertida (`\`) para quebrar linha,
> ou escreva tudo numa linha só. Teste: `GET http://localhost:8080/subprefeituras`.

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
> **`JAVA_VERSION` não é mais necessária** — a versão do Java está fixada dentro do `Dockerfile`.
> Nunca comite credenciais: o `.env` está no `.gitignore` e no `.dockerignore`; em produção tudo vem
> do painel do Render.

---

## 5. Passo a passo no Render

1. **New → Web Service** e conecte o repositório do GitHub.
2. **Language / Runtime:** selecione **Docker**. O Render detecta o `Dockerfile` na raiz
   automaticamente (campo "Dockerfile Path" = `./Dockerfile`).
3. **Build Command / Start Command:** deixe **em branco** — o `Dockerfile` cuida de tudo.
4. **Instance Type:** `Free` é suficiente para a demonstração.
5. **Environment Variables:** adicione as variáveis da Seção 4.
6. **Create Web Service** e acompanhe o log de build/deploy (o primeiro build é mais lento porque
   baixa as dependências Maven; builds seguintes reaproveitam camadas).
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
| Build Docker falha ao baixar dependências | rede/Maven Central instável no build | acione **Manual Deploy → Clear build cache & deploy** e tente de novo |
| Build estoura memória (`OutOfMemoryError` no estágio Maven) | build pesado no plano free | use **Clear build cache & deploy**; se persistir, considere instância paga só para o build |
| `ORA-12541` / timeout na conexão | Oracle FIAP inacessível do Render | verifique `ORACLE_URL` e se o banco aceita conexão remota |
| `Variaveis de ambiente ORACLE_* nao configuradas` | env vars ausentes | preencha `ORACLE_URL/USER/PASSWORD` no painel |
| Front em produção: erro de CORS | `FRONTEND_URL` errada/ausente | defina `FRONTEND_URL` com a URL exata da Vercel e redeploy |
| `500` em `POST /regioes/{id}/analisar` com `IA_MODO=REAL` | Flask fora do ar ou `IA_URL` ausente | use `IA_MODO=MOCK` ou corrija `IA_URL` |
| Render não acha o `Dockerfile` | runtime errado ou path incorreto | garanta runtime **Docker** e "Dockerfile Path" = `./Dockerfile` |

---

*OrbitaSafe · Global Solution 2026/1 · FIAP · 1TDS Agosto*
