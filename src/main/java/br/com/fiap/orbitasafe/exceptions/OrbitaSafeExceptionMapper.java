package br.com.fiap.orbitasafe.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class OrbitaSafeExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException e) {
        if (e instanceof RegistroNaoEncontradoException) {
            return Response.status(404)
                    .entity(Map.of("erro", e.getMessage())).build();
        }
        if (e instanceof EmailJaCadastradoException) {
            return Response.status(409)
                    .entity(Map.of("erro", e.getMessage())).build();
        }
        if (e instanceof CredenciaisInvalidasException) {
            return Response.status(401)
                    .entity(Map.of("erro", e.getMessage())).build();
        }
        if (e instanceof ValidacaoException) {
            return Response.status(400)
                    .entity(Map.of("erro", e.getMessage())).build();
        }
        // Exceções do proprio JAX-RS (ex.: NotFoundException de rota inexistente -> 404,
        // MethodNotAllowedException -> 405). Preserva o status original em vez de virar 500.
        if (e instanceof WebApplicationException wae) {
            int status = wae.getResponse().getStatus();
            String msg = e.getMessage() != null ? e.getMessage() : "HTTP " + status;
            return Response.status(status)
                    .entity(Map.of("erro", msg)).build();
        }
        System.err.println("=== Erro nao tratado capturado pelo ExceptionMapper ===");
        e.printStackTrace();
        return Response.status(500)
                .entity(Map.of("erro", "Erro interno no servidor")).build();
    }
}
