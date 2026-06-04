package br.com.fiap.orbitasafe.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.Map;

// Implementação real: envia as variáveis climáticas à API Flask da equipe de IA.
// Padrão ViaCepService das aulas, com HttpPost.
// URL configurada via variável de ambiente IA_URL.
public class IaServiceReal implements IaService {

    @Override
    public RespostaIa avaliarRisco(Map<String, Object> variaveis) throws Exception {
        String iaUrl = System.getenv("IA_URL");
        if (iaUrl == null || iaUrl.isBlank()) {
            throw new RuntimeException("Variavel de ambiente IA_URL nao configurada.");
        }

        Gson gson = new Gson();
        // Contrato 1 — Map serializado direto; ajustar chaves em AnaliseRiscoBo quando o formato fechar.
        String json = gson.toJson(variaveis);

        HttpPost request = new HttpPost(iaUrl);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(json, "UTF-8"));

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling().build();
        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);

        // A Flask devolve 400/500 com {"status":"erro","mensagem":...}. Sem checar o
        // status, o parse traria risco_geral nulo e estouraria NPE mais adiante.
        if (statusCode != 200) {
            throw new RuntimeException("Erro na API de IA (HTTP " + statusCode + "): "
                    + extrairMensagemErro(gson, result));
        }

        return gson.fromJson(result, RespostaIa.class);
    }

    // Extrai a "mensagem" do corpo de erro da Flask; se não for JSON, devolve o corpo cru.
    private String extrairMensagemErro(Gson gson, String corpo) {
        try {
            JsonObject erro = gson.fromJson(corpo, JsonObject.class);
            if (erro != null && erro.has("mensagem")) {
                String mensagem = erro.get("mensagem").getAsString();
                if (erro.has("campos_faltantes")) {
                    mensagem += " Campos faltantes: " + erro.get("campos_faltantes");
                }
                return mensagem;
            }
        } catch (Exception e) {
            // corpo não-JSON: cai para o retorno do corpo cru
        }
        return corpo;
    }
}
