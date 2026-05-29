package br.com.fiap.orbitasafe.services;

import com.google.gson.Gson;
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
        // =====================================================================
        // CONTRATO 1 — o Map é serializado diretamente. Quando a equipe de IA
        // fechar o formato do endpoint, ajustar as chaves em AnaliseRiscoBo.
        // =====================================================================
        String json = gson.toJson(variaveis);

        HttpPost request = new HttpPost(iaUrl);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(json, "UTF-8"));

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling().build();
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);

        return gson.fromJson(result, RespostaIa.class);
    }
}
