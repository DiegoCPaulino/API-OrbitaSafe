package br.com.fiap.orbitasafe.entities;

import java.time.LocalDate;

public class Usuario {

    private int idUsu;
    private String nmUsu;
    private String emailUsu;
    private String senhaUsu;
    private String tpUsu;
    private LocalDate dtCadastro;

    public Usuario() {}

    public Usuario(int idUsu, String nmUsu, String emailUsu, String senhaUsu,
                   String tpUsu, LocalDate dtCadastro) {
        this.idUsu      = idUsu;
        this.nmUsu      = nmUsu;
        this.emailUsu   = emailUsu;
        this.senhaUsu   = senhaUsu;
        this.tpUsu      = tpUsu;
        this.dtCadastro = dtCadastro;
    }

    public int getIdUsu() { return idUsu; }
    public void setIdUsu(int idUsu) { this.idUsu = idUsu; }

    public String getNmUsu() { return nmUsu; }
    public void setNmUsu(String nmUsu) { this.nmUsu = nmUsu; }

    public String getEmailUsu() { return emailUsu; }
    public void setEmailUsu(String emailUsu) { this.emailUsu = emailUsu; }

    public String getSenhaUsu() { return senhaUsu; }
    public void setSenhaUsu(String senhaUsu) { this.senhaUsu = senhaUsu; }

    public String getTpUsu() { return tpUsu; }
    public void setTpUsu(String tpUsu) { this.tpUsu = tpUsu; }

    public LocalDate getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDate dtCadastro) { this.dtCadastro = dtCadastro; }

    @Override
    public String toString() {
        return "Usuario" +
                "\nidUsu=" + idUsu +
                "\nnmUsu='" + nmUsu + '\'' +
                "\nemailUsu='" + emailUsu + '\'' +
                "\ntpUsu='" + tpUsu + '\'' +
                "\ndtCadastro=" + dtCadastro;
    }
}
