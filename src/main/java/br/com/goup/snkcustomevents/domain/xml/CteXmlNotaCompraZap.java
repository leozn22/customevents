package br.com.goup.snkcustomevents.domain.xml;

import br.com.sankhya.modelcore.comercial.CteXmlNotaCompra;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import com.sankhya.util.StringUtils;
import com.sankhya.util.XMLUtils;
import org.jdom.Element;

import javax.ejb.FinderException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CteXmlNotaCompraZap extends CteXmlNotaCompra {

    private PreferenciasFinanceiroCte preferenciaCteZap;
    private ImpostosXmlCte impostosCteZap;
    private Element prefFinanceiroElem;
    private Element rootElem;
    private Element ideElem;
    private Element infCTeNorm;
    private String modelo;
    private Map<Integer, FinanceiroXmlCompraCte> financeirosZap;

    public CteXmlNotaCompraZap(Element infElement, Element paramsCte) throws Exception {
        super(infElement, paramsCte);

        this.rootElem = infElement;
        this.ideElem = XMLUtils.SimpleXPath.selectSingleNode(XMLUtils.SimpleXPath.selectSingleNode(infElement, ".//infCte"), ".//ide");
        this.infCTeNorm = XMLUtils.SimpleXPath.selectSingleNode(XMLUtils.SimpleXPath.selectSingleNode(infElement, ".//infCte"), ".//infCTeNorm");
        this.modelo = XMLUtils.getContentChildAsString(this.ideElem, "mod");
        this.prefFinanceiroElem = paramsCte;
        preferenciaCteZap = new PreferenciasFinanceiroCte();
        this.carregarPrefFinanceiroCte(paramsCte);
    }

    public PreferenciasFinanceiroCte getPreferenciaCteZap() {
        return preferenciaCteZap;
    }

    public ImpostosXmlCte getImpostosCteZap() throws Exception {
        if (this.impostosCteZap != null) {
            return this.impostosCteZap;
        } else {
            this.impostosCteZap = new ImpostosXmlCte();
            if (this.rootElem != null) {
                this.buildImpostos(this.impostosCteZap, XMLUtils.SimpleXPath.selectSingleNode(this.rootElem, ".//ICMS"));
            }

            return this.impostosCteZap;
        }
    }

    private void carregarPrefFinanceiroCte(Element prefFinanceiroElem) throws Exception {

        if (prefFinanceiroElem != null) {
            if (this.preferenciaCteZap != null) {
                this.preferenciaCteZap.setCodTop(this.getElementBigDecimal(this.prefFinanceiroElem, "codTipOpCte"));
                this.preferenciaCteZap.setCodBanco(this.getElementBigDecimal(this.prefFinanceiroElem, "codBancoCte"));
                this.preferenciaCteZap.setCodNatureza(this.getElementBigDecimal(this.prefFinanceiroElem, "codNatCte"));
                this.preferenciaCteZap.setCodTipTit(this.getElementBigDecimal(this.prefFinanceiroElem, "codTipTitCte"));
                this.preferenciaCteZap.setCodCentroResultado(this.getElementBigDecimal(this.prefFinanceiroElem, "codCenterResultCte"));
                this.preferenciaCteZap.setCodContaBancaria(this.getElementBigDecimal(this.prefFinanceiroElem, "codBankAccountCte"));
                this.preferenciaCteZap.setCodProjeto(this.getElementBigDecimal(this.prefFinanceiroElem, "codProjectCte"));
                this.preferenciaCteZap.setObtencaoCFOP(this.getElementText(this.prefFinanceiroElem, "obtencaoCFOP"));
                this.preferenciaCteZap.setImportaCabCTeTerc(StringUtils.toBoolean(this.getElementText(this.prefFinanceiroElem, "importaCabCTe")));
                this.preferenciaCteZap.setTipoImportacaoCabCTeTerc(StringUtils.getNullAsEmpty(this.getElementText(this.prefFinanceiroElem, "tipoImportacaoCabCte")));
                this.preferenciaCteZap.setCodTipOperCabCTeTerc(this.getElementBigDecimal(this.prefFinanceiroElem, "codTipOperCabCTe"));
                this.preferenciaCteZap.setCodTipNegCabCTeTerc(this.getElementBigDecimal(this.prefFinanceiroElem, "codTipNegCabCTe"));
                this.preferenciaCteZap.setCodServCabCTeTerc(this.getElementBigDecimal(this.prefFinanceiroElem, "codServCabCTe"));
                this.preferenciaCteZap.setCodCenCusCabCTe(StringUtils.getNullAsEmpty(this.getElementText(this.prefFinanceiroElem, "codCenCusCabCTe")));
                this.preferenciaCteZap.setCodNatCabCTe(StringUtils.getNullAsEmpty(this.getElementText(this.prefFinanceiroElem, "codNatCabCTe")));
                this.preferenciaCteZap.setCodProjCabCTe(StringUtils.getNullAsEmpty(this.getElementText(this.prefFinanceiroElem, "codProjCabCTe")));
                this.preferenciaCteZap.setTipoBuscaPedFrete(StringUtils.getNullAsEmpty(this.getElementText(this.prefFinanceiroElem, "tipoBuscaPedFrete")));
                this.preferenciaCteZap.setCopiaRateioPedFrete(StringUtils.toBoolean(this.getElementText(this.prefFinanceiroElem, "copiaRateioPedFrete")));
                this.preferenciaCteZap.setCopiaCompradorPedFrete(StringUtils.toBoolean(this.getElementText(this.prefFinanceiroElem, "copiaCompradorPedFrete")));
                this.preferenciaCteZap.setCopiaCotacaoPedFrete(StringUtils.toBoolean(this.getElementText(this.prefFinanceiroElem, "copiaCotacaoPedFrete")));
                this.preferenciaCteZap.setCopiaObservacaoPedFrete(StringUtils.toBoolean(this.getElementText(this.prefFinanceiroElem, "copiaObsPedFrete")));
                this.preferenciaCteZap.setCopiaObsItemPedFrete(StringUtils.toBoolean(this.getElementText(this.prefFinanceiroElem, "copiaObsItemPedFrete")));
            }
        }
    }

    private void buildImpostos(ImpostosXmlCte impostosCte, Element icmsElement) throws Exception {
        Element icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS00");
        if (icms2Element != null) {
            this.buildIcmsPadrao(icms2Element, impostosCte);
        } else if (XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS20") != null) {
            icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS20");
            this.buildIcmsPadrao(icms2Element, impostosCte);
        } else if (XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS45") != null) {
            icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS45");
            this.buildIcmsPadrao(icms2Element, impostosCte);
        } else if (XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS60") != null) {
            icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS60");
            impostosCte.codTrib = this.elementToBigDecimalValueOrZero(icmsElement, ".//CST");
            impostosCte.baseIcms = this.elementToBigDecimalValueOrZero(icmsElement, ".//vBCSTRet");
            impostosCte.aliqIcms = this.elementToBigDecimalValueOrZero(icmsElement, ".//pICMSSTRet");
            impostosCte.vlrIcms = this.elementToBigDecimalValueOrZero(icmsElement, ".//vICMSSTRet");
        } else if (XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS90") != null) {
            icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMS90");
            this.buildIcmsPadrao(icms2Element, impostosCte);
        } else if (XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMSOutraUF") != null) {
            icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMSOutraUF");
            impostosCte.codTrib = this.elementToBigDecimalValueOrZero(icmsElement, ".//CST");
            impostosCte.baseIcms = this.elementToBigDecimalValueOrZero(icmsElement, ".//vBCOutraUF");
            impostosCte.aliqIcms = this.elementToBigDecimalValueOrZero(icmsElement, ".//pICMSOutraUF");
            impostosCte.vlrIcms = this.elementToBigDecimalValueOrZero(icmsElement, ".//vICMSOutraUF");
        } else if (XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMSSN") != null) {
            icms2Element = XMLUtils.SimpleXPath.selectSingleNode(icmsElement, ".//ICMSSN");
            impostosCte.codTrib = this.elementToBigDecimalValueOrZero(icms2Element, ".//CST");
        }

        if (this.ideElem != null) {
            impostosCte.cfop = this.getCFOPConvertido(this.getElementBigDecimal(this.ideElem, "CFOP"));
        }
    }

    public Map<Integer, FinanceiroXmlCompraCte> getFinanceirosCte() throws Exception {
        if (this.financeirosZap != null) {
            return this.financeirosZap;
        } else {
            this.financeirosZap = new HashMap();
            Element finanElement = XMLUtils.SimpleXPath.selectSingleNode(this.rootElem, ".//cobr");
            if (finanElement != null) {
                Collection<Element> dupElements = XMLUtils.SimpleXPath.selectNodes(finanElement, ".//dup");
                int i = 0;
                Iterator i$ = dupElements.iterator();

                while(i$.hasNext()) {
                    Element dup = (Element)i$.next();
                    FinanceiroXmlCompraCte financeiro = new FinanceiroXmlCompraCte();
                    financeiro.setDesdob(i + 1);
                    if (MGECoreParameter.getParameterAsBoolean("com.importacao.nfe.usa.numnota.financeiro")) {
                        Element ideElement = XMLUtils.SimpleXPath.selectSingleNode(this.rootElem, ".//ide");

                        financeiro.setDup(this.getElementText(ideElement, "nNF"));
                    } else {
                        financeiro.setDup(this.getElementText(dup, "nDup"));
                    }

                    financeiro.setDtVenc(this.buildTimestamp(this.getElementText(dup, "dVenc")));
                    financeiro.setValor(this.getElementBigDecimal(dup, "vDup"));
                    ++i;
                    this.financeirosZap.put(financeiro.getDesdob(), financeiro);
                }
            }

            return this.financeirosZap;
        }
    }

    private BigDecimal getCFOPConvertido(BigDecimal cfopFromXml) throws Exception {
        String result = cfopFromXml.toString();
        int resto = cfopFromXml.intValue() % 1000;
        String pos1 = Character.toString(result.charAt(0));
        switch(Integer.parseInt(pos1)) {
            case 5:
                result = "1" + resto;
                break;
            case 6:
                result = "2" + resto;
                break;
            case 7:
                result = "3" + resto;
        }

        BigDecimal cfopConvertido = new BigDecimal(result);

        try {
            EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKey("ClassificacaoFiscalOperacao", cfopConvertido);
            return cfopConvertido;
        } catch (FinderException var7) {
            return BigDecimal.ZERO;
        }
    }

    private void buildIcmsPadrao(Element icmsElem, ImpostosXmlCte impostosCte) throws Exception {
        impostosCte.codTrib = this.elementToBigDecimalValueOrZero(icmsElem, ".//CST");
        impostosCte.baseIcms = this.elementToBigDecimalValueOrZero(icmsElem, ".//vBC");
        impostosCte.aliqIcms = this.elementToBigDecimalValueOrZero(icmsElem, ".//pICMS");
        impostosCte.vlrIcms = this.elementToBigDecimalValueOrZero(icmsElem, ".//vICMS");
    }

    protected BigDecimal getElementBigDecimal(Element base, String path) throws Exception {
        String result = XMLUtils.SimpleXPath.getNodeText(base, path);
        return result == null ? null : new BigDecimal(result);
    }

    protected String getElementText(Element base, String path) throws Exception {
        return XMLUtils.SimpleXPath.getNodeText(base, path);
    }
}
