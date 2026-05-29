package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Regiao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegiaoDao {

    public Connection minhaConexao;

    public RegiaoDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public String inserir(Regiao regiao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "insert into tb_regiao (id_reg, nm_reg, dt_cadastro, fk_usuario_id_usu, fk_subprefeitura_id_subpref) " +
            "values (?, ?, ?, ?, ?)"
        );
        stmt.setInt(1, regiao.getIdReg());
        stmt.setString(2, regiao.getNmReg());
        stmt.setDate(3, Date.valueOf(regiao.getDtCadastro()));
        stmt.setInt(4, regiao.getFkUsuarioIdUsu());
        stmt.setInt(5, regiao.getFkSubprefeituraIdSubpref());
        stmt.execute();
        stmt.close();
        return "Regiao cadastrada com sucesso!";
    }

    public String atualizar(Regiao regiao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "update tb_regiao set nm_reg = ?, dt_cadastro = ?, fk_usuario_id_usu = ?, " +
            "fk_subprefeitura_id_subpref = ? where id_reg = ?"
        );
        stmt.setString(1, regiao.getNmReg());
        stmt.setDate(2, Date.valueOf(regiao.getDtCadastro()));
        stmt.setInt(3, regiao.getFkUsuarioIdUsu());
        stmt.setInt(4, regiao.getFkSubprefeituraIdSubpref());
        stmt.setInt(5, regiao.getIdReg());
        stmt.executeUpdate();
        stmt.close();
        return "Regiao atualizada com sucesso!";
    }

    public String deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "delete from tb_regiao where id_reg = ?"
        );
        stmt.setInt(1, id);
        stmt.execute();
        stmt.close();
        return "Regiao removida com sucesso!";
    }

    public List<Regiao> selecionar() throws SQLException {
        List<Regiao> lista = new ArrayList<Regiao>();
        PreparedStatement stmt = minhaConexao.prepareStatement("select * from tb_regiao");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
    }

    public Regiao buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_regiao where id_reg = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Regiao r = mapear(rs);
            stmt.close();
            return r;
        }
        stmt.close();
        return null;
    }

    public List<Regiao> selecionarPorUsuario(int idUsuario) throws SQLException {
        List<Regiao> lista = new ArrayList<Regiao>();
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_regiao where fk_usuario_id_usu = ?"
        );
        stmt.setInt(1, idUsuario);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
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
