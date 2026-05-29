package br.com.fiap.orbitasafe.entities;

import java.time.LocalDate;

public class Regiao {

    private int idReg;
    private String nmReg;
    private LocalDate dtCadastro;
    private int fkUsuarioIdUsu;
    private int fkSubprefeituraIdSubpref;

    public Regiao() {}

    public Regiao(int idReg, String nmReg, LocalDate dtCadastro,
                  int fkUsuarioIdUsu, int fkSubprefeituraIdSubpref) {
        this.idReg                    = idReg;
        this.nmReg                    = nmReg;
        this.dtCadastro               = dtCadastro;
        this.fkUsuarioIdUsu           = fkUsuarioIdUsu;
        this.fkSubprefeituraIdSubpref = fkSubprefeituraIdSubpref;
    }

    public int getIdReg() { return idReg; }
    public void setIdReg(int idReg) { this.idReg = idReg; }

    public String getNmReg() { return nmReg; }
    public void setNmReg(String nmReg) { this.nmReg = nmReg; }

    public LocalDate getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDate dtCadastro) { this.dtCadastro = dtCadastro; }

    public int getFkUsuarioIdUsu() { return fkUsuarioIdUsu; }
    public void setFkUsuarioIdUsu(int fkUsuarioIdUsu) { this.fkUsuarioIdUsu = fkUsuarioIdUsu; }

    public int getFkSubprefeituraIdSubpref() { return fkSubprefeituraIdSubpref; }
    public void setFkSubprefeituraIdSubpref(int fkSubprefeituraIdSubpref) {
        this.fkSubprefeituraIdSubpref = fkSubprefeituraIdSubpref;
    }

    @Override
    public String toString() {
        return "Regiao" +
                "\nidReg=" + idReg +
                "\nnmReg='" + nmReg + '\'' +
                "\ndtCadastro=" + dtCadastro +
                "\nfkUsuarioIdUsu=" + fkUsuarioIdUsu +
                "\nfkSubprefeituraIdSubpref=" + fkSubprefeituraIdSubpref;
    }
}
