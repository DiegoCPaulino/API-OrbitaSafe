package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.AlertaModelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertaModeloDao {

    public Connection minhaConexao;

    public AlertaModeloDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public String inserir(AlertaModelo alertaModelo) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "insert into tb_alerta_modelo (fk_alerta_id_alerta, fk_modelo_id_modelo, score_modelo) " +
            "values (?, ?, ?)"
        );
        stmt.setInt(1, alertaModelo.getFkAlertaIdAlerta());
        stmt.setInt(2, alertaModelo.getFkModeloIdModelo());
        stmt.setDouble(3, alertaModelo.getScoreModelo());
        stmt.execute();
        stmt.close();
        return "Associacao alerta-modelo registrada com sucesso!";
    }

    public List<AlertaModelo> selecionarPorAlerta(int idAlerta) throws SQLException {
        List<AlertaModelo> lista = new ArrayList<AlertaModelo>();
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_alerta_modelo where fk_alerta_id_alerta = ?"
        );
        stmt.setInt(1, idAlerta);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            AlertaModelo am = new AlertaModelo();
            am.setFkAlertaIdAlerta(rs.getInt("fk_alerta_id_alerta"));
            am.setFkModeloIdModelo(rs.getInt("fk_modelo_id_modelo"));
            am.setScoreModelo(rs.getDouble("score_modelo"));
            lista.add(am);
        }
        stmt.close();
        return lista;
    }
}
