package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.bo.UsuarioBo;
import br.com.fiap.orbitasafe.dto.UsuarioRespostaDto;
import br.com.fiap.orbitasafe.entities.Usuario;
import br.com.fiap.orbitasafe.exceptions.RegistroNaoEncontradoException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    private UsuarioBo usuarioBo = new UsuarioBo();

    @GET
    public Response listar() throws SQLException, ClassNotFoundException {
        List<Usuario> lista = usuarioBo.listar();
        List<UsuarioRespostaDto> dtos = new ArrayList<UsuarioRespostaDto>();
        for (Usuario u : lista) {
            dtos.add(UsuarioRespostaDto.de(u));
        }
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        Usuario u = usuarioBo.buscarPorId(id);
        if (u == null) {
            throw new RegistroNaoEncontradoException("Usuario nao encontrado: id=" + id);
        }
        return Response.ok(UsuarioRespostaDto.de(u)).build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Usuario usuario)
            throws SQLException, ClassNotFoundException {
        usuario.setIdUsu(id);
        usuarioBo.atualizar(usuario);
        return Response.ok(UsuarioRespostaDto.de(usuario)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletar(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        usuarioBo.deletar(id);
        return Response.noContent().build();
    }
}
