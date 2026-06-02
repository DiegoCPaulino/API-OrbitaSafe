package br.com.fiap.orbitasafe.dto;

import br.com.fiap.orbitasafe.entities.Usuario;
import java.time.LocalDate;

public class UsuarioRespostaDto {

    private int idUsu;
    private String nmUsu;
    private String emailUsu;
    private String tpUsu;
    private LocalDate dtCadastro;

    public UsuarioRespostaDto() {}

    public UsuarioRespostaDto(int idUsu, String nmUsu, String emailUsu,
                              String tpUsu, LocalDate dtCadastro) {
        this.idUsu      = idUsu;
        this.nmUsu      = nmUsu;
        this.emailUsu   = emailUsu;
        this.tpUsu      = tpUsu;
        this.dtCadastro = dtCadastro;
    }

    // Converte Usuario → DTO sem expor senhaUsu
    public static UsuarioRespostaDto de(Usuario u) {
        return new UsuarioRespostaDto(
                u.getIdUsu(), u.getNmUsu(), u.getEmailUsu(),
                u.getTpUsu(), u.getDtCadastro()
        );
    }

    public int getIdUsu() { return idUsu; }
    public void setIdUsu(int idUsu) { this.idUsu = idUsu; }

    public String getNmUsu() { return nmUsu; }
    public void setNmUsu(String nmUsu) { this.nmUsu = nmUsu; }

    public String getEmailUsu() { return emailUsu; }
    public void setEmailUsu(String emailUsu) { this.emailUsu = emailUsu; }

    public String getTpUsu() { return tpUsu; }
    public void setTpUsu(String tpUsu) { this.tpUsu = tpUsu; }

    public LocalDate getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDate dtCadastro) { this.dtCadastro = dtCadastro; }

    @Override
    public String toString() {
        return "UsuarioRespostaDto" +
                "\nidUsu=" + idUsu +
                "\nnmUsu='" + nmUsu + '\'' +
                "\nemailUsu='" + emailUsu + '\'' +
                "\ntpUsu='" + tpUsu + '\'' +
                "\ndtCadastro=" + dtCadastro;
    }
}
