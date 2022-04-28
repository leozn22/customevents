package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProdutoIntermediario {

    private BigDecimal codigoProduto;

    private BigDecimal tamanhoLote;

    private List<ItemProducao> listaProduto;

    private BigDecimal sequenciaOP;

    private String unidade;

    public ProdutoIntermediario() {
        this.listaProduto = new ArrayList<>();
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

    public List<ItemProducao> getListaProduto() {
        return listaProduto;
    }

    public BigDecimal getSequenciaOP() {
        return sequenciaOP;
    }

    public void setSequenciaOP(BigDecimal sequenciaOP) {
        this.sequenciaOP = sequenciaOP;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
}
