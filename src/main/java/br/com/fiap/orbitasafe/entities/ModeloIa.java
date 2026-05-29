package br.com.fiap.orbitasafe.entities;

public class ModeloIa {

    private int idModelo;
    private String nmModelo;
    private String tpModelo;
    private String versaoModelo;

    public ModeloIa() {}

    public ModeloIa(int idModelo, String nmModelo, String tpModelo, String versaoModelo) {
        this.idModelo     = idModelo;
        this.nmModelo     = nmModelo;
        this.tpModelo     = tpModelo;
        this.versaoModelo = versaoModelo;
    }

    public int getIdModelo() { return idModelo; }
    public void setIdModelo(int idModelo) { this.idModelo = idModelo; }

    public String getNmModelo() { return nmModelo; }
    public void setNmModelo(String nmModelo) { this.nmModelo = nmModelo; }

    public String getTpModelo() { return tpModelo; }
    public void setTpModelo(String tpModelo) { this.tpModelo = tpModelo; }

    public String getVersaoModelo() { return versaoModelo; }
    public void setVersaoModelo(String versaoModelo) { this.versaoModelo = versaoModelo; }

    @Override
    public String toString() {
        return "ModeloIa" +
                "\nidModelo=" + idModelo +
                "\nnmModelo='" + nmModelo + '\'' +
                "\ntpModelo='" + tpModelo + '\'' +
                "\nversaoModelo='" + versaoModelo + '\'';
    }
}
