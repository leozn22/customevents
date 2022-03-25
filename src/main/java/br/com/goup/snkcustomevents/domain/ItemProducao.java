package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ItemProducao {

    private int codigoPlanta;

    private BigDecimal codigoProduto;

    private int tamanhoLote;

    private int numeroGrade;

    private List<ViewProducaoSaldoItem> listaSaldoItem;

    public ItemProducao() {
        listaSaldoItem = new ArrayList<>();
    }

    public Integer getCodigoPlanta() {
        return codigoPlanta;
    }

    public void setCodigoPlanta(int codigoPlanta) {
        this.codigoPlanta = codigoPlanta;
    }

    public BigDecimal getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(BigDecimal codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public Integer getTamanhoLote() {
        return tamanhoLote;
    }

    public void setTamanhoLote(int tamanhoLote) {
        this.tamanhoLote = tamanhoLote;
    }

    public int getNumeroGrade() {
        return numeroGrade;
    }

    public void setNumeroGrade(int numeroGrade) {
        this.numeroGrade = numeroGrade;
    }

    public List<ViewProducaoSaldoItem> getListaSaldoItem() {
        return listaSaldoItem;
    }
}
