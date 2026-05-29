package br.com.fiap.orbitasafe.services;

import java.util.Map;

// Mock da IA — retorna resultado baseado em regras simples sobre as variáveis recebidas.
// NÃO faz chamada HTTP. Usado enquanto a API Flask da equipe não está pronta.
public class IaServiceMock implements IaService {

    @Override
    public RespostaIa avaliarRisco(Map<String, Object> variaveis) throws Exception {
        double precipitacao   = ((Number) variaveis.get("precipitacao")).doubleValue();
        double velocidadeVento = ((Number) variaveis.get("velocidade_vento")).doubleValue();

        RespostaIa resposta = new RespostaIa();

        if (precipitacao > 30 || velocidadeVento > 50) {
            resposta.setNivel_risco("ALTO");
            resposta.setProbabilidade(0.85);
        } else if (precipitacao > 10 || velocidadeVento > 25) {
            resposta.setNivel_risco("MEDIO");
            resposta.setProbabilidade(0.60);
        } else {
            resposta.setNivel_risco("BAIXO");
            resposta.setProbabilidade(0.20);
        }

        resposta.setPrecipitacao_prevista(precipitacao * 1.2);
        return resposta;
    }
}
