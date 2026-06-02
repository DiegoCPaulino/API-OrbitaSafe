package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

    public Connection minhaConexao;

    public UsuarioDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public String inserir(Usuario usuario) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "insert into tb_usuario (id_usu, nm_usu, email_usu, senha_usu, tp_usu, dt_cadastro) " +
            "values (?, ?, ?, ?, ?, ?)"
        );
        stmt.setInt(1, usuario.getIdUsu());
        stmt.setString(2, usuario.getNmUsu());
        stmt.setString(3, usuario.getEmailUsu());
        stmt.setString(4, usuario.getSenhaUsu());
        stmt.setString(5, usuario.getTpUsu());
        stmt.setDate(6, Date.valueOf(usuario.getDtCadastro()));
        stmt.execute();
        stmt.close();
        return "Usuario cadastrado com sucesso!";
    }

    public int atualizar(Usuario usuario) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "update tb_usuario set nm_usu = ?, email_usu = ?, senha_usu = ?, tp_usu = ?, dt_cadastro = ? " +
            "where id_usu = ?"
        );
        stmt.setString(1, usuario.getNmUsu());
        stmt.setString(2, usuario.getEmailUsu());
        stmt.setString(3, usuario.getSenhaUsu());
        stmt.setString(4, usuario.getTpUsu());
        stmt.setDate(5, Date.valueOf(usuario.getDtCadastro()));
        stmt.setInt(6, usuario.getIdUsu());
        int linhasAfetadas = stmt.executeUpdate();
        stmt.close();
        return linhasAfetadas;
    }

    public int deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "delete from tb_usuario where id_usu = ?"
        );
        stmt.setInt(1, id);
        int linhasAfetadas = stmt.executeUpdate();
        stmt.close();
        return linhasAfetadas;
    }

    public List<Usuario> selecionar() throws SQLException {
        List<Usuario> lista = new ArrayList<Usuario>();
        PreparedStatement stmt = minhaConexao.prepareStatement("select * from tb_usuario");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Usuario u = new Usuario();
            u.setIdUsu(rs.getInt("id_usu"));
            u.setNmUsu(rs.getString("nm_usu"));
            u.setEmailUsu(rs.getString("email_usu"));
            u.setSenhaUsu(rs.getString("senha_usu"));
            u.setTpUsu(rs.getString("tp_usu"));
            u.setDtCadastro(rs.getDate("dt_cadastro").toLocalDate());
            lista.add(u);
        }
        stmt.close();
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_usuario where id_usu = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Usuario u = new Usuario();
            u.setIdUsu(rs.getInt("id_usu"));
            u.setNmUsu(rs.getString("nm_usu"));
            u.setEmailUsu(rs.getString("email_usu"));
            u.setSenhaUsu(rs.getString("senha_usu"));
            u.setTpUsu(rs.getString("tp_usu"));
            u.setDtCadastro(rs.getDate("dt_cadastro").toLocalDate());
            stmt.close();
            return u;
        }
        stmt.close();
        return null;
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_usuario where email_usu = ?"
        );
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Usuario u = new Usuario();
            u.setIdUsu(rs.getInt("id_usu"));
            u.setNmUsu(rs.getString("nm_usu"));
            u.setEmailUsu(rs.getString("email_usu"));
            u.setSenhaUsu(rs.getString("senha_usu"));
            u.setTpUsu(rs.getString("tp_usu"));
            u.setDtCadastro(rs.getDate("dt_cadastro").toLocalDate());
            stmt.close();
            return u;
        }
        stmt.close();
        return null;
    }
}
