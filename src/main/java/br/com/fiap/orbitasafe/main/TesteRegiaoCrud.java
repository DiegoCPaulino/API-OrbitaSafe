package br.com.fiap.orbitasafe.main;

import br.com.fiap.orbitasafe.bo.RegiaoBo;
import br.com.fiap.orbitasafe.entities.Regiao;

import java.time.LocalDate;
import java.util.List;

public class TesteRegiaoCrud {

    public static void main(String[] args) throws Exception {
        RegiaoBo bo = new RegiaoBo();

        // Cadastrar — usa usuario id=1 e subprefeitura id=1 já presentes no DML da Fase 2
        Regiao regiao = new Regiao();
        regiao.setIdReg(999);
        regiao.setNmReg("Regiao Teste Fase4");
        regiao.setDtCadastro(LocalDate.now());
        regiao.setFkUsuarioIdUsu(1);
        regiao.setFkSubprefeituraIdSubpref(1);
        System.out.println(bo.cadastrar(regiao));

        // Buscar
        Regiao encontrada = bo.buscarPorId(999);
        System.out.println("Buscar: " + encontrada);

        // Atualizar
        encontrada.setNmReg("Regiao Teste Fase4 — Atualizada");
        System.out.println(bo.atualizar(encontrada));

        // Listar por usuario
        List<Regiao> lista = bo.listarPorUsuario(1);
        System.out.println("Regioes do usuario 1: " + lista.size());

        // Deletar
        System.out.println(bo.deletar(999));
    }
}
