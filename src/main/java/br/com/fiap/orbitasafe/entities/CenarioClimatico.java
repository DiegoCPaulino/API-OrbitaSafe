package br.com.fiap.orbitasafe.entities;

public class CenarioClimatico {

    private int idCenario;
    private String nmCenario;
    private double precipitacaoCenario;
    private double umidadeCenario;
    private double pressaoCenario;
    private double ventoCenario;
    private double temperaturaCenario;
    private double umidSoloCenario;
    private String nivelCenario;

    public CenarioClimatico() {}

    public CenarioClimatico(int idCenario, String nmCenario, double precipitacaoCenario,
                            double umidadeCenario, double pressaoCenario, double ventoCenario,
                            double temperaturaCenario, double umidSoloCenario, String nivelCenario) {
        this.idCenario           = idCenario;
        this.nmCenario           = nmCenario;
        this.precipitacaoCenario = precipitacaoCenario;
        this.umidadeCenario      = umidadeCenario;
        this.pressaoCenario      = pressaoCenario;
        this.ventoCenario        = ventoCenario;
        this.temperaturaCenario  = temperaturaCenario;
        this.umidSoloCenario     = umidSoloCenario;
        this.nivelCenario        = nivelCenario;
    }

    public int getIdCenario() { return idCenario; }
    public void setIdCenario(int idCenario) { this.idCenario = idCenario; }

    public String getNmCenario() { return nmCenario; }
    public void setNmCenario(String nmCenario) { this.nmCenario = nmCenario; }

    public double getPrecipitacaoCenario() { return precipitacaoCenario; }
    public void setPrecipitacaoCenario(double precipitacaoCenario) { this.precipitacaoCenario = precipitacaoCenario; }

    public double getUmidadeCenario() { return umidadeCenario; }
    public void setUmidadeCenario(double umidadeCenario) { this.umidadeCenario = umidadeCenario; }

    public double getPressaoCenario() { return pressaoCenario; }
    public void setPressaoCenario(double pressaoCenario) { this.pressaoCenario = pressaoCenario; }

    public double getVentoCenario() { return ventoCenario; }
    public void setVentoCenario(double ventoCenario) { this.ventoCenario = ventoCenario; }

    public double getTemperaturaCenario() { return temperaturaCenario; }
    public void setTemperaturaCenario(double temperaturaCenario) { this.temperaturaCenario = temperaturaCenario; }

    public double getUmidSoloCenario() { return umidSoloCenario; }
    public void setUmidSoloCenario(double umidSoloCenario) { this.umidSoloCenario = umidSoloCenario; }

    public String getNivelCenario() { return nivelCenario; }
    public void setNivelCenario(String nivelCenario) { this.nivelCenario = nivelCenario; }

    @Override
    public String toString() {
        return "CenarioClimatico" +
                "\nidCenario=" + idCenario +
                "\nnmCenario='" + nmCenario + '\'' +
                "\nprecipitacaoCenario=" + precipitacaoCenario +
                "\numidadeCenario=" + umidadeCenario +
                "\npressaoCenario=" + pressaoCenario +
                "\nventoCenario=" + ventoCenario +
                "\ntemperaturaCenario=" + temperaturaCenario +
                "\numidSoloCenario=" + umidSoloCenario +
                "\nnivelCenario='" + nivelCenario + '\'';
    }
}
