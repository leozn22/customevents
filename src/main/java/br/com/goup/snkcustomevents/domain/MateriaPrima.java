package br.com.goup.snkcustomevents.domain;

import com.sankhya.util.XMLUtils;
import org.jdom.Element;

public class MateriaPrima {
    private String codProdMP;
    private String descricaoProduto;
    private String controleMP;
    private String referencia;
    private String necessidade;
    private String estoque;
    private String qtdsubs;
    private String estsuficiente;
    private String decqtd;
    private boolean existempsub;
    private String saldo;
    private String unidade;
    private String codparc;
    private String razaosocial;

    public MateriaPrima() {

    }

    public MateriaPrima(Element element) {
        this.codProdMP = XMLUtils.getAttributeAsString(element, "CODPRODMP");
        this.descricaoProduto = XMLUtils.getAttributeAsString(element, "DESCRPROD");
        this.controleMP = XMLUtils.getAttributeAsString(element, "CONTROLEMP");
        this.referencia = XMLUtils.getAttributeAsString(element, "REFERENCIA");
        this.necessidade = XMLUtils.getAttributeAsString(element, "NECESSIDADE");
        this.estoque = XMLUtils.getAttributeAsString(element, "ESTOQUE");
        this.qtdsubs = XMLUtils.getAttributeAsString(element, "ESTSUFICIENTE");
        this.decqtd = XMLUtils.getAttributeAsString(element, "DECQTD");
        this.existempsub = XMLUtils.getAttributeAsBoolean(element, "EXISTEMPSUB");
        this.saldo = XMLUtils.getAttributeAsString(element, "SALDO");
        this.unidade = XMLUtils.getAttributeAsString(element, "UNIDADE");
        this.codparc = XMLUtils.getAttributeAsString(element, "CODPARC");
        this.razaosocial = XMLUtils.getAttributeAsString(element, "RAZAOSOCIAL");
    }

    public String getCodProdMP() {
        return codProdMP;
    }

    public void setCodProdMP(String codProdMP) {
        this.codProdMP = codProdMP;
    }

    public String getDescricaoProduto() {
        return descricaoProduto;
    }

    public void setDescricaoProduto(String descricaoProduto) {
        this.descricaoProduto = descricaoProduto;
    }

    public String getControleMP() {
        return controleMP;
    }

    public void setControleMP(String controleMP) {
        this.controleMP = controleMP;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getNecessidade() {
        return necessidade;
    }

    public void setNecessidade(String necessidade) {
        this.necessidade = necessidade;
    }

    public String getEstoque() {
        return estoque;
    }

    public void setEstoque(String estoque) {
        this.estoque = estoque;
    }

    public String getQtdsubs() {
        return qtdsubs;
    }

    public void setQtdsubs(String qtdsubs) {
        this.qtdsubs = qtdsubs;
    }

    public String getEstsuficiente() {
        return estsuficiente;
    }

    public void setEstsuficiente(String estsuficiente) {
        this.estsuficiente = estsuficiente;
    }

    public String getDecqtd() {
        return decqtd;
    }

    public void setDecqtd(String decqtd) {
        this.decqtd = decqtd;
    }

    public boolean isExistempsub() {
        return existempsub;
    }

    public void setExistempsub(boolean existempsub) {
        this.existempsub = existempsub;
    }

    public String getSaldo() {
        return saldo;
    }

    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getCodparc() {
        return codparc;
    }

    public void setCodparc(String codparc) {
        this.codparc = codparc;
    }

    public String getRazaosocial() {
        return razaosocial;
    }

    public void setRazaosocial(String razaosocial) {
        this.razaosocial = razaosocial;
    }
}
