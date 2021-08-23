package br.com.goup.snkcustomevents.expedition;

public class PacoteNota {
    private int nuPct = 0;

    private int nuNota = 0;

    private String tipoDocumento = "";

    public int getNuPct() {
        return nuPct;
    }

    public void setNuPct(int nuPct) {
        this.nuPct = nuPct;
    }

    public int getNuNota() {
        return nuNota;
    }

    public void setNuNota(int nuNota) {
        this.nuNota = nuNota;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
}
