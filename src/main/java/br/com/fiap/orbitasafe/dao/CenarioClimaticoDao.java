package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.CenarioClimatico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CenarioClimaticoDao {

    public List<CenarioClimatico> selecionar() throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<CenarioClimatico> lista = new ArrayList<CenarioClimatico>();
            stmt = conexao.prepareStatement("select * from tb_cenario_climatico");
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

    public CenarioClimatico buscarPorId(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_cenario_climatico where id_cenario = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            CenarioClimatico c = null;
            if (rs.next()) {
                c = mapear(rs);
            }
            return c;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    private CenarioClimatico mapear(ResultSet rs) throws SQLException {
        CenarioClimatico c = new CenarioClimatico();
        c.setIdCenario(rs.getInt("id_cenario"));
        c.setNmCenario(rs.getString("nm_cenario"));
        c.setPrecipitacaoCenario(rs.getDouble("precipitacao_cenario"));
        c.setUmidadeCenario(rs.getDouble("umidade_cenario"));
        c.setPressaoCenario(rs.getDouble("pressao_cenario"));
        c.setVentoCenario(rs.getDouble("vento_cenario"));
        c.setTemperaturaCenario(rs.getDouble("temperatura_cenario"));
        c.setUmidSoloCenario(rs.getDouble("umid_solo_cenario"));
        c.setNivelCenario(rs.getString("nivel_cenario"));
        return c;
    }
}
