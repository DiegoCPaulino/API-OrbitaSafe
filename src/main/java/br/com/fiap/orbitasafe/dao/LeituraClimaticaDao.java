package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.LeituraClimatica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeituraClimaticaDao {

    public Connection minhaConexao;

    public LeituraClimaticaDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public String inserir(LeituraClimatica leitura) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "insert into tb_leitura_climatica " +
            "(id_leitura, precipitacao_leitura, umidade_leitura, pressao_leitura, vento_leitura, " +
            "temperatura_leitura, umid_solo_leitura, dt_leitura, dia_previsao, fk_regiao_id_reg) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        stmt.setInt(1, leitura.getIdLeitura());
        stmt.setDouble(2, leitura.getPrecipitacaoLeitura());
        stmt.setDouble(3, leitura.getUmidadeLeitura());
        stmt.setDouble(4, leitura.getPressaoLeitura());
        stmt.setDouble(5, leitura.getVentoLeitura());
        stmt.setDouble(6, leitura.getTemperaturaLeitura());
        stmt.setDouble(7, leitura.getUmidSoloLeitura());
        stmt.setDate(8, Date.valueOf(leitura.getDtLeitura()));
        stmt.setInt(9, leitura.getDiaPrevisao());
        stmt.setInt(10, leitura.getFkRegiaoIdReg());
        stmt.execute();
        stmt.close();
        return "Leitura climatica registrada com sucesso!";
    }

    public LeituraClimatica buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_leitura_climatica where id_leitura = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            LeituraClimatica l = mapear(rs);
            stmt.close();
            return l;
        }
        stmt.close();
        return null;
    }

    public List<LeituraClimatica> selecionarPorRegiao(int idRegiao) throws SQLException {
        List<LeituraClimatica> lista = new ArrayList<LeituraClimatica>();
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_leitura_climatica where fk_regiao_id_reg = ? order by dt_leitura desc"
        );
        stmt.setInt(1, idRegiao);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
    }

    private LeituraClimatica mapear(ResultSet rs) throws SQLException {
        LeituraClimatica l = new LeituraClimatica();
        l.setIdLeitura(rs.getInt("id_leitura"));
        l.setPrecipitacaoLeitura(rs.getDouble("precipitacao_leitura"));
        l.setUmidadeLeitura(rs.getDouble("umidade_leitura"));
        l.setPressaoLeitura(rs.getDouble("pressao_leitura"));
        l.setVentoLeitura(rs.getDouble("vento_leitura"));
        l.setTemperaturaLeitura(rs.getDouble("temperatura_leitura"));
        l.setUmidSoloLeitura(rs.getDouble("umid_solo_leitura"));
        l.setDtLeitura(rs.getDate("dt_leitura").toLocalDate());
        l.setDiaPrevisao(rs.getInt("dia_previsao"));
        l.setFkRegiaoIdReg(rs.getInt("fk_regiao_id_reg"));
        return l;
    }
}
