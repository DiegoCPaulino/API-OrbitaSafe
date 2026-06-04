package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Alerta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertaDao {

    public String inserir(Alerta alerta) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
                "insert into tb_alerta " +
                "(id_alerta, nivel_alerta, tp_evento, ds_alerta, dt_alerta, fk_regiao_id_reg, fk_leitura_id_leitura) " +
                "values (?, ?, ?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, alerta.getIdAlerta());
            stmt.setString(2, alerta.getNivelAlerta());
            stmt.setString(3, alerta.getTpEvento());
            stmt.setString(4, alerta.getDsAlerta());
            stmt.setDate(5, Date.valueOf(alerta.getDtAlerta()));
            stmt.setInt(6, alerta.getFkRegiaoIdReg());
            stmt.setInt(7, alerta.getFkLeituraIdLeitura());
            stmt.execute();
            return "Alerta registrado com sucesso!";
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public Alerta buscarPorId(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_alerta where id_alerta = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            Alerta a = null;
            if (rs.next()) {
                a = mapear(rs);
            }
            return a;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public List<Alerta> selecionarPorRegiao(int idRegiao) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Alerta> lista = new ArrayList<Alerta>();
            stmt = conexao.prepareStatement(
                "select * from tb_alerta where fk_regiao_id_reg = ? order by dt_alerta desc"
            );
            stmt.setInt(1, idRegiao);
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

    private Alerta mapear(ResultSet rs) throws SQLException {
        Alerta a = new Alerta();
        a.setIdAlerta(rs.getInt("id_alerta"));
        a.setNivelAlerta(rs.getString("nivel_alerta"));
        a.setTpEvento(rs.getString("tp_evento"));
        a.setDsAlerta(rs.getString("ds_alerta"));
        a.setDtAlerta(rs.getDate("dt_alerta").toLocalDate());
        a.setFkRegiaoIdReg(rs.getInt("fk_regiao_id_reg"));
        a.setFkLeituraIdLeitura(rs.getInt("fk_leitura_id_leitura"));
        return a;
    }
}
