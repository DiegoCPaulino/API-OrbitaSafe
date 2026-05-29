package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Subprefeitura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubprefeituraDao {

    public Connection minhaConexao;

    public SubprefeituraDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    public List<Subprefeitura> selecionar() throws SQLException {
        List<Subprefeitura> lista = new ArrayList<Subprefeitura>();
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_subprefeitura order by nm_subpref"
        );
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        stmt.close();
        return lista;
    }

    public Subprefeitura buscarPorId(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
            "select * from tb_subprefeitura where id_subpref = ?"
        );
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Subprefeitura s = mapear(rs);
            stmt.close();
            return s;
        }
        stmt.close();
        return null;
    }

    private Subprefeitura mapear(ResultSet rs) throws SQLException {
        Subprefeitura s = new Subprefeitura();
        s.setIdSubpref(rs.getInt("id_subpref"));
        s.setCdSubpref(rs.getInt("cd_subpref"));
        s.setNmSubpref(rs.getString("nm_subpref"));
        s.setLatitudeSubpref(rs.getDouble("latitude_subpref"));
        s.setLongitudeSubpref(rs.getDouble("longitude_subpref"));
        s.setQtAlagamento(rs.getInt("qt_alagamento"));
        return s;
    }
}
