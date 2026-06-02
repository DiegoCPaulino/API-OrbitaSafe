package br.com.fiap.orbitasafe.services;

import br.com.fiap.orbitasafe.dao.CenarioClimaticoDao;
import br.com.fiap.orbitasafe.entities.CenarioClimatico;
import br.com.fiap.orbitasafe.entities.Regiao;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Fonte simulada: lê um cenário de TB_CENARIO_CLIMATICO — sem chamadas externas.
public class ClimaSimulado implements ServicoClima {

    // Contador para round-robin — AtomicInteger evita race condition em
    // ambiente multi-thread (Quarkus serve requisições concorrentes).
    private static final AtomicInteger contador = new AtomicInteger(0);

    @Override
    public DadosClimaticos obterDados(Regiao regiao) throws Exception {
        CenarioClimaticoDao dao = new CenarioClimaticoDao();
        List<CenarioClimatico> cenarios = dao.selecionar();

        if (cenarios == null || cenarios.isEmpty()) {
            throw new RuntimeException("Nenhum cenario climatico encontrado em TB_CENARIO_CLIMATICO.");
        }

        CenarioClimatico cenario;
        String cenarioFixo = System.getenv("CENARIO_FIXO");

        if (cenarioFixo != null && !cenarioFixo.isEmpty()) {
            int idFixo = Integer.parseInt(cenarioFixo);
            cenario = null;
            for (CenarioClimatico c : cenarios) {
                if (c.getIdCenario() == idFixo) {
                    cenario = c;
                    break;
                }
            }
            if (cenario == null) {
                throw new RuntimeException("CENARIO_FIXO=" + cenarioFixo +
                    " não encontrado entre os cenários do banco");
            }
        } else {
            int indice = contador.getAndIncrement() % cenarios.size();
            cenario = cenarios.get(indice);
        }

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
