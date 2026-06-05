package br.com.fiap.orbitasafe.bo;

import br.com.fiap.orbitasafe.dao.*;
import br.com.fiap.orbitasafe.entities.*;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import br.com.fiap.orbitasafe.services.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnaliseRiscoBo {

    // Janela de horas para somar precipitação acumulada — a Flask espera "precipitacao_6h" (acúmulo de 6h).
    private static final int JANELA_PRECIPITACAO_HORAS = 6;

    public Alerta analisar(int idRegiao) throws Exception {
        RegiaoDao regiaoDao = new RegiaoDao();
        Regiao regiao = regiaoDao.buscarPorId(idRegiao);
        if (regiao == null) {
            throw new RegistroNaoEncontradoException("Regiao nao encontrada: id=" + idRegiao);
        }

        SubprefeituraDao subprefDao = new SubprefeituraDao();
        Subprefeitura subpref = subprefDao.buscarPorId(regiao.getFkSubprefeituraIdSubpref());

        ServicoClima servico = ServicoClimaFactory.criar();
        DadosClimaticos dados;
        try {
            dados = servico.obterDados(regiao);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[AnaliseRiscoBo] Falha na fonte principal — usando ClimaSimulado.");
            dados = new ClimaSimulado().obterDados(regiao);
        }

        // Gera IDs únicos para esta análise (banco sem SEQUENCE)
        long baseTimestamp = System.currentTimeMillis();
        int idLeitura = (int)(baseTimestamp % 9_000_000) + 1_000_000;
        int idAlerta  = (int)((baseTimestamp + 1) % 9_000_000) + 1_000_000;
        int idNotif   = (int)((baseTimestamp + 2) % 9_000_000) + 1_000_000;

        LeituraClimatica leitura = new LeituraClimatica();
        leitura.setIdLeitura(idLeitura);
        // leitura registra a precip. da hora atual; a IA recebe o acumulo de 6h (montarVariaveisIa)
        double[] precHoraria = dados.getPrecipitacaoHoraria();
        double precAtual = (precHoraria != null && precHoraria.length > 0) ? precHoraria[0] : 0.0;
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

        int cdSubpref = (subpref != null) ? subpref.getCdSubpref() : 0;
        Map<String, Object> variaveis = montarVariaveisIa(dados, cdSubpref);

        RespostaIa respostaIa;
        try {
            respostaIa = IaServiceFactory.criar().avaliarRisco(variaveis);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao chamar o servico de IA: " + e.getMessage(), e);
        }

        // Criar e gravar Alerta (sempre, mesmo risco BAIXO)
        // Flask devolve "Baixo"/"Medio"/"Alto"; normaliza para o padrao do banco (BAIXO/MEDIO/ALTO)
        if (respostaIa == null || respostaIa.getRisco_geral() == null) {
            throw new RuntimeException("Resposta da IA invalida: risco_geral ausente.");
        }
        String nivel = respostaIa.getRisco_geral().toUpperCase();
        Alerta alerta = new Alerta();
        alerta.setIdAlerta(idAlerta);
        alerta.setNivelAlerta(nivel);
        alerta.setTpEvento("Analise Automatica");
        alerta.setDsAlerta("Risco " + nivel +
                " — score de alagamento: " + respostaIa.getScore_alagamento());
        alerta.setDtAlerta(LocalDate.now());
        alerta.setFkRegiaoIdReg(idRegiao);
        alerta.setFkLeituraIdLeitura(idLeitura);
        new AlertaDao().inserir(alerta);

        List<ModeloIa> modelos = new ModeloIaDao().selecionar();
        AlertaModeloDao alertaModeloDao = new AlertaModeloDao();
        for (ModeloIa modelo : modelos) {
            AlertaModelo am = new AlertaModelo();
            am.setFkAlertaIdAlerta(idAlerta);
            am.setFkModeloIdModelo(modelo.getIdModelo());
            am.setScoreModelo(respostaIa.getScore_alagamento());
            alertaModeloDao.inserir(am);
        }

        // Criar notificação APENAS para risco MEDIO ou ALTO
        if ("MEDIO".equals(nivel) || "ALTO".equals(nivel)) {
            Notificacao notif = new Notificacao();
            notif.setIdNotif(idNotif);
            notif.setDsNotif("Alerta " + nivel + " na regiao: " + regiao.getNmReg());
            notif.setDtNotif(LocalDate.now());
            notif.setEstadoNotif("NAO_LIDA");
            notif.setFkUsuarioIdUsu(regiao.getFkUsuarioIdUsu());
            notif.setFkAlertaIdAlerta(idAlerta);
            new NotificacaoDao().inserir(notif);
        }

        return alerta;
    }

    private Map<String, Object> montarVariaveisIa(DadosClimaticos dados, int cdSubpref) {
        double[] precHoraria = dados.getPrecipitacaoHoraria();
        double precipAcumulada = 0.0;
        int limite = (precHoraria != null) ? Math.min(JANELA_PRECIPITACAO_HORAS, precHoraria.length) : 0;
        for (int i = 0; i < limite; i++) {
            precipAcumulada += precHoraria[i];
        }

        Map<String, Object> variaveis = new HashMap<String, Object>();

        // Nomes exatos esperados pelo endpoint POST /predict da Flask
        variaveis.put("precipitacao_6h", precipAcumulada);
        variaveis.put("umidade",         dados.getUmidadeRelativa());
        variaveis.put("pressao",         dados.getPressao());
        variaveis.put("vento",           dados.getVelocidadeVento());
        variaveis.put("temperatura",     dados.getTemperatura());
        variaveis.put("umidade_solo",    dados.getUmidadeSolo());
        variaveis.put("mes",             LocalDate.now().getMonthValue());
        variaveis.put("subpref_cod",     cdSubpref);

        return variaveis;
    }
}
