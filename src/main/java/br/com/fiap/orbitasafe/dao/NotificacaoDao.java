package br.com.fiap.orbitasafe.dao;

import br.com.fiap.orbitasafe.conexoes.ConexaoFactory;
import br.com.fiap.orbitasafe.entities.Notificacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacaoDao {

    public String inserir(Notificacao notificacao) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
                "insert into tb_notificacao (id_notif, ds_notif, dt_notif, estado_notif, fk_usuario_id_usu, fk_alerta_id_alerta) " +
                "values (?, ?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, notificacao.getIdNotif());
            stmt.setString(2, notificacao.getDsNotif());
            stmt.setDate(3, Date.valueOf(notificacao.getDtNotif()));
            stmt.setString(4, notificacao.getEstadoNotif());
            stmt.setInt(5, notificacao.getFkUsuarioIdUsu());
            stmt.setInt(6, notificacao.getFkAlertaIdAlerta());
            stmt.execute();
            return "Notificacao registrada com sucesso!";
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public int atualizar(Notificacao notificacao) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        try {
            stmt = conexao.prepareStatement(
                "update tb_notificacao set estado_notif = ? where id_notif = ?"
            );
            stmt.setString(1, notificacao.getEstadoNotif());
            stmt.setInt(2, notificacao.getIdNotif());
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas;
        } finally {
            if (stmt != null) stmt.close();
            conexao.close();
        }
    }

    public List<Notificacao> selecionarPorUsuario(int idUsuario) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Notificacao> lista = new ArrayList<Notificacao>();
            stmt = conexao.prepareStatement(
                "select * from tb_notificacao where fk_usuario_id_usu = ? order by dt_notif desc"
            );
            stmt.setInt(1, idUsuario);
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

    public List<Notificacao> selecionarNaoLidasPorUsuario(int idUsuario) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Notificacao> lista = new ArrayList<Notificacao>();
            stmt = conexao.prepareStatement(
                "select * from tb_notificacao where fk_usuario_id_usu = ? and estado_notif = 'NAO_LIDA' " +
                "order by dt_notif desc"
            );
            stmt.setInt(1, idUsuario);
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

    private Notificacao mapear(ResultSet rs) throws SQLException {
        Notificacao n = new Notificacao();
        n.setIdNotif(rs.getInt("id_notif"));
        n.setDsNotif(rs.getString("ds_notif"));
        n.setDtNotif(rs.getDate("dt_notif").toLocalDate());
        n.setEstadoNotif(rs.getString("estado_notif"));
        n.setFkUsuarioIdUsu(rs.getInt("fk_usuario_id_usu"));
        n.setFkAlertaIdAlerta(rs.getInt("fk_alerta_id_alerta"));
        return n;
    }
}
