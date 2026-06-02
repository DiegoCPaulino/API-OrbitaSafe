package br.com.fiap.orbitasafe.resources;

import br.com.fiap.orbitasafe.bo.UsuarioBo;
import br.com.fiap.orbitasafe.dto.UsuarioRespostaDto;
import br.com.fiap.orbitasafe.entities.Usuario;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.sql.SQLException;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private UsuarioBo usuarioBo = new UsuarioBo();

    @POST
    @Path("/cadastro")
    public Response cadastrar(Usuario usuario, @Context UriInfo uriInfo)
            throws SQLException, ClassNotFoundException {
        usuarioBo.cadastrar(usuario);
        URI uri = uriInfo.getBaseUriBuilder()
                .path("usuarios")
                .path(String.valueOf(usuario.getIdUsu()))
                .build();
        return Response.created(uri).entity(UsuarioRespostaDto.de(usuario)).build();
    }

    @POST
    @Path("/login")
    public Response login(Usuario credenciais)
            throws SQLException, ClassNotFoundException {
        // login() lança CredenciaisInvalidasException se email/senha inválidos
        Usuario usuario = usuarioBo.login(credenciais.getEmailUsu(), credenciais.getSenhaUsu());
        return Response.ok(UsuarioRespostaDto.de(usuario)).build();
    }
}
