package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;

public class SetorAtividade {

    private BigDecimal idiatv;

    private BigDecimal codigoItem;

    private String descricaoAtividade;

    public SetorAtividade() {

    }

    public SetorAtividade(BigDecimal idiatv, BigDecimal codigoItem, String descricaoAtividade) {
        this.idiatv = idiatv;
        this.codigoItem = codigoItem;
        this.descricaoAtividade = descricaoAtividade;
    }

    public BigDecimal getIdiatv() {
        return idiatv;
    }

    public void setIdiatv(BigDecimal idiatv) {
        this.idiatv = idiatv;
    }

    public BigDecimal getCodigoItem() {
        return codigoItem;
    }

    public void setCodigoItem(BigDecimal codigoItem) {
        this.codigoItem = codigoItem;
    }

    public String getDescricaoAtividade() {
        return descricaoAtividade;
    }

    public void setDescricaoAtividade(String descricaoAtividade) {
        this.descricaoAtividade = descricaoAtividade;
    }
}
