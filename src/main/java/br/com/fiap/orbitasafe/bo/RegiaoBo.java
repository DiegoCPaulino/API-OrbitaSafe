package br.com.fiap.orbitasafe.bo;

import br.com.fiap.orbitasafe.dao.RegiaoDao;
import br.com.fiap.orbitasafe.dao.SubprefeituraDao;
import br.com.fiap.orbitasafe.dao.UsuarioDao;
import br.com.fiap.orbitasafe.entities.Regiao;

import java.sql.SQLException;
import java.util.List;

public class RegiaoBo {

    public String cadastrar(Regiao regiao) throws SQLException, ClassNotFoundException {
        if (regiao.getNmReg() == null || regiao.getNmReg().isBlank())
            throw new IllegalArgumentException("Nome da regiao e obrigatorio.");

        UsuarioDao usuarioDao = new UsuarioDao();
        if (usuarioDao.buscarPorId(regiao.getFkUsuarioIdUsu()) == null)
            throw new IllegalArgumentException("Usuario informado nao existe.");

        SubprefeituraDao subprefDao = new SubprefeituraDao();
        if (subprefDao.buscarPorId(regiao.getFkSubprefeituraIdSubpref()) == null)
            throw new IllegalArgumentException("Subprefeitura informada nao existe.");

        return new RegiaoDao().inserir(regiao);
    }

    public String atualizar(Regiao regiao) throws SQLException, ClassNotFoundException {
        if (regiao.getNmReg() == null || regiao.getNmReg().isBlank())
            throw new IllegalArgumentException("Nome da regiao e obrigatorio.");
        return new RegiaoDao().atualizar(regiao);
    }

    public String deletar(int id) throws SQLException, ClassNotFoundException {
        return new RegiaoDao().deletar(id);
    }

    public List<Regiao> listar() throws SQLException, ClassNotFoundException {
        return new RegiaoDao().selecionar();
    }

    public Regiao buscarPorId(int id) throws SQLException, ClassNotFoundException {
        return new RegiaoDao().buscarPorId(id);
    }

    public List<Regiao> listarPorUsuario(int idUsuario) throws SQLException, ClassNotFoundException {
        return new RegiaoDao().selecionarPorUsuario(idUsuario);
    }
}
