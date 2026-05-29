package br.com.fiap.orbitasafe.conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoFactory {

    public Connection conexao() throws ClassNotFoundException, SQLException {
        String url    = System.getenv("ORACLE_URL");
        String user   = System.getenv("ORACLE_USER");
        String passwd = System.getenv("ORACLE_PASSWORD");

        if (url == null || user == null || passwd == null) {
            throw new RuntimeException(
                "Variaveis de ambiente ORACLE_URL, ORACLE_USER e ORACLE_PASSWORD nao configuradas.");
        }

        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection(url, user, passwd);
    }
}
