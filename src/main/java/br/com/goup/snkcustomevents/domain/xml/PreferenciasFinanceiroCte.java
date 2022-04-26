package br.com.goup.snkcustomevents.domain.xml;

import com.sankhya.util.StringUtils;
import com.sankhya.util.XMLUtils;
import org.jdom.Element;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PreferenciasFinanceiroCte {
    private BigDecimal codTop;
    private BigDecimal codBanco;
    private BigDecimal codNatureza;
    private BigDecimal codTipTit;
    private BigDecimal codCentroResultado;
    private BigDecimal codContaBancaria;
    private BigDecimal codProjeto;
    private Timestamp data;
    private String obtencaoCFOP;
    private boolean importaCabCTeTerc;
    private String tipoImportacaoCabCTeTerc;
    private BigDecimal codTipOperCabCTeTerc;
    private BigDecimal codTipNegCabCTeTerc;
    private BigDecimal codServCabCTeTerc;
    private String tipoBuscaPedFrete;
    private String codCenCusCabCTe;
    private String codNatCabCTe;
    private String codProjCabCTe;
    private boolean copiaRateioPedFrete;
    private boolean copiaCompradorPedFrete;
    private boolean copiaCotacaoPedFrete;
    private boolean copiaObservacaoPedFrete;
    private boolean copiaObsItemPedFrete;

    public BigDecimal getCodTop() {
        return codTop;
    }

    public void setCodTop(BigDecimal codTop) {
        this.codTop = codTop;
    }

    public BigDecimal getCodBanco() {
        return codBanco;
    }

    public void setCodBanco(BigDecimal codBanco) {
        this.codBanco = codBanco;
    }

    public BigDecimal getCodNatureza() {
        return codNatureza;
    }

    public void setCodNatureza(BigDecimal codNatureza) {
        this.codNatureza = codNatureza;
    }

    public BigDecimal getCodTipTit() {
        return codTipTit;
    }

    public void setCodTipTit(BigDecimal codTipTit) {
        this.codTipTit = codTipTit;
    }

    public BigDecimal getCodCentroResultado() {
        return codCentroResultado;
    }

    public void setCodCentroResultado(BigDecimal codCentroResultado) {
        this.codCentroResultado = codCentroResultado;
    }

    public BigDecimal getCodContaBancaria() {
        return codContaBancaria;
    }

    public void setCodContaBancaria(BigDecimal codContaBancaria) {
        this.codContaBancaria = codContaBancaria;
    }

    public BigDecimal getCodProjeto() {
        return codProjeto;
    }

    public void setCodProjeto(BigDecimal codProjeto) {
        this.codProjeto = codProjeto;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public String getObtencaoCFOP() {
        return obtencaoCFOP;
    }

    public void setObtencaoCFOP(String obtencaoCFOP) {
        this.obtencaoCFOP = obtencaoCFOP;
    }

    public boolean isImportaCabCTeTerc() {
        return importaCabCTeTerc;
    }

    public void setImportaCabCTeTerc(boolean importaCabCTeTerc) {
        this.importaCabCTeTerc = importaCabCTeTerc;
    }

    public String getTipoImportacaoCabCTeTerc() {
        return tipoImportacaoCabCTeTerc;
    }

    public void setTipoImportacaoCabCTeTerc(String tipoImportacaoCabCTeTerc) {
        this.tipoImportacaoCabCTeTerc = tipoImportacaoCabCTeTerc;
    }

    public BigDecimal getCodTipOperCabCTeTerc() {
        return codTipOperCabCTeTerc;
    }

    public void setCodTipOperCabCTeTerc(BigDecimal codTipOperCabCTeTerc) {
        this.codTipOperCabCTeTerc = codTipOperCabCTeTerc;
    }

    public BigDecimal getCodTipNegCabCTeTerc() {
        return codTipNegCabCTeTerc;
    }

    public void setCodTipNegCabCTeTerc(BigDecimal codTipNegCabCTeTerc) {
        this.codTipNegCabCTeTerc = codTipNegCabCTeTerc;
    }

    public BigDecimal getCodServCabCTeTerc() {
        return codServCabCTeTerc;
    }

    public void setCodServCabCTeTerc(BigDecimal codServCabCTeTerc) {
        this.codServCabCTeTerc = codServCabCTeTerc;
    }

    public String getTipoBuscaPedFrete() {
        return tipoBuscaPedFrete;
    }

    public void setTipoBuscaPedFrete(String tipoBuscaPedFrete) {
        this.tipoBuscaPedFrete = tipoBuscaPedFrete;
    }

    public String getCodCenCusCabCTe() {
        return codCenCusCabCTe;
    }

    public void setCodCenCusCabCTe(String codCenCusCabCTe) {
        this.codCenCusCabCTe = codCenCusCabCTe;
    }

    public String getCodNatCabCTe() {
        return codNatCabCTe;
    }

    public void setCodNatCabCTe(String codNatCabCTe) {
        this.codNatCabCTe = codNatCabCTe;
    }

    public String getCodProjCabCTe() {
        return codProjCabCTe;
    }

    public void setCodProjCabCTe(String codProjCabCTe) {
        this.codProjCabCTe = codProjCabCTe;
    }

    public boolean isCopiaRateioPedFrete() {
        return copiaRateioPedFrete;
    }

    public void setCopiaRateioPedFrete(boolean copiaRateioPedFrete) {
        this.copiaRateioPedFrete = copiaRateioPedFrete;
    }

    public boolean isCopiaCompradorPedFrete() {
        return copiaCompradorPedFrete;
    }

    public void setCopiaCompradorPedFrete(boolean copiaCompradorPedFrete) {
        this.copiaCompradorPedFrete = copiaCompradorPedFrete;
    }

    public boolean isCopiaCotacaoPedFrete() {
        return copiaCotacaoPedFrete;
    }

    public void setCopiaCotacaoPedFrete(boolean copiaCotacaoPedFrete) {
        this.copiaCotacaoPedFrete = copiaCotacaoPedFrete;
    }

    public boolean isCopiaObservacaoPedFrete() {
        return copiaObservacaoPedFrete;
    }

    public void setCopiaObservacaoPedFrete(boolean copiaObservacaoPedFrete) {
        this.copiaObservacaoPedFrete = copiaObservacaoPedFrete;
    }

    public boolean isCopiaObsItemPedFrete() {
        return copiaObsItemPedFrete;
    }

    public void setCopiaObsItemPedFrete(boolean copiaObsItemPedFrete) {
        this.copiaObsItemPedFrete = copiaObsItemPedFrete;
    }

    public void setTipoBuscaPedidoFrete(String tipoBuscaPedFrete) {
        this.tipoBuscaPedFrete = tipoBuscaPedFrete;
    }
}
