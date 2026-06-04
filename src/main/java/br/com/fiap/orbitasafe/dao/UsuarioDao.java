package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

    // Conexao aberta/fechada por metodo: em servidor de vida longa, manter no atributo vazaria sessoes Oracle (ORA-02391).
    public String inserir(Usuario usuario) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
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
            return "Usuario cadastrado com sucesso!";
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public int atualizar(Usuario usuario) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
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
            return linhasAfetadas;
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public int deletar(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
                "delete from tb_usuario where id_usu = ?"
            );
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas;
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public List<Usuario> selecionar() throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Usuario> lista = new ArrayList<Usuario>();
            stmt = conexao.prepareStatement("select * from tb_usuario");
            rs = stmt.executeQuery();
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
            return lista;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public Usuario buscarPorId(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_usuario where id_usu = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            Usuario u = null;
            if (rs.next()) {
                u = new Usuario();
                u.setIdUsu(rs.getInt("id_usu"));
                u.setNmUsu(rs.getString("nm_usu"));
                u.setEmailUsu(rs.getString("email_usu"));
                u.setSenhaUsu(rs.getString("senha_usu"));
                u.setTpUsu(rs.getString("tp_usu"));
                u.setDtCadastro(rs.getDate("dt_cadastro").toLocalDate());
            }
            return u;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_usuario where email_usu = ?"
            );
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            Usuario u = null;
            if (rs.next()) {
                u = new Usuario();
                u.setIdUsu(rs.getInt("id_usu"));
                u.setNmUsu(rs.getString("nm_usu"));
                u.setEmailUsu(rs.getString("email_usu"));
                u.setSenhaUsu(rs.getString("senha_usu"));
                u.setTpUsu(rs.getString("tp_usu"));
                u.setDtCadastro(rs.getDate("dt_cadastro").toLocalDate());
            }
            return u;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }
}
