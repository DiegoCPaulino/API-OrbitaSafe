package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Regiao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegiaoDao {

    public String inserir(Regiao regiao) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
                "insert into tb_regiao (id_reg, nm_reg, dt_cadastro, fk_usuario_id_usu, fk_subprefeitura_id_subpref) " +
                "values (?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, regiao.getIdReg());
            stmt.setString(2, regiao.getNmReg());
            stmt.setDate(3, Date.valueOf(regiao.getDtCadastro()));
            stmt.setInt(4, regiao.getFkUsuarioIdUsu());
            stmt.setInt(5, regiao.getFkSubprefeituraIdSubpref());
            stmt.execute();
            return "Regiao cadastrada com sucesso!";
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public int atualizar(Regiao regiao) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
                "update tb_regiao set nm_reg = ?, dt_cadastro = ?, fk_usuario_id_usu = ?, " +
                "fk_subprefeitura_id_subpref = ? where id_reg = ?"
            );
            stmt.setString(1, regiao.getNmReg());
            stmt.setDate(2, Date.valueOf(regiao.getDtCadastro()));
            stmt.setInt(3, regiao.getFkUsuarioIdUsu());
            stmt.setInt(4, regiao.getFkSubprefeituraIdSubpref());
            stmt.setInt(5, regiao.getIdReg());
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
                "delete from tb_regiao where id_reg = ?"
            );
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas;
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public List<Regiao> selecionar() throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Regiao> lista = new ArrayList<Regiao>();
            stmt = conexao.prepareStatement("select * from tb_regiao");
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public Regiao buscarPorId(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_regiao where id_reg = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            Regiao r = null;
            if (rs.next()) {
                r = mapear(rs);
            }
            return r;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public List<Regiao> selecionarPorUsuario(int idUsuario) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Regiao> lista = new ArrayList<Regiao>();
            stmt = conexao.prepareStatement(
                "select * from tb_regiao where fk_usuario_id_usu = ?"
            );
            stmt.setInt(1, idUsuario);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    private Regiao mapear(ResultSet rs) throws SQLException {
        Regiao r = new Regiao();
        r.setIdReg(rs.getInt("id_reg"));
        r.setNmReg(rs.getString("nm_reg"));
        r.setDtCadastro(rs.getDate("dt_cadastro").toLocalDate());
        r.setFkUsuarioIdUsu(rs.getInt("fk_usuario_id_usu"));
        r.setFkSubprefeituraIdSubpref(rs.getInt("fk_subprefeitura_id_subpref"));
        return r;
    }
}
