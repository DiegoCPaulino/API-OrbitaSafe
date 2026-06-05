package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.bo.RegiaoBo;
import br.com.fiap.orbitasafe.bo.UsuarioBo;
import br.com.fiap.orbitasafe.dao.NotificacaoDao;
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
    private RegiaoBo regiaoBo = new RegiaoBo();

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

    // Sub-recursos de usuario (regioes, notificacoes) ficam aqui para evitar ambiguidade de @Path entre Resources.

    @GET
    @Path("/{id}/regioes")
    public Response listarRegioes(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        if (usuarioBo.buscarPorId(id) == null) {
            throw new RegistroNaoEncontradoException("Usuario nao encontrado: id=" + id);
        }
        return Response.ok(regiaoBo.listarPorUsuario(id)).build();
    }

    @GET
    @Path("/{id}/notificacoes")
    public Response listarNotificacoes(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        if (usuarioBo.buscarPorId(id) == null) {
            throw new RegistroNaoEncontradoException("Usuario nao encontrado: id=" + id);
        }
        return Response.ok(new NotificacaoDao().selecionarPorUsuario(id)).build();
    }

    @GET
    @Path("/{id}/notificacoes/nao-lidas")
    public Response listarNotificacoesNaoLidas(@PathParam("id") int id)
            throws SQLException, ClassNotFoundException {
        if (usuarioBo.buscarPorId(id) == null) {
            throw new RegistroNaoEncontradoException("Usuario nao encontrado: id=" + id);
        }
        return Response.ok(new NotificacaoDao().selecionarNaoLidasPorUsuario(id)).build();
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
