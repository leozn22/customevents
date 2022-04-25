package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RetornoLancamentoOrdemProducao {
    private boolean sucesso;

    private String msg;

    private List<String> listaNumeroOrdem;

    private BigDecimal nulop;

    public RetornoLancamentoOrdemProducao() {
        this.sucesso = true;
        listaNumeroOrdem = new ArrayList<>();
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

    public List<String> getListaNumeroOrdem() {
        return listaNumeroOrdem;
    }

    public void setListaNumeroOrdem(List<String> listaNumeroOrdem) {
        this.listaNumeroOrdem = listaNumeroOrdem;
    }

    public BigDecimal getNulop() {
        return nulop;
    }

    public void setNulop(BigDecimal nulop) {
        this.nulop = nulop;
    }
}
