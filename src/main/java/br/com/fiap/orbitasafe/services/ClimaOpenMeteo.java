package br.com.fiap.orbitasafe.services;

import br.com.fiap.orbitasafe.dao.SubprefeituraDao;
import br.com.fiap.orbitasafe.entities.Regiao;
import br.com.fiap.orbitasafe.entities.Subprefeitura;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.time.LocalDateTime;

// Fonte real: chama a API Open-Meteo no padrão ViaCepService das aulas.
public class ClimaOpenMeteo implements ServicoClima {

    @Override
    public DadosClimaticos obterDados(Regiao regiao) throws Exception {
        double lat = -23.5505;
        double lon = -46.6333;

        // Tenta buscar lat/lon da subprefeitura. Fallback: coordenadas centrais de SP.
        try {
            SubprefeituraDao subprefDao = new SubprefeituraDao();
            Subprefeitura subpref = subprefDao.buscarPorId(regiao.getFkSubprefeituraIdSubpref());
            if (subpref != null && subpref.getLatitudeSubpref() != 0.0) {
                lat = subpref.getLatitudeSubpref();
                lon = subpref.getLongitudeSubpref();
            }
        } catch (Exception e) {
            // fallback silencioso — continua com coordenadas fixas de SP
        }

        String url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=" + lat +
            "&longitude=" + lon +
            "&hourly=temperature_2m,relative_humidity_2m,precipitation,pressure_msl,wind_speed_10m,soil_moisture_0_to_1cm" +
            "&timezone=America%2FSao_Paulo" +
            "&forecast_days=1";

        HttpGet request = new HttpGet(url);
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling().build();
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);

        Gson gson = new Gson();
        RespostaOpenMeteo resp = gson.fromJson(result, RespostaOpenMeteo.class);

        return mapearDados(resp);
    }

    private DadosClimaticos mapearDados(RespostaOpenMeteo resp) {
        if (resp == null || resp.hourly == null) {
            throw new RuntimeException("Resposta da Open-Meteo esta vazia ou mal-formada.");
        }

        // Determina o índice correspondente à hora atual no array hourly
        int horaAtual = LocalDateTime.now().getHour();
        int indice = 0;
        if (resp.hourly.time != null) {
            for (int i = 0; i < resp.hourly.time.length; i++) {
                if (resp.hourly.time[i].contains("T" + String.format("%02d", horaAtual))) {
                    indice = i;
                    break;
                }
            }
        }

        if (resp.hourly.temperature_2m == null || resp.hourly.relative_humidity_2m == null
                || resp.hourly.pressure_msl == null || resp.hourly.wind_speed_10m == null
                || resp.hourly.soil_moisture_0_to_1cm == null || resp.hourly.precipitation == null
                || indice >= resp.hourly.temperature_2m.length) {
            throw new RuntimeException("Resposta da Open-Meteo incompleta (arrays horarios ausentes).");
        }

        DadosClimaticos dados = new DadosClimaticos();
        dados.setTemperatura(resp.hourly.temperature_2m[indice]);
        dados.setUmidadeRelativa(resp.hourly.relative_humidity_2m[indice]);
        dados.setPressao(resp.hourly.pressure_msl[indice]);
        dados.setVelocidadeVento(resp.hourly.wind_speed_10m[indice]);
        dados.setUmidadeSolo(resp.hourly.soil_moisture_0_to_1cm[indice]);
        dados.setPrecipitacaoHoraria(resp.hourly.precipitation); // array completo para janela
        return dados;
    }
}
