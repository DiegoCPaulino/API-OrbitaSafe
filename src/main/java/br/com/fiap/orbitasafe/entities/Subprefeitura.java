package br.com.fiap.orbitasafe.entities;

public class Subprefeitura {

    private int idSubpref;
    private int cdSubpref;
    private String nmSubpref;
    private double latitudeSubpref;
    private double longitudeSubpref;
    private int qtAlagamento;

    public Subprefeitura() {}

    public Subprefeitura(int idSubpref, int cdSubpref, String nmSubpref,
                         double latitudeSubpref, double longitudeSubpref, int qtAlagamento) {
        this.idSubpref        = idSubpref;
        this.cdSubpref        = cdSubpref;
        this.nmSubpref        = nmSubpref;
        this.latitudeSubpref  = latitudeSubpref;
        this.longitudeSubpref = longitudeSubpref;
        this.qtAlagamento     = qtAlagamento;
    }

    public int getIdSubpref() { return idSubpref; }
    public void setIdSubpref(int idSubpref) { this.idSubpref = idSubpref; }

    public int getCdSubpref() { return cdSubpref; }
    public void setCdSubpref(int cdSubpref) { this.cdSubpref = cdSubpref; }

    public String getNmSubpref() { return nmSubpref; }
    public void setNmSubpref(String nmSubpref) { this.nmSubpref = nmSubpref; }

    public double getLatitudeSubpref() { return latitudeSubpref; }
    public void setLatitudeSubpref(double latitudeSubpref) { this.latitudeSubpref = latitudeSubpref; }

    public double getLongitudeSubpref() { return longitudeSubpref; }
    public void setLongitudeSubpref(double longitudeSubpref) { this.longitudeSubpref = longitudeSubpref; }

    public int getQtAlagamento() { return qtAlagamento; }
    public void setQtAlagamento(int qtAlagamento) { this.qtAlagamento = qtAlagamento; }

    @Override
    public String toString() {
        return "Subprefeitura" +
                "\nidSubpref=" + idSubpref +
                "\ncdSubpref=" + cdSubpref +
                "\nnmSubpref='" + nmSubpref + '\'' +
                "\nlatitudeSubpref=" + latitudeSubpref +
                "\nlongitudeSubpref=" + longitudeSubpref +
                "\nqtAlagamento=" + qtAlagamento;
    }
}
