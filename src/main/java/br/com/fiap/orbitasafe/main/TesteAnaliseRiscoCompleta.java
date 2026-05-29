package br.com.fiap.orbitasafe.main;

import br.com.fiap.orbitasafe.bo.AnaliseRiscoBo;
import br.com.fiap.orbitasafe.dao.NotificacaoDao;
import br.com.fiap.orbitasafe.entities.Alerta;
import br.com.fiap.orbitasafe.entities.Notificacao;

import java.util.List;

public class TesteAnaliseRiscoCompleta {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando teste de analise de risco ===");
        System.out.println("Configurar antes de rodar:");
        System.out.println("  FONTE_CLIMA=SIMULADO  (ou omitir — default e SIMULADO)");
        System.out.println("  IA_MODO=MOCK          (ou omitir — default e MOCK)");
        System.out.println("  ORACLE_URL, ORACLE_USER, ORACLE_PASSWORD");
        System.out.println("---");

        AnaliseRiscoBo bo = new AnaliseRiscoBo();

        // Regiao id=1 pertence ao usuario id=1 segundo o DML da Fase 2
        Alerta alerta = bo.analisar(1);

        System.out.println("Alerta gerado:");
        System.out.println(alerta);
        System.out.println("---");

        // Confirma se notificação foi criada (só para MEDIO ou ALTO)
        NotificacaoDao notifDao = new NotificacaoDao();
        List<Notificacao> notifs = notifDao.selecionarPorUsuario(1);
        System.out.println("Total de notificacoes do usuario 1: " + notifs.size());
        if (!notifs.isEmpty()) {
            System.out.println("Ultima notificacao: " + notifs.get(0));
        } else {
            System.out.println("Nenhuma notificacao (risco BAIXO nao gera notificacao — comportamento esperado).");
        }

        System.out.println("=== Teste concluido ===");
    }
}
