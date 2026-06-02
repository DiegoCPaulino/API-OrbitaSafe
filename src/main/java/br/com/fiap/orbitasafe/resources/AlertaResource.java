package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.dao.AlertaDao;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLException;

@Path("/regioes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertaResource {

    @GET
    @Path("/{id}/alertas")
    public Response listarPorRegiao(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        AlertaDao dao = new AlertaDao();
        return Response.ok(dao.selecionarPorRegiao(id)).build();
    }
}
