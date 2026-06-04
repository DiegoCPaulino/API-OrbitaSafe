package br.com.fiap.orbitasafe.services;

import java.util.Map;

// Mock da IA — retorna resultado baseado em regras simples sobre as variáveis recebidas.
// NÃO faz chamada HTTP. Usado enquanto a API Flask da equipe não está pronta.
public class IaServiceMock implements IaService {

    @Override
    public RespostaIa avaliarRisco(Map<String, Object> variaveis) throws Exception {
        double precipitacao = ((Number) variaveis.get("precipitacao_6h")).doubleValue();
        double vento        = ((Number) variaveis.get("vento")).doubleValue();

        RespostaIa resposta = new RespostaIa();

        // Mesma grafia capitalizada da Flask real ("Baixo"/"Medio"/"Alto")
        if (precipitacao > 30 || vento > 50) {
            resposta.setRisco_geral("Alto");
            resposta.setScore_alagamento(0.85);
        } else if (precipitacao > 10 || vento > 25) {
            resposta.setRisco_geral("Medio");
            resposta.setScore_alagamento(0.60);
        } else {
            resposta.setRisco_geral("Baixo");
            resposta.setScore_alagamento(0.20);
        }

        resposta.setStatus("sucesso");
        return resposta;
    }
}
