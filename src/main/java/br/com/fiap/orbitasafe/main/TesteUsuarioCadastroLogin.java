package br.com.fiap.orbitasafe.main;

import br.com.fiap.orbitasafe.bo.UsuarioBo;
import br.com.fiap.orbitasafe.entities.Usuario;
import br.com.fiap.orbitasafe.exceptions.CredenciaisInvalidasException;

import java.time.LocalDate;

public class TesteUsuarioCadastroLogin {

    public static void main(String[] args) throws Exception {
        UsuarioBo bo = new UsuarioBo();

        Usuario usuario = new Usuario();
        usuario.setIdUsu(999);
        usuario.setNmUsu("Teste Fase4");
        usuario.setEmailUsu("fase4@orbita.com");
        usuario.setSenhaUsu("senha123");
        usuario.setTpUsu("COMUM");
        usuario.setDtCadastro(LocalDate.now());

        System.out.println(bo.cadastrar(usuario));
        System.out.println("Hash gravado: " + usuario.getSenhaUsu());

        Usuario logado = bo.login("fase4@orbita.com", "senha123");
        System.out.println("Login: " + (logado != null ? "OK — " + logado.getNmUsu() : "FALHOU"));

        try {
            bo.login("fase4@orbita.com", "senhaerrada");
            System.out.println("Login com senha errada: OK (inesperado)");
        } catch (CredenciaisInvalidasException e) {
            System.out.println("Login com senha errada: Recusado (esperado)");
        }
    }
}
