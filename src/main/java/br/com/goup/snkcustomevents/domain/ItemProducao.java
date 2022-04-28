package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ItemProducao {

    private int codigoPlanta;

    private BigDecimal codigoProduto;

    private BigDecimal tamanhoLote;

    private BigDecimal tamanhoLoteMetrosQuadrado;

    private List<Integer> listaGrade;

    private BigDecimal idiProc;

    private List<ViewProducaoSaldoItem> listaSaldoItem;

    private int idProcesso;

    public ItemProducao() {
        listaSaldoItem = new ArrayList<>();
        listaGrade = new ArrayList<>();
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

    public BigDecimal getTamanhoLote() {
        return tamanhoLote;
    }

    public void setTamanhoLote(BigDecimal tamanhoLote) {
        this.tamanhoLote = tamanhoLote;
    }

    public List<Integer> getListaGrade() {
        return listaGrade;
    }

    public void setListaGrade(List<Integer> listaNumeroGrade) {
        this.listaGrade = listaNumeroGrade;
    }

    public List<ViewProducaoSaldoItem> getListaSaldoItem() {
        return listaSaldoItem;
    }

    public BigDecimal getIdiProc() {
        return idiProc;
    }

    public void setIdiProc(BigDecimal idiProc) {
        this.idiProc = idiProc;
    }

    public BigDecimal getTamanhoLoteMetrosQuadrado() {
        return tamanhoLoteMetrosQuadrado;
    }

    public void setTamanhoLoteMetrosQuadrado(BigDecimal tamanhoLoteMetrosQuadrado) {
        this.tamanhoLoteMetrosQuadrado = tamanhoLoteMetrosQuadrado;
    }

    public void setListaSaldoItem(List<ViewProducaoSaldoItem> listaSaldoItem) {
        this.listaSaldoItem = listaSaldoItem;
    }

    public int getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(int idProcesso) {
        this.idProcesso = idProcesso;
    }
}
