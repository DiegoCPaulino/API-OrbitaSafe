package br.com.fiap.orbitasafe.entities;

import java.time.LocalDate;

public class Alerta {

    private int idAlerta;
    private String nivelAlerta;
    private String tpEvento;
    private String dsAlerta;
    private LocalDate dtAlerta;
    private int fkRegiaoIdReg;
    private int fkLeituraIdLeitura;

    public Alerta() {}

    public Alerta(int idAlerta, String nivelAlerta, String tpEvento, String dsAlerta,
                  LocalDate dtAlerta, int fkRegiaoIdReg, int fkLeituraIdLeitura) {
        this.idAlerta           = idAlerta;
        this.nivelAlerta        = nivelAlerta;
        this.tpEvento           = tpEvento;
        this.dsAlerta           = dsAlerta;
        this.dtAlerta           = dtAlerta;
        this.fkRegiaoIdReg      = fkRegiaoIdReg;
        this.fkLeituraIdLeitura = fkLeituraIdLeitura;
    }

    public int getIdAlerta() { return idAlerta; }
    public void setIdAlerta(int idAlerta) { this.idAlerta = idAlerta; }

    public String getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(String nivelAlerta) { this.nivelAlerta = nivelAlerta; }

    public String getTpEvento() { return tpEvento; }
    public void setTpEvento(String tpEvento) { this.tpEvento = tpEvento; }

    public String getDsAlerta() { return dsAlerta; }
    public void setDsAlerta(String dsAlerta) { this.dsAlerta = dsAlerta; }

    public LocalDate getDtAlerta() { return dtAlerta; }
    public void setDtAlerta(LocalDate dtAlerta) { this.dtAlerta = dtAlerta; }

    public int getFkRegiaoIdReg() { return fkRegiaoIdReg; }
    public void setFkRegiaoIdReg(int fkRegiaoIdReg) { this.fkRegiaoIdReg = fkRegiaoIdReg; }

    public int getFkLeituraIdLeitura() { return fkLeituraIdLeitura; }
    public void setFkLeituraIdLeitura(int fkLeituraIdLeitura) { this.fkLeituraIdLeitura = fkLeituraIdLeitura; }

    @Override
    public String toString() {
        return "Alerta" +
                "\nidAlerta=" + idAlerta +
                "\nnivelAlerta='" + nivelAlerta + '\'' +
                "\ntpEvento='" + tpEvento + '\'' +
                "\ndsAlerta='" + dsAlerta + '\'' +
                "\ndtAlerta=" + dtAlerta +
                "\nfkRegiaoIdReg=" + fkRegiaoIdReg +
                "\nfkLeituraIdLeitura=" + fkLeituraIdLeitura;
    }
}
