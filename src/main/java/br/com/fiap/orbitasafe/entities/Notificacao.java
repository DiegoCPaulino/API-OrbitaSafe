package br.com.fiap.orbitasafe.entities;

import java.time.LocalDate;

public class Notificacao {

    private int idNotif;
    private String dsNotif;
    private LocalDate dtNotif;
    private String estadoNotif;
    private int fkUsuarioIdUsu;
    private int fkAlertaIdAlerta;

    public Notificacao() {}

    public Notificacao(int idNotif, String dsNotif, LocalDate dtNotif, String estadoNotif,
                       int fkUsuarioIdUsu, int fkAlertaIdAlerta) {
        this.idNotif        = idNotif;
        this.dsNotif        = dsNotif;
        this.dtNotif        = dtNotif;
        this.estadoNotif    = estadoNotif;
        this.fkUsuarioIdUsu = fkUsuarioIdUsu;
        this.fkAlertaIdAlerta = fkAlertaIdAlerta;
    }

    public int getIdNotif() { return idNotif; }
    public void setIdNotif(int idNotif) { this.idNotif = idNotif; }

    public String getDsNotif() { return dsNotif; }
    public void setDsNotif(String dsNotif) { this.dsNotif = dsNotif; }

    public LocalDate getDtNotif() { return dtNotif; }
    public void setDtNotif(LocalDate dtNotif) { this.dtNotif = dtNotif; }

    public String getEstadoNotif() { return estadoNotif; }
    public void setEstadoNotif(String estadoNotif) { this.estadoNotif = estadoNotif; }

    public int getFkUsuarioIdUsu() { return fkUsuarioIdUsu; }
    public void setFkUsuarioIdUsu(int fkUsuarioIdUsu) { this.fkUsuarioIdUsu = fkUsuarioIdUsu; }

    public int getFkAlertaIdAlerta() { return fkAlertaIdAlerta; }
    public void setFkAlertaIdAlerta(int fkAlertaIdAlerta) { this.fkAlertaIdAlerta = fkAlertaIdAlerta; }

    @Override
    public String toString() {
        return "Notificacao" +
                "\nidNotif=" + idNotif +
                "\ndsNotif='" + dsNotif + '\'' +
                "\ndtNotif=" + dtNotif +
                "\nestadoNotif='" + estadoNotif + '\'' +
                "\nfkUsuarioIdUsu=" + fkUsuarioIdUsu +
                "\nfkAlertaIdAlerta=" + fkAlertaIdAlerta;
    }
}
