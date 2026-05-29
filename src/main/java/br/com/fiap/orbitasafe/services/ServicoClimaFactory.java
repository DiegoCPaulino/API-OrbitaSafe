package br.com.fiap.orbitasafe.services;

// Seleciona a fonte de clima via variável de ambiente FONTE_CLIMA.
// Valores: OPEN_METEO | SIMULADO (default: SIMULADO).
// Extensão consciente: permite trocar a fonte em runtime sem recompilar.
public class ServicoClimaFactory {

    public static ServicoClima criar() {
        String fonte = System.getenv("FONTE_CLIMA");
        if ("OPEN_METEO".equalsIgnoreCase(fonte)) {
            return new ClimaOpenMeteo();
        }
        return new ClimaSimulado();
    }
}
