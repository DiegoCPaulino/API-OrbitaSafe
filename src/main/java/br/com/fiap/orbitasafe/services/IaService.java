package br.com.fiap.orbitasafe.services;

import java.util.Map;

public interface IaService {
    RespostaIa avaliarRisco(Map<String, Object> variaveis) throws Exception;
}
