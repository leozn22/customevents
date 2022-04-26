package br.com.goup.snkcustomevents.domain.xml;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DadosFin {
    private Timestamp dtVenc;
    private BigDecimal vlrDesdob;
    private String chaveAcesso;
    private String chaveAcessoReferencia;

    public DadosFin() {
    }

    public Timestamp getDtVenc() {
        return dtVenc;
    }

    public void setDtVenc(Timestamp dtVenc) {
        this.dtVenc = dtVenc;
    }

    public BigDecimal getVlrDesdob() {
        return vlrDesdob;
    }

    public void setVlrDesdob(BigDecimal vlrDesdob) {
        this.vlrDesdob = vlrDesdob;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public void setChaveAcesso(String chaveAcesso) {
        this.chaveAcesso = chaveAcesso;
    }

    public String getChaveAcessoReferencia() {
        return chaveAcessoReferencia;
    }

    public void setChaveAcessoReferencia(String chaveAcessoReferencia) {
        this.chaveAcessoReferencia = chaveAcessoReferencia;
    }
}
