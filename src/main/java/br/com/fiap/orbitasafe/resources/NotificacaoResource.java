package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.dao.NotificacaoDao;
import br.com.fiap.orbitasafe.entities.Notificacao;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLException;

// Listagens por usuario migraram para UsuarioResource; aqui resta o PUT de marcar como lida.
@Path("/notificacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificacaoResource {

    @PUT
    @Path("/{id}/marcar-lida")
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
