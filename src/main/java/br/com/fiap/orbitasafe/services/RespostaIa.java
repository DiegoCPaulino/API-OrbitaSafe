package br.com.fiap.orbitasafe.services;

// Ajustar este POJO quando a equipe de IA definir o formato real da resposta.

// Campos em snake_case para refletir a resposta JSON da API Flask (padrão das aulas).
public class RespostaIa {

    private String nivel_risco;          // "BAIXO", "MEDIO" ou "ALTO"
    private double probabilidade;        // 0.0 a 1.0
    private double precipitacao_prevista; // mm (modelo de regressão)

    public RespostaIa() {}

    public RespostaIa(String nivel_risco, double probabilidade, double precipitacao_prevista) {
        this.nivel_risco           = nivel_risco;
        this.probabilidade         = probabilidade;
        this.precipitacao_prevista = precipitacao_prevista;
    }

    public String getNivel_risco() { return nivel_risco; }
    public void setNivel_risco(String nivel_risco) { this.nivel_risco = nivel_risco; }

    public double getProbabilidade() { return probabilidade; }
    public void setProbabilidade(double probabilidade) { this.probabilidade = probabilidade; }

    public double getPrecipitacao_prevista() { return precipitacao_prevista; }
    public void setPrecipitacao_prevista(double precipitacao_prevista) { this.precipitacao_prevista = precipitacao_prevista; }

    @Override
    public String toString() {
        return "RespostaIa" +
                "\nnivel_risco='" + nivel_risco + '\'' +
                "\nprobabilidade=" + probabilidade +
                "\nprecipitacao_prevista=" + precipitacao_prevista;
    }
}
