package br.com.fiap.orbitasafe.services;

// POJO que espelha o JSON da Open-Meteo literalmente (padrão "Planeta" das aulas).
// Campos em snake_case refletem as chaves JSON. Sem @SerializedName.
// Gson acessa os campos diretamente por reflexão.
public class RespostaOpenMeteo {

    double latitude;
    double longitude;
    Hourly hourly;

    public static class Hourly {
        String[] time;
        double[] temperature_2m;
        double[] relative_humidity_2m;
        double[] precipitation;
        double[] pressure_msl;
        double[] wind_speed_10m;
        double[] soil_moisture_0_to_1cm;
    }
}
