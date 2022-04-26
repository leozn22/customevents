package br.com.goup.snkcustomevents.domain.xml;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class FinanceiroXmlCompraCte {
    private int desdob;
    private String dup;
    private Timestamp dtVenc;
    private BigDecimal valor;

    public FinanceiroXmlCompraCte() {
    }

    public int getDesdob() {
        return desdob;
    }

    public void setDesdob(int desdob) {
        this.desdob = desdob;
    }

    public String getDup() {
        return dup;
    }

    public void setDup(String dup) {
        this.dup = dup;
    }

    public Timestamp getDtVenc() {
        return dtVenc;
    }

    public void setDtVenc(Timestamp dtVenc) {
        this.dtVenc = dtVenc;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
