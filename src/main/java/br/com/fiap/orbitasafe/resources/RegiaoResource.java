package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.bo.AnaliseRiscoBo;
import br.com.fiap.orbitasafe.bo.RegiaoBo;
import br.com.fiap.orbitasafe.entities.Alerta;
import br.com.fiap.orbitasafe.entities.Regiao;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.sql.SQLException;

@Path("/regioes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegiaoResource {

    private RegiaoBo regiaoBo = new RegiaoBo();
    private AnaliseRiscoBo analiseRiscoBo = new AnaliseRiscoBo();

    @POST
    public Response cadastrar(Regiao regiao, @Context UriInfo uriInfo) throws Exception {
        regiaoBo.cadastrar(regiao);
        // 1ª análise automática ao cadastrar região (dispara fluxo completo)
        analiseRiscoBo.analisar(regiao.getIdReg());
        URI uri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(regiao.getIdReg()))
                .build();
        return Response.created(uri).entity(regiao).build();
    }

    @GET
    public Response listar() throws SQLException, ClassNotFoundException {
        return Response.ok(regiaoBo.listar()).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        Regiao regiao = regiaoBo.buscarPorId(id);
        if (regiao == null) {
            throw new RegistroNaoEncontradoException("Regiao nao encontrada: id=" + id);
        }
        return Response.ok(regiao).build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Regiao regiao)
            throws SQLException, ClassNotFoundException {
        regiao.setIdReg(id);
        regiaoBo.atualizar(regiao);
        return Response.ok(regiao).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletar(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        regiaoBo.deletar(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/analisar")
    public Response analisar(@PathParam("id") int id) throws Exception {
        Alerta alerta = analiseRiscoBo.analisar(id);
        return Response.ok(alerta).build();
    }
}
