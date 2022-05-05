package br.com.goup.snkcustomevents.notas;

import br.com.sankhya.dwf.services.ServiceUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.facades.ImportacaoXMLNotasSP;
import br.com.sankhya.modelcore.facades.ImportacaoXMLNotasSPBean;
import br.com.sankhya.modelcore.facades.ImportacaoXMLNotasSPHome;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.XMLUtils;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;
import org.jdom.Attribute;
import org.jdom.Element;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;

public class ProcessarImportacaoXMLNotaAgendamento implements ScheduledAction, AcaoRotinaJava {

    private ServiceContext sctx;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        try {
            processarXML();
        } catch (Exception e) {
            e.printStackTrace();
            contextoAcao.setMensagemRetorno("Error: " + e.getMessage());
        }
    }

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {
        try {
            processarXML();
        } catch (Exception e) {
            e.printStackTrace();
            scheduledActionContext.info("Error: " + e.getMessage());
        }
    }

    public void processarXML() throws Exception {

        sctx = new ServiceContext(null);
        sctx.setAutentication(AuthenticationInfo.getCurrent());

        SPBeanUtils.setupContext(sctx);

        JdbcWrapper jdbc = null;
        try {
            try {
                EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
                jdbc = dwfEntityFacade.getJdbcWrapper();
                jdbc.openSession();
                NativeSql sqlNotas = new NativeSql(jdbc);

                sqlNotas.appendSql("SELECT NUARQUIVO FROM TGFIXN t \n" +
                        "WHERE STATUS = 0\n" +
                        "AND AD_STATUSIMPORT = 'S'\n" +
                        "ORDER BY NUARQUIVO\n" +
                        "FETCH NEXT 1000 ROWS ONLY");

                ResultSet rsArquivos = sqlNotas.executeQuery();

                while (rsArquivos.next()) {
                    BigDecimal nuArquivo = rsArquivos.getBigDecimal("NUARQUIVO");
                    System.out.println("Acao Agendada - Processar Importação XML Nro. Arquivo: " + nuArquivo);

                    try {
                        Element element = this.gerarXmlImportacao(nuArquivo);
                        sctx.setRequestBody(element);

                        ImportacaoXMLNotasSP importacao = (ImportacaoXMLNotasSP) ServiceUtils.getStatelessFacade(ImportacaoXMLNotasSPHome.JNDI_NAME, ImportacaoXMLNotasSPHome.class);
                        importacao.processarArquivo(sctx);

                        Element retorno = sctx.getBodyElement();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rsArquivos.close();
            } finally {
                JdbcWrapper.closeSession(jdbc);
            }
        } catch (Exception e) {
            RuntimeException re = new RuntimeException(e);
            System.out.println("Erro Exception: " + re);
            throw re;
        }
    }

    private Element gerarXmlImportacao(BigDecimal nuArquivo) {
        Element element = new Element("serviceRequest");
        Element param = new Element("params");
        param.setAttribute(new Attribute("tela","PORTALIMPORTACAOXML"));
        param.setAttribute(new Attribute("multiplosAvisos","false"));
        param.setAttribute(new Attribute("reprocessar","false"));
        param.addContent(new Element("NUARQUIVO").setText(nuArquivo.toString()));
        param.addContent(this.getParamsCte());
        param.addContent(this.getParamsLibDivergencia());
        param.addContent(this.getParamsNFe());
        param.addContent(this.getParamsNFeEmissaoPropria());
        element.addContent(param);
        return element;
    }

    private Element getParamsCte() {
        Element paramsCte = new Element("paramsCte");
        paramsCte.addContent(new Element("codTipOpCte").setText("4102"));
        paramsCte.addContent(new Element("codBancoCte").setText("1"));
        paramsCte.addContent(new Element("codNatCte").setText("2020600"));
        paramsCte.addContent(new Element("codTipTitCte").setText("4"));
        paramsCte.addContent(new Element("codCenterResultCte").setText("4080201"));
        paramsCte.addContent(new Element("tipoDataCte").setText("N"));
        paramsCte.addContent(new Element("obtencaoCFOP").setText("C"));
        paramsCte.addContent(new Element("importaCabCTe").setText("true"));
        paramsCte.addContent(new Element("tipoImportacaoCabCte").setText("C"));
        paramsCte.addContent(new Element("codTipOperCabCTe").setText("2107"));
        paramsCte.addContent(new Element("codTipNegCabCTe").setText("1024"));
        paramsCte.addContent(new Element("codServCabCTe").setText("344"));
        paramsCte.addContent(new Element("codCenCusCabCTe").setText("F"));
        paramsCte.addContent(new Element("codNatCabCTe").setText("F"));
        paramsCte.addContent(new Element("codProjCabCTe").setText("F"));
        paramsCte.addContent(new Element("copiaRateioPedFrete").setText("false"));
        paramsCte.addContent(new Element("copiaCompradorPedFrete").setText("false"));
        paramsCte.addContent(new Element("copiaCotacaoPedFrete").setText("false"));
        paramsCte.addContent(new Element("copiaObsPedFrete").setText("false"));
        paramsCte.addContent(new Element("copiaObsItemPedFrete").setText("false"));
        paramsCte.addContent(new Element("exigeVinculoNota").setText("N"));
        paramsCte.addContent(new Element("exigePedFrete").setText("false"));
        paramsCte.addContent(new Element("vinculaNotaNaoEletronica").setText("false"));
        paramsCte.addContent(new Element("criterioRateio").setText("P"));
        paramsCte.addContent(new Element("exigeCteComplementado").setText("false"));
        paramsCte.addContent(new Element("tolerancia").setText("0,00"));
        paramsCte.addContent(new Element("vincularCTeComplOrigem").setText("false"));
        paramsCte.addContent(new Element("codTipOpCteEmisPropria").setText("3201"));
        paramsCte.addContent(new Element("codTipNegEmisPropria").setText("1019"));
        paramsCte.addContent(new Element("codServCTe").setText("333"));
        paramsCte.addContent(new Element("importarCTeDocAnterior").setText("false"));
        return paramsCte;
    }

    private Element getParamsLibDivergencia() {
        Element paramsLibDivergencia = new Element("paramsLibDivergencia");
        paramsLibDivergencia.addContent(new Element("exigeLiberacaoImposto").setText("N"));
        paramsLibDivergencia.addContent(new Element("exigeConverterOrig").setText("N"));
        paramsLibDivergencia.addContent(new Element("tipoLiberacaoCadProduto").setText("NN"));
        return paramsLibDivergencia;
    }

    private Element getParamsNFe() {
        Element paramsNFe = new Element("paramsNFe");
        paramsNFe.addContent(new Element("atualizarCodAnvisa").setText("N"));
        paramsNFe.addContent(new Element("atualizarInfoCombustivel").setText("N"));
        paramsNFe.addContent(new Element("usarTributacaoSistema").setText("N"));
        paramsNFe.addContent(new Element("converterCSTParaCSTAnt").setText("N"));
        paramsNFe.addContent(new Element("desconsiderarValidacao").setText("N"));
        paramsNFe.addContent(new Element("nuNotaModeloProdutorRural").setText(""));
        paramsNFe.addContent(new Element("usarComoDtFaturamento").setText("ES"));
        return paramsNFe;
    }

    private Element getParamsNFeEmissaoPropria() {
        Element paramsNFeEmissaoPropria = new Element("paramsNFeEmissaoPropria");
        paramsNFeEmissaoPropria.addContent(new Element("codTipOpVenda").setText("3219"));
        paramsNFeEmissaoPropria.addContent(new Element("codTipOpDevVenda").setText(""));
        paramsNFeEmissaoPropria.addContent(new Element("codTipOpDevCompra").setText(""));
        paramsNFeEmissaoPropria.addContent(new Element("codTipOpComplemento").setText(""));
        paramsNFeEmissaoPropria.addContent(new Element("codTipOpAjuste").setText(""));
        paramsNFeEmissaoPropria.addContent(new Element("codTipNeg").setText("9998"));
        paramsNFeEmissaoPropria.addContent(new Element("codNat").setText("1010300"));
        paramsNFeEmissaoPropria.addContent(new Element("codCCus").setText("3020401"));
        paramsNFeEmissaoPropria.addContent(new Element("codProj").setText(""));
        paramsNFeEmissaoPropria.addContent(new Element("codTipOpCompra").setText(""));
        paramsNFeEmissaoPropria.addContent(new Element("chkvalidaCadastroParceiroNaImportacaoXML").setText("N"));
        paramsNFeEmissaoPropria.addContent(new Element("chkImportarDadosDoInterm").setText("S"));
        paramsNFeEmissaoPropria.addContent(new Element("qtdDiasDtExtemporanea").setText("0"));
        return paramsNFeEmissaoPropria;
    }
}
