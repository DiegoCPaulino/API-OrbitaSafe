package br.com.fiap.orbitasafe.services;

import br.com.fiap.orbitasafe.dao.CenarioClimaticoDao;
import br.com.fiap.orbitasafe.entities.CenarioClimatico;
import br.com.fiap.orbitasafe.entities.Regiao;

import java.util.List;

// Fonte simulada: lê um cenário de TB_CENARIO_CLIMATICO — sem chamadas externas.
public class ClimaSimulado implements ServicoClima {

    @Override
    public DadosClimaticos obterDados(Regiao regiao) throws Exception {
        CenarioClimaticoDao dao = new CenarioClimaticoDao();
        List<CenarioClimatico> cenarios = dao.selecionar();

        if (cenarios == null || cenarios.isEmpty()) {
            throw new RuntimeException("Nenhum cenario climatico encontrado em TB_CENARIO_CLIMATICO.");
        }

        CenarioClimatico cenario = cenarios.get(0);

        DadosClimaticos dados = new DadosClimaticos();
        dados.setUmidadeRelativa(cenario.getUmidadeCenario());
        dados.setPressao(cenario.getPressaoCenario());
        dados.setVelocidadeVento(cenario.getVentoCenario());
        dados.setTemperatura(cenario.getTemperaturaCenario());
        dados.setUmidadeSolo(cenario.getUmidSoloCenario());
        // cenário tem valor único de precipitação; cria array de 1 elemento para o cálculo de janela
        dados.setPrecipitacaoHoraria(new double[]{cenario.getPrecipitacaoCenario()});

        return dados;
    }
}
