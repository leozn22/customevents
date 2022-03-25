package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;

public class RetornoLancamentoOrdemProducao {
    private boolean sucesso;

    private String msg;

    private String numeroOrdem;

    private BigDecimal nulop;

    public RetornoLancamentoOrdemProducao() {
        this.sucesso = true;
    }

    public RetornoLancamentoOrdemProducao(Boolean sucesso) {
        this.sucesso = sucesso;
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

    public String getNumeroOrdem() {
        return numeroOrdem;
    }

    public void setNumeroOrdem(String numeroOrdem) {
        this.numeroOrdem = numeroOrdem;
    }

    public BigDecimal getNulop() {
        return nulop;
    }

    public void setNulop(BigDecimal nulop) {
        this.nulop = nulop;
    }
}
