package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.dao.SubprefeituraDao;
import br.com.fiap.orbitasafe.entities.Subprefeitura;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLException;

@Path("/subprefeituras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubprefeituraResource {

    @GET
    public Response listar() throws SQLException, ClassNotFoundException {
        SubprefeituraDao dao = new SubprefeituraDao();
        return Response.ok(dao.selecionar()).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        SubprefeituraDao dao = new SubprefeituraDao();
        Subprefeitura s = dao.buscarPorId(id);
        if (s == null) {
            throw new RegistroNaoEncontradoException("Subprefeitura nao encontrada: id=" + id);
        }
        return Response.ok(s).build();
    }
}
