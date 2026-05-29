package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.ModeloIa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModeloIaDao {

    public Connection minhaConexao;

    public ModeloIaDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public List<ModeloIa> selecionar() throws SQLException {
        List<ModeloIa> lista = new ArrayList<ModeloIa>();
        PreparedStatement stmt = minhaConexao.prepareStatement("select * from tb_modelo_ia");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
    }

    public ModeloIa buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_modelo_ia where id_modelo = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            ModeloIa m = mapear(rs);
            stmt.close();
            return m;
        }
        stmt.close();
        return null;
    }

    private ModeloIa mapear(ResultSet rs) throws SQLException {
        ModeloIa m = new ModeloIa();
        m.setIdModelo(rs.getInt("id_modelo"));
        m.setNmModelo(rs.getString("nm_modelo"));
        m.setTpModelo(rs.getString("tp_modelo"));
        m.setVersaoModelo(rs.getString("versao_modelo"));
        return m;
    }
}
