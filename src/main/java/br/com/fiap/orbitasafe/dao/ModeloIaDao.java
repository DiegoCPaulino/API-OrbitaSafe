package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.ModeloIa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModeloIaDao {

    public List<ModeloIa> selecionar() throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<ModeloIa> lista = new ArrayList<ModeloIa>();
            stmt = conexao.prepareStatement("select * from tb_modelo_ia");
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

    public ModeloIa buscarPorId(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_modelo_ia where id_modelo = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            ModeloIa m = null;
            if (rs.next()) {
                m = mapear(rs);
            }
            return m;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
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
