package br.com.fiap.orbitasafe.entities;

// TB_ALERTA_MODELO — tabela associativa N:N (PK composta pelas duas FKs, sem PK propria)
public class AlertaModelo {

    private int fkAlertaIdAlerta;
    private int fkModeloIdModelo;
    private double scoreModelo;

    public AlertaModelo() {}

    public AlertaModelo(int fkAlertaIdAlerta, int fkModeloIdModelo, double scoreModelo) {
        this.fkAlertaIdAlerta = fkAlertaIdAlerta;
        this.fkModeloIdModelo = fkModeloIdModelo;
        this.scoreModelo      = scoreModelo;
    }

    public int getFkAlertaIdAlerta() { return fkAlertaIdAlerta; }
    public void setFkAlertaIdAlerta(int fkAlertaIdAlerta) { this.fkAlertaIdAlerta = fkAlertaIdAlerta; }

    public int getFkModeloIdModelo() { return fkModeloIdModelo; }
    public void setFkModeloIdModelo(int fkModeloIdModelo) { this.fkModeloIdModelo = fkModeloIdModelo; }

    public double getScoreModelo() { return scoreModelo; }
    public void setScoreModelo(double scoreModelo) { this.scoreModelo = scoreModelo; }

    @Override
    public String toString() {
        return "AlertaModelo" +
                "\nfkAlertaIdAlerta=" + fkAlertaIdAlerta +
                "\nfkModeloIdModelo=" + fkModeloIdModelo +
                "\nscoreModelo=" + scoreModelo;
    }
}
