package br.com.fiap.orbitasafe.bo;

import br.com.fiap.orbitasafe.dao.UsuarioDao;
import br.com.fiap.orbitasafe.entities.Usuario;

import java.sql.SQLException;
import java.util.List;

public class UsuarioBo {

    public String cadastrar(Usuario usuario) throws SQLException, ClassNotFoundException {
        if (usuario.getNmUsu() == null || usuario.getNmUsu().isBlank())
            throw new IllegalArgumentException("Nome do usuario e obrigatorio.");
        if (usuario.getEmailUsu() == null || !usuario.getEmailUsu().contains("@"))
            throw new IllegalArgumentException("E-mail invalido.");
        if (usuario.getSenhaUsu() == null || usuario.getSenhaUsu().length() < 6)
            throw new IllegalArgumentException("Senha deve ter no minimo 6 caracteres.");

        UsuarioDao dao = new UsuarioDao();
        if (dao.buscarPorEmail(usuario.getEmailUsu()) != null)
            throw new IllegalArgumentException("E-mail ja cadastrado.");

        usuario.setSenhaUsu(SenhaUtil.gerarHash(usuario.getSenhaUsu()));
        return dao.inserir(usuario);
    }

    public Usuario login(String email, String senha) throws SQLException, ClassNotFoundException {
        UsuarioDao dao = new UsuarioDao();
        Usuario usuario = dao.buscarPorEmail(email);
        if (usuario == null)
            return null;
        if (SenhaUtil.gerarHash(senha).equals(usuario.getSenhaUsu()))
            return usuario;
        return null;
    }

    public String atualizar(Usuario usuario) throws SQLException, ClassNotFoundException {
        if (usuario.getNmUsu() == null || usuario.getNmUsu().isBlank())
            throw new IllegalArgumentException("Nome do usuario e obrigatorio.");
        if (usuario.getEmailUsu() == null || !usuario.getEmailUsu().contains("@"))
            throw new IllegalArgumentException("E-mail invalido.");

        UsuarioDao dao = new UsuarioDao();

        if (usuario.getSenhaUsu() != null && !usuario.getSenhaUsu().isBlank()) {
            // nova senha informada: aplica hash
            usuario.setSenhaUsu(SenhaUtil.gerarHash(usuario.getSenhaUsu()));
        } else {
            // senha em branco: mantém a que está no banco
            Usuario atual = dao.buscarPorId(usuario.getIdUsu());
            if (atual != null)
                usuario.setSenhaUsu(atual.getSenhaUsu());
        }

        return dao.atualizar(usuario);
    }

    public String deletar(int id) throws SQLException, ClassNotFoundException {
        return new UsuarioDao().deletar(id);
    }

    public List<Usuario> listar() throws SQLException, ClassNotFoundException {
        return new UsuarioDao().selecionar();
    }

    public Usuario buscarPorId(int id) throws SQLException, ClassNotFoundException {
        return new UsuarioDao().buscarPorId(id);
    }
}
