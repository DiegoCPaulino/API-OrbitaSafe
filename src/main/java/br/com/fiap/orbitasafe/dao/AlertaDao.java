package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Alerta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertaDao {

    public Connection minhaConexao;

    public AlertaDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public String inserir(Alerta alerta) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
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
        stmt.close();
        return "Alerta registrado com sucesso!";
    }

    public Alerta buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_alerta where id_alerta = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Alerta a = mapear(rs);
            stmt.close();
            return a;
        }
        stmt.close();
        return null;
    }

    public List<Alerta> selecionarPorRegiao(int idRegiao) throws SQLException {
        List<Alerta> lista = new ArrayList<Alerta>();
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_alerta where fk_regiao_id_reg = ? order by dt_alerta desc"
        );
        stmt.setInt(1, idRegiao);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
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
