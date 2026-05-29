package br.com.fiap.orbitasafe.services;

// Seleciona a implementação da IA via variável de ambiente IA_MODO.
// Valores: REAL | MOCK (default: MOCK enquanto a Flask da equipe não está pronta).
public class IaServiceFactory {

    public static IaService criar() {
        String modo = System.getenv("IA_MODO");
        if ("REAL".equalsIgnoreCase(modo)) {
            return new IaServiceReal();
        }
        return new IaServiceMock();
    }
}
