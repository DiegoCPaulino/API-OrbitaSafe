package br.com.fiap.orbitasafe.bo;

import br.com.fiap.orbitasafe.dao.UsuarioDao;
import br.com.fiap.orbitasafe.entities.Usuario;
import br.com.fiap.orbitasafe.exceptions.CredenciaisInvalidasException;
import br.com.fiap.orbitasafe.exceptions.EmailJaCadastradoException;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import br.com.fiap.orbitasafe.exceptions.ValidacaoException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class UsuarioBo {

    public String cadastrar(Usuario usuario) throws SQLException, ClassNotFoundException {
        if (usuario.getNmUsu() == null || usuario.getNmUsu().isBlank())
            throw new ValidacaoException("Nome do usuario e obrigatorio.");
        if (usuario.getEmailUsu() == null || !usuario.getEmailUsu().contains("@"))
            throw new ValidacaoException("E-mail invalido.");
        if (usuario.getSenhaUsu() == null || usuario.getSenhaUsu().length() < 6)
            throw new ValidacaoException("Senha deve ter no minimo 6 caracteres.");

        UsuarioDao dao = new UsuarioDao();
        if (dao.buscarPorEmail(usuario.getEmailUsu()) != null)
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");

        if (usuario.getDtCadastro() == null) {
            usuario.setDtCadastro(LocalDate.now());
        }
        usuario.setSenhaUsu(SenhaUtil.gerarHash(usuario.getSenhaUsu()));
        return dao.inserir(usuario);
    }

    public Usuario login(String email, String senha) throws SQLException, ClassNotFoundException {
        UsuarioDao dao = new UsuarioDao();
        Usuario usuario = dao.buscarPorEmail(email);
        if (usuario == null || !SenhaUtil.gerarHash(senha).equals(usuario.getSenhaUsu()))
            throw new CredenciaisInvalidasException("E-mail ou senha invalidos.");
        return usuario;
    }

    public String atualizar(Usuario usuario) throws SQLException, ClassNotFoundException {
        if (usuario.getNmUsu() == null || usuario.getNmUsu().isBlank())
            throw new ValidacaoException("Nome do usuario e obrigatorio.");
        if (usuario.getEmailUsu() == null || !usuario.getEmailUsu().contains("@"))
            throw new ValidacaoException("E-mail invalido.");

        UsuarioDao dao = new UsuarioDao();
        Usuario atual = dao.buscarPorId(usuario.getIdUsu());
        if (atual == null) {
            throw new RegistroNaoEncontradoException("Usuario nao encontrado: id=" + usuario.getIdUsu());
        }

        if (usuario.getSenhaUsu() != null && !usuario.getSenhaUsu().isBlank()) {
            // nova senha informada: aplica hash
            usuario.setSenhaUsu(SenhaUtil.gerarHash(usuario.getSenhaUsu()));
        } else if (atual != null) {
            // senha em branco: mantém a que está no banco
            usuario.setSenhaUsu(atual.getSenhaUsu());
        }

        // dtCadastro é a data de criação: preserva a do banco se o PUT não enviar
        if (usuario.getDtCadastro() == null && atual != null) {
            usuario.setDtCadastro(atual.getDtCadastro());
        }

        dao.atualizar(usuario);
        return "Usuario atualizado com sucesso!";
    }

    public String deletar(int id) throws SQLException, ClassNotFoundException {
        int linhasAfetadas = new UsuarioDao().deletar(id);
        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Usuario nao encontrado: id=" + id);
        }
        return "Usuario removido com sucesso!";
    }

    public List<Usuario> listar() throws SQLException, ClassNotFoundException {
        return new UsuarioDao().selecionar();
    }

    public Usuario buscarPorId(int id) throws SQLException, ClassNotFoundException {
        return new UsuarioDao().buscarPorId(id);
    }
}
