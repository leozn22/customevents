package br.com.goup.snkcustomevents.domain.xml;

import java.math.BigDecimal;

public class RetornoGeracaoCTe {

    private boolean sucesso;
    private String msg;
    private BigDecimal numeroNota;

    public RetornoGeracaoCTe(){
        this(false, "");
        this.numeroNota = BigDecimal.ZERO;
    }

    public RetornoGeracaoCTe(boolean sucesso, String msg) {
        this.sucesso = sucesso;
        this.msg = msg;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public BigDecimal getNumeroNota() {
        return numeroNota;
    }

    public void setNumeroNota(BigDecimal numeroNota) {
        this.numeroNota = numeroNota;
    }
}
