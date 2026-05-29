package br.com.fiap.orbitasafe.main;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Connection conexao = new ConexaoFactory().conexao();

        PreparedStatement stmt = conexao.prepareStatement("SELECT 1 FROM DUAL");
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            System.out.println("Conexao com Oracle estabelecida com sucesso! Resultado: " + rs.getInt(1));
        }

        stmt.close();
        conexao.close();
    }
}
