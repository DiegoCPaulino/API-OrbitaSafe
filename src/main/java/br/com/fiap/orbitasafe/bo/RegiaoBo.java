package br.com.fiap.orbitasafe.bo;

import br.com.fiap.orbitasafe.dao.RegiaoDao;
import br.com.fiap.orbitasafe.dao.SubprefeituraDao;
import br.com.fiap.orbitasafe.dao.UsuarioDao;
import br.com.fiap.orbitasafe.entities.Regiao;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import br.com.fiap.orbitasafe.exceptions.ValidacaoException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RegiaoBo {

    public String cadastrar(Regiao regiao) throws SQLException, ClassNotFoundException {
        if (regiao.getNmReg() == null || regiao.getNmReg().isBlank())
            throw new ValidacaoException("Nome da regiao e obrigatorio.");

        UsuarioDao usuarioDao = new UsuarioDao();
        if (usuarioDao.buscarPorId(regiao.getFkUsuarioIdUsu()) == null)
            throw new ValidacaoException("Usuario informado nao existe.");

        SubprefeituraDao subprefDao = new SubprefeituraDao();
        if (subprefDao.buscarPorId(regiao.getFkSubprefeituraIdSubpref()) == null)
            throw new ValidacaoException("Subprefeitura informada nao existe.");

        if (regiao.getDtCadastro() == null) {
            regiao.setDtCadastro(LocalDate.now());
        }
        return new RegiaoDao().inserir(regiao);
    }

    public String atualizar(Regiao regiao) throws SQLException, ClassNotFoundException {
        if (regiao.getNmReg() == null || regiao.getNmReg().isBlank())
            throw new ValidacaoException("Nome da regiao e obrigatorio.");

        RegiaoDao dao = new RegiaoDao();
        Regiao atual = dao.buscarPorId(regiao.getIdReg());
        if (atual == null) {
            throw new RegistroNaoEncontradoException("Regiao nao encontrada: id=" + regiao.getIdReg());
        }
        // dtCadastro é a data de criação: preserva a do banco se o PUT não enviar
        if (regiao.getDtCadastro() == null) {
            regiao.setDtCadastro(atual.getDtCadastro());
        }
        dao.atualizar(regiao);
        return "Regiao atualizada com sucesso!";
    }

    public String deletar(int id) throws SQLException, ClassNotFoundException {
        int linhasAfetadas = new RegiaoDao().deletar(id);
        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Regiao nao encontrada: id=" + id);
        }
        return "Regiao removida com sucesso!";
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
