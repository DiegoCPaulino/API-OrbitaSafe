package br.com.fiap.orbitasafe;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isBlank()) {
            frontendUrl = "http://localhost:5173";
        }
        responseContext.getHeaders().add(
                "Access-Control-Allow-Origin", frontendUrl
        );
        responseContext.getHeaders().add(
                "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD"
        );
        responseContext.getHeaders().add(
                "Access-Control-Allow-Headers", "origin, content-type, accept, authorization"
        );
    }
}
