package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Subprefeitura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubprefeituraDao {

    public List<Subprefeitura> selecionar() throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Subprefeitura> lista = new ArrayList<Subprefeitura>();
            stmt = conexao.prepareStatement(
                "select * from tb_subprefeitura order by nm_subpref"
            );
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

    public Subprefeitura buscarPorId(int id) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conexao.prepareStatement(
                "select * from tb_subprefeitura where id_subpref = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            Subprefeitura s = null;
            if (rs.next()) {
                s = mapear(rs);
            }
            return s;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            conexao.close();
        }
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
