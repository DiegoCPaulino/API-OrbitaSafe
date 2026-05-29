package br.com.fiap.orbitasafe.services;

import br.com.fiap.orbitasafe.entities.Regiao;

public interface ServicoClima {
    DadosClimaticos obterDados(Regiao regiao) throws Exception;
}
