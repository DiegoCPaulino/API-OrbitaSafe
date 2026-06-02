package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.bo.RegiaoBo;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLException;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegiaoUsuarioResource {

    private RegiaoBo regiaoBo = new RegiaoBo();

    @GET
    @Path("/{id}/regioes")
    public Response listarPorUsuario(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        return Response.ok(regiaoBo.listarPorUsuario(id)).build();
    }
}
