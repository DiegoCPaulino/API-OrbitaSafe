package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.dao.NotificacaoDao;
import br.com.fiap.orbitasafe.entities.Notificacao;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLException;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificacaoResource {

    @GET
    @Path("/usuarios/{id}/notificacoes")
    public Response listarPorUsuario(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        return Response.ok(new NotificacaoDao().selecionarPorUsuario(id)).build();
    }

    @GET
    @Path("/usuarios/{id}/notificacoes/nao-lidas")
    public Response listarNaoLidasPorUsuario(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        return Response.ok(new NotificacaoDao().selecionarNaoLidasPorUsuario(id)).build();
    }

    @PUT
    @Path("/notificacoes/{id}/marcar-lida")
    public Response marcarLida(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        Notificacao n = new Notificacao();
        n.setIdNotif(id);
        n.setEstadoNotif("LIDA");
        int linhasAfetadas = new NotificacaoDao().atualizar(n);
        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Notificacao nao encontrada: " + id);
        }
        return Response.ok().build();
    }
}
