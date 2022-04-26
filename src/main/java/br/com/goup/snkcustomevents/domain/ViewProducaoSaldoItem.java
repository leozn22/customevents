package br.com.goup.snkcustomevents.domain;

import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;

public class ViewProducaoSaldoItem {

    private BigDecimal codigoEmpresa;

    private String nomeProduto;

    private BigDecimal tzaCodProd;

    private String tzaNuItem;

    private BigDecimal tzaNumNota;

    private String tzaStatusItem;

    private String tzaStatusPg;

    private BigDecimal codigoPlanta;

    private BigDecimal quantidade;

    private BigDecimal numeroGrade;

    public ViewProducaoSaldoItem() {

    }

    public ViewProducaoSaldoItem(Registro registro) {
        this.codigoEmpresa = new BigDecimal(registro.getCampo("CODEMP").toString());
        this.nomeProduto = registro.getCampo("NOMEPROD").toString();
        this.tzaCodProd =  new BigDecimal(registro.getCampo("TZACODPROD").toString());
        this.tzaNuItem = registro.getCampo("TZANUITEM").toString();
        this.tzaNumNota =  new BigDecimal(registro.getCampo("TZANUMNOTA").toString());
        this.tzaStatusItem = registro.getCampo("TZASTATUSITEM").toString();
        this.tzaStatusPg = registro.getCampo("TZASTATUSPG").toString();
        this.codigoPlanta =  new BigDecimal(registro.getCampo("CODIGOPLAN").toString());
        this.quantidade =  new BigDecimal(registro.getCampo("QTDNEG").toString());
    }

    public ViewProducaoSaldoItem(QueryExecutor qry) throws Exception {
        this.codigoEmpresa = qry.getBigDecimal("CODEMP");
        this.nomeProduto = qry.getString("NOMEPROD");
        this.tzaCodProd = qry.getBigDecimal("TZACODPROD");
        this.tzaNuItem = qry.getString("TZANUITEM");
        this.tzaNumNota = qry.getBigDecimal("TZANUMNOTA");
        this.tzaStatusItem = qry.getString("TZASTATUSITEM");
        this.tzaStatusPg = qry.getString("TZASTATUSPG");
        this.codigoPlanta = qry.getBigDecimal("CODIGOPLAN");
        this.quantidade = qry.getBigDecimal("QTDNEG");
        this.numeroGrade = qry.getBigDecimal("CODGRADE");
    }

    public BigDecimal getCodigoEmpresa() {
        return codigoEmpresa;
    }

    public void setCodigoEmpresa(BigDecimal codigoEmpresa) {
        this.codigoEmpresa = codigoEmpresa;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public BigDecimal getTzaCodProd() {
        return tzaCodProd;
    }

    public void setTzaCodProd(BigDecimal tzaCodProd) {
        this.tzaCodProd = tzaCodProd;
    }

    public String getTzaNuItem() {
        return tzaNuItem;
    }

    public void setTzaNuItem(String tzaNuItem) {
        this.tzaNuItem = tzaNuItem;
    }

    public BigDecimal getTzaNumNota() {
        return tzaNumNota;
    }

    public void setTzaNumNota(BigDecimal tzaNumNota) {
        this.tzaNumNota = tzaNumNota;
    }

    public String getTzaStatusItem() {
        return tzaStatusItem;
    }

    public void setTzaStatusItem(String tzaStatusItem) {
        this.tzaStatusItem = tzaStatusItem;
    }

    public String getTzaStatusPg() {
        return tzaStatusPg;
    }

    public void setTzaStatusPg(String tzaStatusPg) {
        this.tzaStatusPg = tzaStatusPg;
    }

    public BigDecimal getCodigoPlanta() {
        return codigoPlanta;
    }

    public void setCodigoPlanta(BigDecimal codigoPlanta) {
        this.codigoPlanta = codigoPlanta;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getNumeroGrade() {
        return numeroGrade;
    }

    public void setNumeroGrade(BigDecimal numeroGrade) {
        this.numeroGrade = numeroGrade;
    }
}
