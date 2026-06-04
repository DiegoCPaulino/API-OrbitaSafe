package br.com.fiap.orbitasafe.services;

// Espelha a resposta JSON do endpoint POST /predict da API Flask.
// Campos em snake_case para refletir o JSON da Flask (padrão das aulas).
public class RespostaIa {

    private String risco_geral;        // "Baixo", "Medio" ou "Alto"
    private double score_alagamento;   // 0.0 a 1.0
    private String status;             // "sucesso" ou "erro"

    public RespostaIa() {}

    public RespostaIa(String risco_geral, double score_alagamento, String status) {
        this.risco_geral      = risco_geral;
        this.score_alagamento = score_alagamento;
        this.status           = status;
    }

    public String getRisco_geral() { return risco_geral; }
    public void setRisco_geral(String risco_geral) { this.risco_geral = risco_geral; }

    public double getScore_alagamento() { return score_alagamento; }
    public void setScore_alagamento(double score_alagamento) { this.score_alagamento = score_alagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "RespostaIa" +
                "\nrisco_geral='" + risco_geral + '\'' +
                "\nscore_alagamento=" + score_alagamento +
                "\nstatus='" + status + '\'';
    }
}
