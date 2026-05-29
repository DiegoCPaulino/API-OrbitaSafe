package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.CenarioClimatico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CenarioClimaticoDao {

    public Connection minhaConexao;

    public CenarioClimaticoDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public List<CenarioClimatico> selecionar() throws SQLException {
        List<CenarioClimatico> lista = new ArrayList<CenarioClimatico>();
        PreparedStatement stmt = minhaConexao.prepareStatement("select * from tb_cenario_climatico");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
    }

    public CenarioClimatico buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_cenario_climatico where id_cenario = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            CenarioClimatico c = mapear(rs);
            stmt.close();
            return c;
        }
        stmt.close();
        return null;
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
