package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.domain.*;
import br.com.sankhya.dwf.services.ServiceUtils;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public RetornoLancamentoOrdemProducao processoProducaoGrade(Integer codigoGrade, Integer processoZap) throws Exception {
        ItemProducao itemProducao = this.carregarItemProducaoGrade(codigoGrade, processoZap);
        return this.processoProducao(itemProducao);
    }

    public RetornoLancamentoOrdemProducao processoProducao(ItemProducao itemProducao) {
        RetornoLancamentoOrdemProducao retorno;

        try {
            List<ItemProducao> listaItemProducao = new ArrayList<>();
            listaItemProducao.add(itemProducao);
            retorno = this.realizarLancamento(listaItemProducao);
        } catch (Exception e) {
            e.printStackTrace();
            retorno = new RetornoLancamentoOrdemProducao(false, "Erro: " + e.getMessage());
        }

        return retorno;
    }

    public RetornoLancamentoOrdemProducao processoProducaoListaGrade(List<String> listaGrade) {
        RetornoLancamentoOrdemProducao retorno;

        try {
            List<ItemProducao> listaItemProducao = this.carregarItemProducaoGrade(listaGrade);
            retorno = this.realizarLancamento(listaItemProducao);
        } catch (Exception e) {
            e.printStackTrace();
            retorno = new RetornoLancamentoOrdemProducao(false, "Erro: " + e.getMessage());
        }

        return retorno;
    }

    public int quantidadeItensGrade(Integer numeroGrade) {
        try {
            QueryExecutor qryGrade = contextoAcao.getQuery();
            qryGrade.setParam("CODGRADE", numeroGrade);
            qryGrade.nativeSelect("SELECT COUNT(*) QTD FROM AD_TGFFINSAL at2 WHERE CODGRADE = {CODGRADE}");

            if (qryGrade.next()) {
                return qryGrade.getInt("QTD");
            }

            qryGrade.close();

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private RetornoLancamentoOrdemProducao realizarLancamento(List<ItemProducao> listaItemProducao) throws Exception {
        BigDecimal nulop = this.getNuLop();

        for (ItemProducao item: listaItemProducao) {
            item.setIdProcesso(this.buscarIdProcessoProduto(nulop, item));
            this.inserirProdutoHTML5(nulop, item);
            //this.alterarAtributoProduto(nulop, item);
        }

        this.calcularMetrosQuadradosGrade(nulop, listaItemProducao);

        RetornoLancamentoOrdemProducao retorno = this.lancarOrdensDeProducao(nulop);

        if (retorno.getListaNumeroOrdem().size() == 0) {
            contextoAcao.mostraErro("Não foi possível lançar a Ordem de Produção! <br>" + retorno.getMsg());
        }

        this.gravaRetornoLancamento(retorno, nulop, listaItemProducao);
        return retorno;
    }

    private void calcularMetrosQuadradosGrade(BigDecimal nulop, List<ItemProducao> listaItemProducao) {
        List<ProdutoIntermediario> listaProdutoIntermediario = this.carregarListaProdutoAcabadoIntermediario(nulop, listaItemProducao);

        for(ProdutoIntermediario pi: listaProdutoIntermediario) {
            for (ItemProducao item: listaItemProducao) {
                if (this.isProdutoIntermedirio(pi.getCodigoProduto(), item)) {
                    pi.getListaProduto().add(item);
                }
            }
        }

        for(ProdutoIntermediario pi: listaProdutoIntermediario) {
            if ("M".equals(pi.getUnidade()) && pi.getListaProduto().size() > 0) {
                pi.setTamanhoLote(pi.getListaProduto().stream().map(i -> i.getTamanhoLoteMetrosQuadrado()).reduce(BigDecimal.ZERO, BigDecimal::add));
                this.atualizarLoteProdutoIntermediario(nulop, pi);
            }
        }
    }

    private void atualizarLoteProdutoIntermediario(BigDecimal nulop, ProdutoIntermediario pi) {
        QueryExecutor query = contextoAcao.getQuery();
        query.setParam("P_TAMLOTE", pi.getTamanhoLote());
        query.setParam("P_NULOP", nulop.intValue());
        query.setParam("P_SEQOP", pi.getSequenciaOP());
        query.setParam("P_CODPRODPA", pi.getCodigoProduto());

        try {
            query.update("UPDATE TPRPLOP SET TAMLOTE = {P_TAMLOTE} WHERE NULOP = {P_NULOP} and SEQOP = {P_SEQOP} and CODPRODPA = {P_CODPRODPA}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        query.close();
    }

    private boolean isProdutoIntermedirio(BigDecimal codigoPI, ItemProducao item) {

        QueryExecutor query = contextoAcao.getQuery();
        try {
            query.setParam("P_CODPRODPA", item.getCodigoProduto());
            query.setParam("P_IDPROC", item.getIdProcesso());
            query.setParam("P_CODPRODPI", codigoPI);
            query.nativeSelect("SELECT * FROM VPRLPIPRO where CODPRODPA = {P_CODPRODPA} " +
                    "AND IDPROC = {P_IDPROC} and CODPRODPI = {P_CODPRODPI}");

            return query.next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            query.close();
        }
        return false;
    }

    private List<ProdutoIntermediario> carregarListaProdutoAcabadoIntermediario(BigDecimal nulop, List<ItemProducao> listaItemProducao) {
        List<ProdutoIntermediario> retorno = new ArrayList<>();

        List<String> produtos = new ArrayList<>();
        for (ItemProducao item: listaItemProducao) {
            produtos.add(item.getCodigoProduto().toString());
        }

        QueryExecutor query = contextoAcao.getQuery();
        try {
            query.setParam("P_NULOP", nulop);
            query.nativeSelect("SELECT LOP.CODPRODPA, LOP.TAMLOTE, LOP.SEQOP, PROD.UNIDADE " +
                                  "FROM TPRPLOP LOP " +
                                  "INNER JOIN TGFPRO PROD ON LOP.CODPRODPA = PROD.CODPROD " +
                                  "WHERE LOP.NULOP = {P_NULOP} " +
                                  "AND LOP.CODPRODPA NOT IN (" + String.join(",", produtos) + ")");

            if (query.next()) {
                ProdutoIntermediario pi = new ProdutoIntermediario();
                pi.setCodigoProduto(query.getBigDecimal("CODPRODPA"));
                pi.setTamanhoLote(query.getBigDecimal("TAMLOTE"));
                pi.setSequenciaOP(query.getBigDecimal("SEQOP"));
                pi.setUnidade(query.getString("UNIDADE"));

                retorno.add(pi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            query.close();
        }
        return retorno;
    }

    private void gravaRetornoLancamento(RetornoLancamentoOrdemProducao retorno, BigDecimal nulop, List<ItemProducao> listaItemProducao) throws Exception {
        if (retorno.isSucesso()) {
            for (String idiProc : retorno.getListaNumeroOrdem()) {
                carrregarIdiProc(listaItemProducao, idiProc);
            }

            this.atualizarNulop(retorno, nulop);

            for (ItemProducao item: listaItemProducao) {
                this.atualizarClienteSaldo(nulop, item.getIdiProc(), item.getListaSaldoItem());
                this.atualizarTabelaGradeOP(item, item.getIdiProc());
            }
        }
    }

    private void atualizarNulop(RetornoLancamentoOrdemProducao retorno, BigDecimal nulop) throws Exception {
        for (String idiproc: retorno.getListaNumeroOrdem()) {
            QueryExecutor query = contextoAcao.getQuery();
            query.setParam("P_IDIPROC", idiproc);
            query.setParam("P_AD_NULOP", nulop.intValue());

            query.update("UPDATE TPRIPROC SET AD_NULOP = {P_AD_NULOP} WHERE IDIPROC = {P_IDIPROC}");
            query.close();
        }
    }

    private void carrregarIdiProc(List<ItemProducao> listaItemProducao, String idiProc) throws Exception {
        QueryExecutor query = contextoAcao.getQuery();
        try {
            query.setParam("P_IDIPROC", idiProc);
            query.nativeSelect("SELECT * FROM TPRIPA t WHERE IDIPROC = {P_IDIPROC}");

            if(query.next()){
                for (ItemProducao item: listaItemProducao) {
                    if (item.getCodigoProduto().compareTo(query.getBigDecimal("CODPRODPA")) == 0) {
                        item.setIdiProc(new BigDecimal(idiProc));
                        break;
                    }
                }
            }
        } finally {
            query.close();
        }
    }

    private ItemProducao carregarItemProducaoGrade(Integer numeroGrade, Integer processoZap) throws Exception {
        ItemProducao itemProducao = new ItemProducao();

        QueryExecutor qryConfiguracao = contextoAcao.getQuery();
        try {
            qryConfiguracao.setParam("P_NUCONF", processoZap);
            qryConfiguracao.nativeSelect("SELECT * FROM AD_CONFPP WHERE NUCONF = {P_NUCONF}");

            if (qryConfiguracao.next()) {
                try {
                    itemProducao.setCodigoPlanta(qryConfiguracao.getInt("CODPLP"));
                    itemProducao.setCodigoProduto(BigDecimal.valueOf(qryConfiguracao.getInt("CODPROD")));
                    itemProducao.setTamanhoLote(qryConfiguracao.getBigDecimal("TAMLOTE"));
                    itemProducao.getListaGrade().add(numeroGrade);
                } catch (Exception e) {
                    throw new Exception("Falha ao carregar algum campo do processo [" + processoZap + "] !");
                }
            } else {
                throw new Exception("Nao foi possivel carregar a configuração do processo [" + processoZap + "] !");
            }
        } finally {
            qryConfiguracao.close();
        }

        QueryExecutor query = contextoAcao.getQuery();
        try {
            query.setParam("P_CODGRADE", numeroGrade);
            query.nativeSelect("SELECT 0   AS CODEMP, '' AS NOMEPROD, CODPROD AS TZACODPROD, " +
                    "ITEM AS TZANUITEM, NUMNOTA AS TZANUMNOTA, STATUSITEM  AS TZASTATUSITEM, " +
                    "STATUSPG  AS TZASTATUSPG, 5 AS CODIGOPLAN, QTDNEG " +
                    "FROM AD_TGFFINSAL SAL WHERE CODGRADE = {P_CODGRADE}");

            while(query.next()){
                itemProducao.getListaSaldoItem().add(new ViewProducaoSaldoItem(query));
            }
        } finally {
            query.close();
        }

        return itemProducao;
    }

    private List<ItemProducao> carregarItemProducaoGrade(List<String> listaGrade) throws Exception {
        List<ItemProducao> retorno = new ArrayList<>();

        QueryExecutor qryListaProducao = contextoAcao.getQuery();
        try {
            qryListaProducao.nativeSelect("SELECT CODPROD, sum(QTDNEG) TOTAL_QTD, TRUNC((sum(QTDNEG * (LARGURA  * COMPRIMENTO )) / 1000000),2) AREA " +
                    "FROM AD_TGFFINSAL at2 WHERE CODGRADE in (" + String.join(",", listaGrade) + ") " +
                    "GROUP BY CODPROD");

            while(qryListaProducao.next()) {
                try {
                    ItemProducao itemProducao = new ItemProducao();
                    itemProducao.setCodigoProduto(BigDecimal.valueOf(qryListaProducao.getInt("CODPROD")));
                    itemProducao.setCodigoPlanta(5);
                    itemProducao.setTamanhoLote(qryListaProducao.getBigDecimal("TOTAL_QTD"));
                    itemProducao.setTamanhoLoteMetrosQuadrado(qryListaProducao.getBigDecimal("AREA"));

                    QueryExecutor query = contextoAcao.getQuery();
                    try {
                        query.setParam("P_CODPROD", qryListaProducao.getInt("CODPROD"));
                        query.nativeSelect("SELECT 0 AS CODEMP, '' AS NOMEPROD, CODPROD AS TZACODPROD, " +
                                "ITEM AS TZANUITEM, NUMNOTA AS TZANUMNOTA, STATUSITEM  AS TZASTATUSITEM, " +
                                "STATUSPG  AS TZASTATUSPG, 5 AS CODIGOPLAN, QTDNEG, CODGRADE " +
                                "FROM AD_TGFFINSAL SAL " +
                                "WHERE CODGRADE in ( " + String.join(",", listaGrade) + ") " +
                                "AND CODPROD = {P_CODPROD}");

                        while(query.next()){
                            itemProducao.getListaSaldoItem().add(new ViewProducaoSaldoItem(query));

                            if (!itemProducao.getListaGrade().contains(query.getInt("CODGRADE"))) {
                                itemProducao.getListaGrade().add(query.getInt("CODGRADE"));
                            }
                        }
                    } finally {
                        query.close();
                    }

                    retorno.add(itemProducao);
                } catch (Exception e) {
                    throw new Exception("Falha ao carregar a lista de itens da grade [" + String.join(",", listaGrade) + "] !");
                }
            }
        } finally {
            qryListaProducao.close();
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

    private boolean inserirProdutoHTML5(BigDecimal nulop, ItemProducao itemProducao) throws Exception {
        try {
            String json = "{" +
                    " 'params': {" +
                    "    'agruparEmUnicaOP': false," +
                    "    'codplp': " + itemProducao.getCodigoPlanta().toString() + "," +
                    "    'codprod': " + itemProducao.getCodigoProduto().toString() + "," +
                    "    'controle': {}," +
                    "    'idproc': " + itemProducao.getIdProcesso() + "," +
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

    private boolean alterarAtributoProduto(BigDecimal nulop, ItemProducao itemProducao) {
        try {
            String json = "{" +
                    " 'params': {" +
                    "    'nulop': " + nulop + "," +
                    "    'seqop': 1," +
                    "    'codprod': " + itemProducao.getCodigoProduto().toString() + "," +
                    "    'controle': ' '," +
                    "    'atributo': 'TAMLOTE'," +
                    "    'valor': " + itemProducao.getTamanhoLote() +
                    "  }" +
                    "}";
            sctx.setRequestBody(Json2XMLParser.jsonToElement("root", new JsonParser().parse(json).getAsJsonObject()));
            lancamentoOrdemProducaoSP.alterarAtributoProduto(sctx);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
                    for (int i = 0; i < ((Element) element).getContent().size(); i++) {
                        retorno.getListaNumeroOrdem().add(((Content) ((Element) element).getContent().get(i)).getValue());
                    }
                }
            }

            if (retorno.getListaNumeroOrdem().size() == 0) {
                for (Object element: elements) {
                    if ("mensagens".equals(((Element) element).getName())) {
                        retorno.setMsg(((Content) ((Element) element).getContent().get(0)).getValue());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            retorno.setSucesso(false);
            contextoAcao.mostraErro("Falha no processo alterarAtributoProduto. Erro: " + e.getMessage());
        }
        return retorno;
    }

    private void atualizarTabelaGradeOP(ItemProducao item, BigDecimal idiproc) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.execWithTX(() -> {
                for (ViewProducaoSaldoItem itemSaldoItem : item.getListaSaldoItem()) {
                    Registro gradeOP = contextoAcao.novaLinha("AD_GRADEOP");
                    gradeOP.setCampo("IDIPROC", idiproc.intValue());
                    gradeOP.setCampo("CODGRADE", itemSaldoItem.getNumeroGrade());
                    gradeOP.setCampo("TZANUITEM", itemSaldoItem.getTzaNuItem());
                    gradeOP.save();
                }
            });
        } finally {
            JapeSession.close(hnd);
        }
    }

    private void atualizarClienteSaldo(BigDecimal nulop, BigDecimal idiproc, List<ViewProducaoSaldoItem> listaItem) throws Exception {
        QueryExecutor query = contextoAcao.getQuery();
        query.setParam("P_NULOP", nulop.intValue());
        query.setParam("P_IDIPROC", idiproc.intValue());

        for(ViewProducaoSaldoItem item : listaItem) {
            query.setParam("P_ITENS", Integer.valueOf(item.getTzaNuItem()));
            query.update("UPDATE AD_TGFFINSAL SET STATUSPRODUCAO = 'OP CRIADA',  NULOP = {P_NULOP}, IDIPROC = {P_IDIPROC} WHERE ITEM = {P_ITENS}");
        }

        query.close();
    }

    private List<MateriaPrima> listarMateriasPrimas(BigDecimal nulop) {
        List<MateriaPrima> retorno = new ArrayList<>();
        try {
            String json = "{" +
                    " 'params': {" +
                    "    'nulop': " + nulop + "," +
                    "    'apresentarPIs': true" +
                    "  }" +
                    "}";
            sctx.setRequestBody(Json2XMLParser.jsonToElement("root", new JsonParser().parse(json).getAsJsonObject()));
            lancamentoOrdemProducaoSP.getListaExtratoMateriais(sctx);

            for (Object element: sctx.getBodyElement().getContent()) {
                if ("materiais".equals(((Element) element).getName())) {
                    for (int i = 0; i < ((Element) element).getContent().size(); i++) {
                        retorno.add(new MateriaPrima((Element) ((Element) element).getContent().get(i)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retorno;
    }
}
