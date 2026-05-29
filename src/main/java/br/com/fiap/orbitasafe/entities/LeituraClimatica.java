package br.com.fiap.orbitasafe.entities;

import java.time.LocalDate;

public class LeituraClimatica {

    private int idLeitura;
    private double precipitacaoLeitura;
    private double umidadeLeitura;
    private double pressaoLeitura;
    private double ventoLeitura;
    private double temperaturaLeitura;
    private double umidSoloLeitura;
    private LocalDate dtLeitura;
    private int diaPrevisao;
    private int fkRegiaoIdReg;

    public LeituraClimatica() {}

    public LeituraClimatica(int idLeitura, double precipitacaoLeitura, double umidadeLeitura,
                            double pressaoLeitura, double ventoLeitura, double temperaturaLeitura,
                            double umidSoloLeitura, LocalDate dtLeitura, int diaPrevisao,
                            int fkRegiaoIdReg) {
        this.idLeitura           = idLeitura;
        this.precipitacaoLeitura = precipitacaoLeitura;
        this.umidadeLeitura      = umidadeLeitura;
        this.pressaoLeitura      = pressaoLeitura;
        this.ventoLeitura        = ventoLeitura;
        this.temperaturaLeitura  = temperaturaLeitura;
        this.umidSoloLeitura     = umidSoloLeitura;
        this.dtLeitura           = dtLeitura;
        this.diaPrevisao         = diaPrevisao;
        this.fkRegiaoIdReg       = fkRegiaoIdReg;
    }

    public int getIdLeitura() { return idLeitura; }
    public void setIdLeitura(int idLeitura) { this.idLeitura = idLeitura; }

    public double getPrecipitacaoLeitura() { return precipitacaoLeitura; }
    public void setPrecipitacaoLeitura(double precipitacaoLeitura) { this.precipitacaoLeitura = precipitacaoLeitura; }

    public double getUmidadeLeitura() { return umidadeLeitura; }
    public void setUmidadeLeitura(double umidadeLeitura) { this.umidadeLeitura = umidadeLeitura; }

    public double getPressaoLeitura() { return pressaoLeitura; }
    public void setPressaoLeitura(double pressaoLeitura) { this.pressaoLeitura = pressaoLeitura; }

    public double getVentoLeitura() { return ventoLeitura; }
    public void setVentoLeitura(double ventoLeitura) { this.ventoLeitura = ventoLeitura; }

    public double getTemperaturaLeitura() { return temperaturaLeitura; }
    public void setTemperaturaLeitura(double temperaturaLeitura) { this.temperaturaLeitura = temperaturaLeitura; }

    public double getUmidSoloLeitura() { return umidSoloLeitura; }
    public void setUmidSoloLeitura(double umidSoloLeitura) { this.umidSoloLeitura = umidSoloLeitura; }

    public LocalDate getDtLeitura() { return dtLeitura; }
    public void setDtLeitura(LocalDate dtLeitura) { this.dtLeitura = dtLeitura; }

    public int getDiaPrevisao() { return diaPrevisao; }
    public void setDiaPrevisao(int diaPrevisao) { this.diaPrevisao = diaPrevisao; }

    public int getFkRegiaoIdReg() { return fkRegiaoIdReg; }
    public void setFkRegiaoIdReg(int fkRegiaoIdReg) { this.fkRegiaoIdReg = fkRegiaoIdReg; }

    @Override
    public String toString() {
        return "LeituraClimatica" +
                "\nidLeitura=" + idLeitura +
                "\nprecipitacaoLeitura=" + precipitacaoLeitura +
                "\numidadeLeitura=" + umidadeLeitura +
                "\npressaoLeitura=" + pressaoLeitura +
                "\nventoLeitura=" + ventoLeitura +
                "\ntemperaturaLeitura=" + temperaturaLeitura +
                "\numidSoloLeitura=" + umidSoloLeitura +
                "\ndtLeitura=" + dtLeitura +
                "\ndiaPrevisao=" + diaPrevisao +
                "\nfkRegiaoIdReg=" + fkRegiaoIdReg;
    }
}
