package br.com.fiap.orbitasafe.services;

// Dados brutos vindos da fonte de clima ANTES do tratamento para a IA.
public class DadosClimaticos {

    private double umidadeRelativa;      // %
    private double pressao;              // hPa
    private double velocidadeVento;      // km/h
    private double temperatura;          // °C
    private double umidadeSolo;          // m³/m³
    private double[] precipitacaoHoraria; // array horário — usado para somar a janela

    public DadosClimaticos() {}

    public DadosClimaticos(double umidadeRelativa, double pressao, double velocidadeVento,
                           double temperatura, double umidadeSolo, double[] precipitacaoHoraria) {
        this.umidadeRelativa      = umidadeRelativa;
        this.pressao              = pressao;
        this.velocidadeVento      = velocidadeVento;
        this.temperatura          = temperatura;
        this.umidadeSolo          = umidadeSolo;
        this.precipitacaoHoraria  = precipitacaoHoraria;
    }

    public double getUmidadeRelativa() { return umidadeRelativa; }
    public void setUmidadeRelativa(double umidadeRelativa) { this.umidadeRelativa = umidadeRelativa; }

    public double getPressao() { return pressao; }
    public void setPressao(double pressao) { this.pressao = pressao; }

    public double getVelocidadeVento() { return velocidadeVento; }
    public void setVelocidadeVento(double velocidadeVento) { this.velocidadeVento = velocidadeVento; }

    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }

    public double getUmidadeSolo() { return umidadeSolo; }
    public void setUmidadeSolo(double umidadeSolo) { this.umidadeSolo = umidadeSolo; }

    public double[] getPrecipitacaoHoraria() { return precipitacaoHoraria; }
    public void setPrecipitacaoHoraria(double[] precipitacaoHoraria) { this.precipitacaoHoraria = precipitacaoHoraria; }

    @Override
    public String toString() {
        return "DadosClimaticos" +
                "\numidadeRelativa=" + umidadeRelativa +
                "\npressao=" + pressao +
                "\nvelocidadeVento=" + velocidadeVento +
                "\ntemperatura=" + temperatura +
                "\numidadeSolo=" + umidadeSolo +
                "\nprecipitacaoHoraria.length=" + (precipitacaoHoraria != null ? precipitacaoHoraria.length : 0);
    }
}
