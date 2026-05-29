package br.com.fiap.orbitasafe.bo;

import br.com.fiap.orbitasafe.dao.*;
import br.com.fiap.orbitasafe.entities.*;
import br.com.fiap.orbitasafe.services.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnaliseRiscoBo {

    // Janela de horas usada para somar precipitação acumulada (Contrato 1, variável "precipitacao").
    private static final int JANELA_PRECIPITACAO_HORAS = 24;

    public Alerta analisar(int idRegiao) throws Exception {
        // 1. Buscar região
        RegiaoDao regiaoDao = new RegiaoDao();
        Regiao regiao = regiaoDao.buscarPorId(idRegiao);
        if (regiao == null) {
            throw new RuntimeException("Regiao nao encontrada: id=" + idRegiao);
        }

        // 2. Buscar subprefeitura (para o código estável usado pela IA)
        SubprefeituraDao subprefDao = new SubprefeituraDao();
        Subprefeitura subpref = subprefDao.buscarPorId(regiao.getFkSubprefeituraIdSubpref());

        // 3. Obter dados climáticos; se falhar, faz fallback automático para fonte simulada
        ServicoClima servico = ServicoClimaFactory.criar();
        DadosClimaticos dados;
        try {
            dados = servico.obterDados(regiao);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[AnaliseRiscoBo] Falha na fonte principal — usando ClimaSimulado.");
            dados = new ClimaSimulado().obterDados(regiao);
        }

        // 4. Gerar IDs únicos para esta análise (banco sem SEQUENCE)
        long base = System.currentTimeMillis();
        int idLeitura = (int)(base % 9_000_000) + 1_000_000;
        int idAlerta  = (int)((base + 1) % 9_000_000) + 1_000_000;
        int idNotif   = (int)((base + 2) % 9_000_000) + 1_000_000;

        // 5. Gravar leitura climática
        LeituraClimatica leitura = new LeituraClimatica();
        leitura.setIdLeitura(idLeitura);
        double precAtual = dados.getPrecipitacaoHoraria().length > 0 ? dados.getPrecipitacaoHoraria()[0] : 0.0;
        leitura.setPrecipitacaoLeitura(precAtual);
        leitura.setUmidadeLeitura(dados.getUmidadeRelativa());
        leitura.setPressaoLeitura(dados.getPressao());
        leitura.setVentoLeitura(dados.getVelocidadeVento());
        leitura.setTemperaturaLeitura(dados.getTemperatura());
        leitura.setUmidSoloLeitura(dados.getUmidadeSolo());
        leitura.setDtLeitura(LocalDate.now());
        leitura.setDiaPrevisao(0);
        leitura.setFkRegiaoIdReg(idRegiao);
        new LeituraClimaticaDao().inserir(leitura);

        // 6. Montar as 8 variáveis do Contrato 1 e chamar a IA
        int cdSubpref = (subpref != null) ? subpref.getCdSubpref() : 0;
        Map<String, Object> variaveis = montarVariaveisIa(dados, cdSubpref);

        RespostaIa respostaIa;
        try {
            respostaIa = IaServiceFactory.criar().avaliarRisco(variaveis);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao chamar o servico de IA: " + e.getMessage(), e);
        }

        // 7. Criar e gravar Alerta (sempre, mesmo risco BAIXO)
        Alerta alerta = new Alerta();
        alerta.setIdAlerta(idAlerta);
        alerta.setNivelAlerta(respostaIa.getNivel_risco());
        alerta.setTpEvento("Analise Automatica");
        alerta.setDsAlerta("Risco " + respostaIa.getNivel_risco() +
                " — probabilidade: " + respostaIa.getProbabilidade() +
                " — precipitacao prevista: " + respostaIa.getPrecipitacao_prevista() + "mm");
        alerta.setDtAlerta(LocalDate.now());
        alerta.setFkRegiaoIdReg(idRegiao);
        alerta.setFkLeituraIdLeitura(idLeitura);
        new AlertaDao().inserir(alerta);

        // 8. Associar todos os modelos de IA ativos ao alerta
        List<ModeloIa> modelos = new ModeloIaDao().selecionar();
        AlertaModeloDao alertaModeloDao = new AlertaModeloDao();
        for (ModeloIa modelo : modelos) {
            AlertaModelo am = new AlertaModelo();
            am.setFkAlertaIdAlerta(idAlerta);
            am.setFkModeloIdModelo(modelo.getIdModelo());
            am.setScoreModelo(respostaIa.getProbabilidade());
            alertaModeloDao.inserir(am);
        }

        // 9. Criar notificação APENAS para risco MEDIO ou ALTO
        if ("MEDIO".equals(respostaIa.getNivel_risco()) || "ALTO".equals(respostaIa.getNivel_risco())) {
            Notificacao notif = new Notificacao();
            notif.setIdNotif(idNotif);
            notif.setDsNotif("Alerta " + respostaIa.getNivel_risco() + " na regiao: " + regiao.getNmReg());
            notif.setDtNotif(LocalDate.now());
            notif.setEstadoNotif("NAO_LIDA");
            notif.setFkUsuarioIdUsu(regiao.getFkUsuarioIdUsu());
            notif.setFkAlertaIdAlerta(idAlerta);
            new NotificacaoDao().inserir(notif);
        }

        return alerta;
    }

    private Map<String, Object> montarVariaveisIa(DadosClimaticos dados, int cdSubpref) {
        // Soma de precipitação na janela de horas configurada
        double[] precHoraria = dados.getPrecipitacaoHoraria();
        double precipAcumulada = 0.0;
        int limite = Math.min(JANELA_PRECIPITACAO_HORAS, precHoraria.length);
        for (int i = 0; i < limite; i++) {
            precipAcumulada += precHoraria[i];
        }

        Map<String, Object> variaveis = new HashMap<String, Object>();

        // =====================================================================
        // CONTRATO 1 — INVIOLÁVEL
        // As 8 chaves abaixo refletem a Seção 7.2 do Documento Base.
        // QUANDO A EQUIPE DE IA FECHAR OS NOMES REAIS DA API FLASK, AJUSTAR
        // AS CHAVES DESTE MAP — É O ÚNICO PONTO DE MUDANÇA NECESSÁRIO.
        // =====================================================================
        variaveis.put("precipitacao",           precipAcumulada);
        variaveis.put("umidade_relativa",        dados.getUmidadeRelativa());
        variaveis.put("pressao_atmosferica",     dados.getPressao());
        variaveis.put("velocidade_vento",        dados.getVelocidadeVento());
        variaveis.put("temperatura",             dados.getTemperatura());
        variaveis.put("umidade_solo",            dados.getUmidadeSolo());
        variaveis.put("mes",                     LocalDate.now().getMonthValue());
        variaveis.put("codigo_subprefeitura",    cdSubpref);

        return variaveis;
    }
}
