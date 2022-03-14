package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.domain.ItemProducao;
import br.com.goup.snkcustomevents.domain.RetornoLancamentoOrdemProducao;
import br.com.goup.snkcustomevents.domain.ViewProducaoSaldoItem;
import br.com.sankhya.dwf.services.ServiceUtils;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.mgeprod.model.services.LancamentoOrdemProducaoSP;
import br.com.sankhya.mgeprod.model.services.LancamentoOrdemProducaoSPHome;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;
import br.com.sankhya.ws.transformer.json.Json2XMLParser;
import com.google.gson.JsonParser;
import com.sankhya.util.XMLUtils;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;

import java.math.BigDecimal;
import java.util.List;

public class OrdemProducao {

    private final ContextoAcao contextoAcao;
    private final ServiceContext sctx;
    private final LancamentoOrdemProducaoSP lancamentoOrdemProducaoSP;

    public OrdemProducao(ContextoAcao contextoAcao) throws Exception {
        this.contextoAcao = contextoAcao;

        sctx = new ServiceContext(null);
        sctx.setAutentication(AuthenticationInfo.getCurrent());

        SPBeanUtils.setupContext(sctx);
        lancamentoOrdemProducaoSP = (LancamentoOrdemProducaoSP) ServiceUtils.getStatelessFacade(LancamentoOrdemProducaoSPHome.JNDI_NAME, LancamentoOrdemProducaoSPHome.class);
    }

    public RetornoLancamentoOrdemProducao processoProducao(ItemProducao itemProducao) {
        RetornoLancamentoOrdemProducao retorno = new RetornoLancamentoOrdemProducao();

        BigDecimal nulop;
        try {
            nulop = this.getNuLop();

            int idProcesso = this.buscarIdProcessoProduto(nulop, itemProducao);
            this.inserirProdutoHTML5(nulop, idProcesso, itemProducao);
            this.alterarAtributoProduto(nulop, itemProducao);
            retorno = this.lancarOrdensDeProducao(nulop);

            if (retorno.isSucesso()) {
                this.atualizarClienteSaldo(nulop, itemProducao.getListaSaldoItem());
            }
        } catch (Exception e) {
            e.printStackTrace();

            retorno.setSucesso(false);
            retorno.setMsg("Erro: " + e.getMessage());
        }

        return retorno;
    }

    private BigDecimal getNuLop() throws Exception {
        try {
            Element element = new Element("serviceRequest");
            Element param = new Element("params");
            param.setAttribute(new Attribute("descricao", "DEFAULT"));
            param.setAttribute(new Attribute("reutilizar", "N"));
            element.addContent(param);

            sctx.setRequestBody(element);
            lancamentoOrdemProducaoSP.getNovoLancamentoOP(sctx);

            Element retorno = (Element) sctx.getBodyElement().getContent().get(0);
            return new BigDecimal(retorno.getAttribute("nulop").getValue());
        } catch (Exception e) {
            e.printStackTrace();
            contextoAcao.mostraErro("Falha ao buscar o nulop. Erro: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private int buscarIdProcessoProduto(BigDecimal nulop, ItemProducao itemProducao) throws Exception {
        try {
            Element element = new Element("serviceRequest");
            Element param = new Element("params");
            param.setAttribute(new Attribute("nulop", nulop.toString()));
            param.setAttribute(new Attribute("codprod", itemProducao.getCodigoProduto().toString()));
            param.setAttribute(new Attribute("tipop", "'P'"));
            param.setAttribute(new Attribute("producaoParaTerc", "'N'"));
            param.setAttribute(new Attribute("controle", "' '"));
            param.setAttribute(new Attribute("codplp", itemProducao.getCodigoPlanta().toString()));
            element.addContent(param);

            sctx.setRequestBody(element);
            lancamentoOrdemProducaoSP.getProcessoProdutivoParaProduto(sctx);

            Element retorno = sctx.getBodyElement();
            return XMLUtils.getAttributeAsBigDecimalOrZero(retorno, "idProcesso").intValue();
        } catch (Exception e) {
            e.printStackTrace();
            contextoAcao.mostraErro("Falha ao buscar o IdProcessoProduto. Erro: " + e.getMessage());
        }
        return 0;
    }

    private boolean inserirProdutoHTML5(BigDecimal nulop, Integer idProcesso, ItemProducao itemProducao) throws Exception {
        try {
            String json = "{" +
                    " 'params': {" +
                    "    'agruparEmUnicaOP': false," +
                    "    'codplp': " + itemProducao.getCodigoPlanta().toString() + "," +
                    "    'codprod': " + itemProducao.getCodigoProduto().toString() + "," +
                    "    'controle': {}," +
                    "    'idproc': " + idProcesso + "," +
                    "    'minLote': '0.0'," +
                    "    'multIdeal': '0.0'," +
                    "    'nulop': " + nulop + "," +
                    "    'oldTamLote': '" + itemProducao.getTamanhoLote().toString() + "'," +
                    "    'opDesmonte': 'N'," +
                    "    'tamlote': '" + itemProducao.getTamanhoLote().toString() + "'" +
                    "  }" +
                    "}";
            sctx.setRequestBody(Json2XMLParser.jsonToElement("root", new JsonParser().parse(json).getAsJsonObject()));
            lancamentoOrdemProducaoSP.inserirProdutoHTML5(sctx);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            contextoAcao.mostraErro("Falha no processo inserirProdutoHTML5. Erro: " + e.getMessage());
        }
        return false;
    }

    private boolean alterarAtributoProduto(BigDecimal nulop, ItemProducao itemProducao) throws Exception {
        try {
            String json = "{" +
                    " 'params': {" +
                    "    'nulop': " + nulop + "," +
                    "    'seqop': 1," +
                    "    'codprod': " + itemProducao.getCodigoProduto().toString() + "," +
                    "    'controle': ' '," +
                    "    'atributo': 'TAMLOTE'," +
                    "    'valor': " + itemProducao.getTamanhoLote().toString() +
                    "  }" +
                    "}";
            sctx.setRequestBody(Json2XMLParser.jsonToElement("root", new JsonParser().parse(json).getAsJsonObject()));
            lancamentoOrdemProducaoSP.alterarAtributoProduto(sctx);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            contextoAcao.mostraErro("Falha no processo alterarAtributoProduto. Erro: " + e.getMessage());
        }
        return false;
    }

    private RetornoLancamentoOrdemProducao lancarOrdensDeProducao(BigDecimal nulop) throws Exception {
        RetornoLancamentoOrdemProducao retorno = new RetornoLancamentoOrdemProducao();

        try {
            String json = "{" +
                    " 'params': {" +
                    "    'nulop': " + nulop + "," +
                    "    'ignorarWarnings': 'N'" +
                    "  }" +
                    "}";
            sctx.setRequestBody(Json2XMLParser.jsonToElement("root", new JsonParser().parse(json).getAsJsonObject()));
            lancamentoOrdemProducaoSP.lancarOrdensDeProducao(sctx);

            List elements =  sctx.getBodyElement().getContent();

            retorno.setNulop(nulop);

            for (Object element: elements) {
                if ("ordens".equals(((Element) element).getName())) {
                    retorno.setNumeroOrdem(((Content) ((Element) element).getContent().get(0)).getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            retorno.setSucesso(false);
            contextoAcao.mostraErro("Falha no processo alterarAtributoProduto. Erro: " + e.getMessage());
        }
        return retorno;
    }

    private void atualizarClienteSaldo(BigDecimal nulop, List<ViewProducaoSaldoItem> listaItem) throws Exception {
        QueryExecutor query = contextoAcao.getQuery();
        query.setParam("P_NULOP", nulop.intValue());

        for(ViewProducaoSaldoItem item : listaItem) {
            query.setParam("P_ITENS", Integer.valueOf(item.getTzaNuItem()));
            query.update("UPDATE AD_TGFFINSAL SET NULOP = {P_NULOP} WHERE ITEM = {P_ITENS}");
        }

        query.close();
    }
}