package br.com.goup.snkcustomevents.domain.xml;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.nfe.NFeUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.util.troubleshooting.SKError;
import br.com.sankhya.util.troubleshooting.TSLevel;
import br.com.goup.snkcustomevents.Enumerator.CodigoEmpresa;
import com.sankhya.util.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.CharArrayReader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ImportacaoXmlNotaCompraZapHelper {

    private EntityFacade dwfEntityFacade;

    private String tipoCriterioRateio;
    private BigDecimal pesoTotalNotasCTe;
    private BigDecimal vlrTotalNotasCTe;
    private Collection<BigDecimal> notasDoCTe;

    private DynamicVO munIniVO;
    private DynamicVO munFimVO;
    private boolean cfopCalculado;
    private BigDecimal codParceiroXML;
    private BigDecimal codParc;
    private BigDecimal codEmp;
    private BigDecimal nuArquivoImportacao;
    private DynamicVO topVO;
    private Element validacaoCabCTeTerc;
    private AtualizacaoDadosFreteHelper atualizacaoDadosFreteHelper;

    final String xmlConfiguracao = "<paramsCte> " +
            "<codTipOpCte>4102</codTipOpCte> " +
            "<codBancoCte>1</codBancoCte> " +
            "<codNatCte>2020600</codNatCte> " +
            "<codTipTitCte>4</codTipTitCte> " +
            "<codCenterResultCte>4080201</codCenterResultCte> " +
            "<tipoDataCte>N</tipoDataCte> " +
            "<obtencaoCFOP>C</obtencaoCFOP> " +
            "<importaCabCTe>true</importaCabCTe> " +
            "<tipoImportacaoCabCte>C</tipoImportacaoCabCte> " +
            "<codTipOperCabCTe>2107</codTipOperCabCTe> " +
            "<codTipNegCabCTe>1024</codTipNegCabCTe> " +
            "<codServCabCTe>344</codServCabCTe> " +
            "<codCenCusCabCTe>F</codCenCusCabCTe> " +
            "<codNatCabCTe>F</codNatCabCTe> " +
            "<codProjCabCTe>F</codProjCabCTe> " +
            "<copiaRateioPedFrete>false</copiaRateioPedFrete> " +
            "<copiaCompradorPedFrete>false</copiaCompradorPedFrete> " +
            "<copiaCotacaoPedFrete>false</copiaCotacaoPedFrete> " +
            "<copiaObsPedFrete>false</copiaObsPedFrete> " +
            "<copiaObsItemPedFrete>false</copiaObsItemPedFrete> " +
            "<exigeVinculoNota>N</exigeVinculoNota> " +
            "<exigePedFrete>false</exigePedFrete> " +
            "<vinculaNotaNaoEletronica>false</vinculaNotaNaoEletronica> " +
            "<criterioRateio>P</criterioRateio> " +
            "<exigeCteComplementado>false</exigeCteComplementado> " +
            "<tolerancia>0,00</tolerancia> " +
            "<vincularCTeComplOrigem>false</vincularCTeComplOrigem> " +
            "<codTipOpCteEmisPropria>3201</codTipOpCteEmisPropria> " +
            "<codTipNegEmisPropria>1019</codTipNegEmisPropria> " +
            "<codServCTe>333</codServCTe> " +
            "<importarCTeDocAnterior>false</importarCTeDocAnterior> " +
            "</paramsCte>";

    public ImportacaoXmlNotaCompraZapHelper() {
        dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
        this.notasDoCTe = new ArrayList();
        this.pesoTotalNotasCTe = BigDecimal.ZERO;
        this.vlrTotalNotasCTe = BigDecimal.ZERO;
    }

    public RetornoGeracaoCTe gerarNotaCompra(DynamicVO dynamicVO) throws Exception {
        RetornoGeracaoCTe retornoGeracaoCTe = new RetornoGeracaoCTe();
        try {
            FinderWrapper finderEmpresas = new FinderWrapper("Empresa", "this.CGC = ? AND EmpresaFinanceiro->ATIVO = 'S' AND this.CODEMP > 0", new Object[]{ dynamicVO.asString("CNPJREMET") });
            finderEmpresas.setOrderBy("this.CODEMP");

            Collection empresas = this.dwfEntityFacade.findByDynamicFinderAsVO(finderEmpresas);

            if (empresas.size() <= 0) {
                return retornoGeracaoCTe;
            }

            StringBuilder where = new StringBuilder("this.CHAVECTE = ? AND this.TIPMOV = ?");
            Collection<DynamicVO> notasVO = this.dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper("CabecalhoNota", where.toString(), new Object[]{dynamicVO.asString("CHAVEACESSO"), "C"}));
            if (!notasVO.isEmpty()) {
                return retornoGeracaoCTe;
            }

            if (dynamicVO.asClob("XML") == null) {
                retornoGeracaoCTe.setMsg("Não é possível processar esta importação, pois ela não possui XML.");
                return retornoGeracaoCTe;
            }

            Element paramsCte = XMLUtils.stringToElement(xmlConfiguracao);

            SAXBuilder sax = new SAXBuilder();
            Document cteDoc = sax.build(new CharArrayReader(dynamicVO.asClob("XML")));
            Element infElement = cteDoc.getRootElement();

            CteXmlNotaCompraZap xmlNotaCompra = new CteXmlNotaCompraZap(infElement, paramsCte);
            this.topVO = ComercialUtils.getTipoOperacao(xmlNotaCompra.getPreferenciaCteZap().getCodTop());

            Element errosBuscaMunicipios = this.buscaValidaMunicipioOrigDest(cteDoc);

            if (errosBuscaMunicipios != null) {
                retornoGeracaoCTe.setMsg("Erro ao buscar municipio. Detalhe:" + this.getElementText(errosBuscaMunicipios, "msg"));
                return retornoGeracaoCTe;
            }

            DynamicVO empresa = (DynamicVO)empresas.iterator().next();

            this.codEmp = empresa.asBigDecimal("CODEMP");
            this.nuArquivoImportacao = dynamicVO.asBigDecimal("NUARQUIVO");
            this.codParceiroXML = dynamicVO.asBigDecimal("CODPARC");
            this.codParc = this.buscarParceiroArquivo(this.nuArquivoImportacao);

            if (this.codParc.compareTo(BigDecimal.ZERO) == 0) {
                retornoGeracaoCTe.setMsg("Parceiro não localizado para a nota de compra");
                return retornoGeracaoCTe;
            }

            DynamicVO cabVO = criarCabecalhoNota(dynamicVO, xmlNotaCompra);
            retornoGeracaoCTe.setNumeroNota(cabVO.asBigDecimal("NUNOTA"));

            this.incluirFinanceiroCteTerceiros(xmlNotaCompra, cabVO);
            this.vinculaNotaConhecimentoTransporteCTeTerceiros(cabVO, xmlNotaCompra);
            DynamicVO itemVO = this.incluirItensCTeTerceiros(cabVO, xmlNotaCompra);
            this.atualizaImpostosCTeTerceiros(cabVO, itemVO);
            this.ratearValorFreteCTeTerceiros(xmlNotaCompra, cabVO);

        } catch (Exception e){
            retornoGeracaoCTe.setSucesso(false);
            retornoGeracaoCTe.setMsg(e.getMessage());
        }

        return retornoGeracaoCTe;
    }

    private BigDecimal buscarParceiroArquivo(BigDecimal nuArquivo) {
        BigDecimal codigoParceiro = BigDecimal.ZERO;
        JdbcWrapper jdbc = null;
        try {
            try {
                NativeSql sqlNotas = new NativeSql(this.dwfEntityFacade.getJdbcWrapper());

                sqlNotas.appendSql("SELECT par.CODPARC FROM TGFIXN t \n" +
                        "INNER JOIN TSIEMP emp ON t.CODEMP = emp.CODEMP\n" +
                        "INNER JOIN TGFPAR par ON par.CGC_CPF = emp.CGC\n" +
                        "WHERE t.NUARQUIVO = " + nuArquivo + "\n" +
                        "AND par.ATIVO = 'S'\n" +
                        "AND ROWNUM = 1");

                ResultSet rs = sqlNotas.executeQuery();

                while (rs.next()) {
                    codigoParceiro = rs.getBigDecimal("CODPARC");
                }
                rs.close();
            } finally {
                JdbcWrapper.closeSession(jdbc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return codigoParceiro;
    }

    private DynamicVO criarCabecalhoNota(DynamicVO dynamicVO, CteXmlNotaCompraZap xmlNotaCompra) throws Exception {
        DynamicVO cabVO = (DynamicVO)this.dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
        cabVO.setProperty("CODEMP", this.codEmp);
        cabVO.setProperty("CODPARC", this.codParc);
        cabVO.setProperty("CODPARCREMETENTE", dynamicVO.asBigDecimal("CODPARC"));
        cabVO.setProperty("CODPARCDEST", dynamicVO.asBigDecimal("CODPARCDEST"));
        cabVO.setProperty("CODTIPOPER", xmlNotaCompra.getPreferenciaCteZap().getCodTipOperCabCTeTerc());
        cabVO.setProperty("CODTIPVENDA", xmlNotaCompra.getPreferenciaCteZap().getCodTipNegCabCTeTerc());
        cabVO.setProperty("CODCIDINICTE", this.munIniVO.asBigDecimal("CODCID"));
        cabVO.setProperty("CODCIDFIMCTE", this.munFimVO.asBigDecimal("CODCID"));
        cabVO.setProperty("CHAVECTE", xmlNotaCompra.getDiversos().getChaveAcesso());
        cabVO.setProperty("NUMPROTOCCTE", xmlNotaCompra.getDiversos().getNumProtocolo());
        cabVO.setProperty("VLRNOTA", xmlNotaCompra.getDiversos().getVlrDesdobramento());
        cabVO.setProperty("OBSERVACAO", this.getObservacao(xmlNotaCompra.getDiversos().getObservacao()));
        cabVO.setProperty("VLRCARGAAVERB", xmlNotaCompra.getDiversos().getVlrCargaAverb());
        cabVO.setProperty("NUMALEATORIOCTE", xmlNotaCompra.getDiversos().getNumAleatorio());
        cabVO.setProperty("SERIENOTA", xmlNotaCompra.getDiversos().getSerieNota());
        cabVO.setProperty("NUMNOTA", xmlNotaCompra.getDiversos().getNumNota());
        cabVO.setProperty("DTNEG", xmlNotaCompra.getDiversos().getDtNeg());
        cabVO.setProperty("TPEMISCTE", xmlNotaCompra.getDiversos().getTpEmis());
        cabVO.setProperty("TPAMBCTE", xmlNotaCompra.getDiversos().getTpAmb().toString());
        cabVO.setProperty("DHPROTOCCTE", xmlNotaCompra.getDiversos().getDtRecebimento());
        if (xmlNotaCompra.getDiversos().getLotacao() != null) {
            cabVO.setProperty("CTELOTACAO", xmlNotaCompra.getDiversos().getLotacao() ? "S" : "N");
        }

        if (xmlNotaCompra.getDiversos().getCteGlobal() != null) {
            cabVO.setProperty("CTEGLOBAL", xmlNotaCompra.getDiversos().getCteGlobal() ? "S" : "N");
        }

        cabVO.setProperty("DTFATUR", xmlNotaCompra.getDiversos().getDtSaiEnt());
        DynamicVO parcVO = (DynamicVO)this.dwfEntityFacade.findEntityByPrimaryKeyAsVO("Parceiro", dynamicVO.asBigDecimal("CODPARC"));
        cabVO.setProperty("TERMACORDNOTA", parcVO.asString("TERMACORD"));
        cabVO.setProperty("CODNAT", xmlNotaCompra.getPreferenciaCteZap().getCodNatureza());
        cabVO.setProperty("CODCENCUS", xmlNotaCompra.getPreferenciaCteZap().getCodCentroResultado());
        this.dwfEntityFacade.createEntity("CabecalhoNota", (EntityVO)cabVO);
        return cabVO;
    }

    private void ratearValorFreteCTeTerceiros(CteXmlNotaCompraZap cteXmlNotaCompra, DynamicVO cabCteVO) throws Exception {
        if (BigDecimalUtil.isNullOrZero(cteXmlNotaCompra.getDiversos().getVlrDesdobramento())) {
            StringBuilder strBuilder = new StringBuilder("Não foi possível efetuar o rateio, pois não há valor no CT-e!");
            this.addValidacaoCabCTeTerc(strBuilder.toString());
        } else {
            Collection<InfoNF> infoNFs = cteXmlNotaCompra.getInfoNFs();
            if (!infoNFs.isEmpty()) {
                List<String> listaChaveNFeRef = new ArrayList();
                Iterator i$ = infoNFs.iterator();

                while(i$.hasNext()) {
                    InfoNF infoNF = (InfoNF)i$.next();
                    if (StringUtils.isNotEmpty(infoNF.getChave())) {
                        listaChaveNFeRef.add(infoNF.getChave());
                    }
                }

                if (listaChaveNFeRef.isEmpty()) {
                    return;
                }

                FinderWrapper finderCabRefNFe = new FinderWrapper("CabecalhoNota", SQLUtils.buildINClause("this.CHAVENFE", listaChaveNFeRef.size()));
                finderCabRefNFe.setFinderArguments(listaChaveNFeRef.toArray());
                Collection<DynamicVO> cabRefNFeCollection = this.dwfEntityFacade.findByDynamicFinderAsVO(finderCabRefNFe);
                if (cabRefNFeCollection.isEmpty()) {
                    StringBuilder strBuilder = new StringBuilder("Não existe(m) nota(s) para efetuar o rateio do frete!");
                    this.addValidacaoCabCTeTerc(strBuilder.toString());
                    return;
                }

                if (this.atualizacaoDadosFreteHelper == null) {
                    this.atualizacaoDadosFreteHelper = new AtualizacaoDadosFreteHelper(this.dwfEntityFacade);
                }

                boolean isRatICMSFretCTE = MGECoreParameter.getParameterAsBoolean("mge.comercial.cte.ratear.icms.frete");
                BigDecimal vlrDesdobramentoCTe = cteXmlNotaCompra.getDiversos().getVlrDesdobramento();
                BigDecimal baseIcmsFrete = BigDecimalUtil.getValueOrZero(cteXmlNotaCompra.getImpostosCteZap().getBaseIcms());
                BigDecimal icmsFrete = BigDecimalUtil.getValueOrZero(cteXmlNotaCompra.getImpostosCteZap().getVlrIcms());
                BigDecimal vlrFreteProporcional = BigDecimal.ZERO;
                BigDecimal vlrAcumulado = BigDecimal.ZERO;
                BigDecimal baseIcmsFreteRateado = BigDecimal.ZERO;
                BigDecimal baseIcmsFreteAcumulada = BigDecimal.ZERO;
                BigDecimal icmsFreteRateado = BigDecimal.ZERO;
                BigDecimal icmsFreteAcumulado = BigDecimal.ZERO;
                int qtdNotasAnalisadas = 1;
                int qtdNotasRefCTe = cabRefNFeCollection.size();
                i$ = cabRefNFeCollection.iterator();

                while(i$.hasNext()) {
                    DynamicVO cabRefVo = (DynamicVO)i$.next();
                    if (qtdNotasAnalisadas != qtdNotasRefCTe) {
                        vlrFreteProporcional = this.calculaValorProporcional(vlrDesdobramentoCTe, cabRefVo);
                    } else {
                        vlrFreteProporcional = vlrDesdobramentoCTe.subtract(vlrAcumulado);
                    }

                    if (isRatICMSFretCTE) {
                        baseIcmsFreteRateado = this.calculaValorProporcional(baseIcmsFrete, cabRefVo);
                        icmsFreteRateado = this.calculaValorProporcional(icmsFrete, cabRefVo);
                        baseIcmsFreteAcumulada = baseIcmsFreteAcumulada.add(baseIcmsFreteRateado);
                        icmsFreteAcumulado = icmsFreteAcumulado.add(icmsFreteRateado);
                    }

                    vlrAcumulado = vlrAcumulado.add(vlrFreteProporcional);
                    ++qtdNotasAnalisadas;
                    this.atualizacaoDadosFreteHelper.addDadosFreteNota(cabRefVo.asBigDecimal("NUNOTA"), cteXmlNotaCompra.getPreferenciaCteZap().getData(), vlrFreteProporcional, baseIcmsFreteRateado, icmsFreteRateado);
                }

                this.atualizacaoDadosFreteHelper.atualizarDadosFreteNota();
                this.calcularCustos(this.dwfEntityFacade.getJdbcWrapper());
            }
        }
    }

    private void calcularCustos(JdbcWrapper jdbc) throws Exception {
        if (MGECoreParameter.getParameterAsBoolean("mgecom.recalcular.custos.vinculo.nota.importacao.cte") && this.notasDoCTe.size() != 0) {
            PrecoCustoHelper precoCusto = null;
            NativeSql queNotas = null;

            try {
                queNotas = new NativeSql(jdbc);
                queNotas.appendSql("SELECT C.NUNOTA FROM TGFCAB C");
                queNotas.appendSql("   INNER JOIN TGFTOP T ON(C.CODTIPOPER = T.CODTIPOPER AND C.DHTIPOPER = T.DHALTER)");
                queNotas.appendSql(" WHERE");
                queNotas.appendSql("    C.TIPMOV = 'C' ");
                queNotas.appendSql("    AND C.STATUSNOTA = 'L'");
                queNotas.appendSql("    AND T.PRECIFICA NOT IN('A', 'N')");
                queNotas.appendSql("    AND ").appendSql(SQLUtils.buildINClauseByValues("C.NUNOTA", this.notasDoCTe));
                ResultSet rs = null;

                try {
                    for(rs = queNotas.executeQuery(); rs.next(); precoCusto.calcularCustoEntradaNota(rs.getBigDecimal("NUNOTA"), jdbc)) {
                        if (precoCusto == null) {
                            precoCusto = new PrecoCustoHelper();
                        }
                    }
                } finally {
                    JdbcUtils.closeResultSet(rs);
                }
            } finally {
                NativeSql.releaseResources(queNotas);
            }

        }
    }

    private BigDecimal calculaValorProporcional(BigDecimal valor, DynamicVO cabVO) throws Exception {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal percentual = BigDecimal.ZERO;
        String ratearPor = "P".equals(this.tipoCriterioRateio) && BigDecimalUtil.getValueOrZero(this.pesoTotalNotasCTe).doubleValue() <= 0.0D ? "V" : this.tipoCriterioRateio;
        if ("V".equals(ratearPor)) {
            if (BigDecimalUtil.getValueOrZero(this.vlrTotalNotasCTe).doubleValue() > 0.0D) {
                percentual = BigDecimalUtil.getRounded(cabVO.asBigDecimal("VLRNOTA").divide(this.vlrTotalNotasCTe, BigDecimalUtil.MATH_CTX), 4);
                result = BigDecimalUtil.getRounded(valor.multiply(percentual, BigDecimalUtil.MATH_CTX), 2);
            }
        } else if (BigDecimalUtil.getValueOrZero(this.pesoTotalNotasCTe).doubleValue() > 0.0D) {
            percentual = BigDecimalUtil.getRounded(cabVO.asBigDecimalOrZero("PESO").divide(this.pesoTotalNotasCTe, BigDecimalUtil.MATH_CTX), 4);
            result = BigDecimalUtil.getRounded(valor.multiply(percentual, BigDecimalUtil.MATH_CTX), 2);
        }

        return result;
    }

    private void addValidacaoCabCTeTerc(String mensagem) throws Exception {
        if (this.validacaoCabCTeTerc == null) {
            this.validacaoCabCTeTerc = new Element("validacaoCabCTeTerc");
        } else {
            String oldValue = XMLUtils.getContentChildAsString(this.validacaoCabCTeTerc, "msg");
            XMLUtils.removeChildIgnoringNameSpace(this.validacaoCabCTeTerc, "msg");
            mensagem = oldValue.concat("<br><br>").concat(mensagem);
        }

        XMLUtils.addCDATAContentElement(this.validacaoCabCTeTerc, "msg", mensagem);
    }

    private void atualizaImpostosCTeTerceiros(DynamicVO notaVO, DynamicVO itemVO) throws Exception {
        BigDecimal nunota = notaVO.asBigDecimal("NUNOTA");
        PersistentLocalEntity cabEntity = this.dwfEntityFacade.findEntityByPrimaryKey("CabecalhoNota", new Object[]{nunota});
        ImpostosHelpper impostosHelpper = new ImpostosHelpper();
        impostosHelpper.referenciarNota(notaVO, cabEntity);
        impostosHelpper.calcularImpostosItem((ItemNotaVO)itemVO.wrapInterface(ItemNotaVO.class), itemVO.asBigDecimalOrZero("CODPROD"));
        impostosHelpper.totalizarNota(nunota);
        impostosHelpper.salvarNota();
        impostosHelpper.calcularPIS();
        impostosHelpper.calcularCOFINS();
        impostosHelpper.calcularCSSL();
        impostosHelpper.salvarNota();
    }

    private void vinculaNotaConhecimentoTransporteCTeTerceiros(DynamicVO cabVO, CteXmlNotaCompra cteXmlNotaCompra) throws Exception {
        Collection<InfoNF> infoNFs = cteXmlNotaCompra.getInfoNFs();
        if (!infoNFs.isEmpty()) {
            Iterator i$ = infoNFs.iterator();

            while(i$.hasNext()) {
                InfoNF infoNF = (InfoNF)i$.next();
                DynamicVO notaCTVO = (DynamicVO)this.dwfEntityFacade.getDefaultValueObjectInstance("NotaConhecimentoTransporte");
                notaCTVO.setProperty("CODMODDOC", infoNF.getModelo());
                notaCTVO.setProperty("SERIE", infoNF.getSerie());
                notaCTVO.setProperty("NUMERO", infoNF.getNumero());
                notaCTVO.setProperty("DTEMISSAO", infoNF.getDtEmissao());
                notaCTVO.setProperty("BASEICMS", infoNF.getVlrBaseIcms());
                notaCTVO.setProperty("VLRICMS", infoNF.getVlrIcms());
                notaCTVO.setProperty("BASEST", infoNF.getVlrBaseST());
                notaCTVO.setProperty("VLRST", infoNF.getVlrST());
                notaCTVO.setProperty("VLRTOTPROD", infoNF.getVlrTotalProdutos());
                notaCTVO.setProperty("VLRNOTA", infoNF.getVlrNf());
                notaCTVO.setProperty("CFOP", infoNF.getCfop());
                notaCTVO.setProperty("CHAVENFE", infoNF.getChave());
                notaCTVO.setProperty("NUNOTA", cabVO.asBigDecimal("NUNOTA"));
                this.dwfEntityFacade.createEntity("NotaConhecimentoTransporte", (EntityVO)notaCTVO);
            }
        }

    }

    private void incluirFinanceiroCteTerceiros(CteXmlNotaCompraZap cteXmlNotaCompra, DynamicVO cabVO) throws Exception {
        this.dwfEntityFacade.removeByCriteria(new FinderWrapper("Financeiro", "this.NUNOTA = ?", new Object[]{cabVO.asBigDecimal("NUNOTA")}));

        int desdobramento = 1;
        Collection<DynamicVO> parcelasPagamentoVO = new ArrayList();
        if (cabVO != null && cabVO.asBigDecimal("CODTIPVENDA") != null) {
            FinderWrapper finderPPG = new FinderWrapper("ParcelaPagamento", "this.CODTIPVENDA = ? AND ((this.TIPOEMP = 'E' AND this.TIPOPAR = 'N')  OR (this.TIPOEMP = 'X' AND this.CODEMP = ?)) ");
            finderPPG.setFinderArguments(new Object[]{cabVO.asBigDecimal("CODTIPVENDA"), this.codEmp});
            finderPPG.setOrderBy("this.PRAZO, this.TIPOEMP, this.CODEMP, this.TIPOPAR, this.CODPARC");
            parcelasPagamentoVO.addAll(this.dwfEntityFacade.findByDynamicFinderAsVO(finderPPG));
        }

        Iterator<DynamicVO> ppgIter = parcelasPagamentoVO.iterator();
        DadosFin dadosFin = new DadosFin();
        XmlNotaCompra.DiversosXmlCompra diversos = cteXmlNotaCompra.getDiversos();
        if (diversos != null) {
            dadosFin.setDtVenc(diversos.getDtNeg());
            dadosFin.setVlrDesdob(diversos.getVlrDesdobramento());
            dadosFin.setChaveAcesso(diversos.getChaveAcesso());
            dadosFin.setChaveAcessoReferencia(diversos.getChaveAcessoReferencia());
        }

        if (cteXmlNotaCompra.getFinanceirosCte().size() == 0 && dadosFin.getDtVenc() != null) {
            DynamicVO ppgVO = this.getDadosParcelaPagamento(ppgIter, parcelasPagamentoVO);
            if (ppgVO != null && ppgVO.asInt("PRAZO") > 0) {
                dadosFin.setDtVenc(new Timestamp(TimeUtils.add(dadosFin.getDtVenc().getTime(), ppgVO.asInt("PRAZO"), 5)));
            }

            this.incluirFinanceiroCteTerceiros(cabVO, desdobramento, ppgVO, dadosFin);
        } else {
            for(Iterator i$ = cteXmlNotaCompra.getFinanceirosCte().values().iterator(); i$.hasNext(); ++desdobramento) {

                FinanceiroXmlCompraCte financeiro = (FinanceiroXmlCompraCte)i$.next();
                if (financeiro.getDtVenc() == null) {
                    throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E03148", new Exception("O financeiro do arquivo não poderá ser utilizado por não haver informação de data de vencimento."));
                }

                dadosFin.setDtVenc(financeiro.getDtVenc());
                if (financeiro.getValor() != null) {
                    dadosFin.setVlrDesdob(financeiro.getValor());
                }

                this.incluirFinanceiroCteTerceiros(cabVO, desdobramento, this.getDadosParcelaPagamento(ppgIter, parcelasPagamentoVO), dadosFin);
            }
        }
    }

    private DynamicVO getDadosParcelaPagamento(Iterator<DynamicVO> ppgIter, Collection<DynamicVO> parcelasPagamentoVO) throws Exception {
        DynamicVO ppgVO = null;
        if (ppgIter.hasNext()) {
            ppgVO = (DynamicVO)ppgIter.next();
        } else if (!parcelasPagamentoVO.isEmpty()) {
            ppgVO = (DynamicVO)parcelasPagamentoVO.iterator().next();
        }

        return ppgVO;
    }

    private void incluirFinanceiroCteTerceiros(DynamicVO cabVO, int desdobramento, DynamicVO ppgVO, DadosFin dadosFin) throws Exception {
        DynamicVO financeiroVO = (DynamicVO)this.dwfEntityFacade.getDefaultValueObjectInstance("Financeiro");
        financeiroVO.setProperty("CODEMP", this.codEmp);
        financeiroVO.setProperty("NUNOTA", cabVO.asBigDecimal("NUNOTA"));
        financeiroVO.setProperty("NUMNOTA", cabVO.asBigDecimal("NUMNOTA"));
        financeiroVO.setProperty("DTNEG", cabVO.asTimestamp("DTNEG"));
        financeiroVO.setProperty("CODPARC", this.codParc);
        financeiroVO.setProperty("CODNAT", cabVO.asBigDecimal("CODNAT"));
        financeiroVO.setProperty("CODCENCUS", cabVO.asBigDecimal("CODCENCUS"));
        financeiroVO.setProperty("CODTIPOPER", cabVO.asBigDecimal("CODTIPOPER"));
        financeiroVO.setProperty("DHTIPOPER", cabVO.asTimestamp("DHTIPOPER"));
        financeiroVO.setProperty("CODPARC", cabVO.asBigDecimal("CODPARC"));
        financeiroVO.setProperty("VLRDESDOB", dadosFin.getVlrDesdob());
        financeiroVO.setProperty("DTVENC", dadosFin.getDtVenc());
        financeiroVO.setProperty("RECDESP", new BigDecimal(-1));
        financeiroVO.setProperty("ORIGEM", "E");
        financeiroVO.setProperty("DTENTSAI", cabVO.asTimestamp("DTENTSAI"));
        financeiroVO.setProperty("PROVISAO", this.getProvisao(this.topVO, cabVO));
        financeiroVO.setProperty("DESDOBRAMENTO", String.valueOf(desdobramento));
        financeiroVO.setProperty("CHAVECTE", dadosFin.getChaveAcesso());
        financeiroVO.setProperty("CHAVECTEREF", dadosFin.getChaveAcessoReferencia());
        if (ppgVO != null) {
            if (ppgVO.asBigDecimal("CODBCOPAD") != null) {
                financeiroVO.setProperty("CODBCO", ppgVO.asBigDecimal("CODBCOPAD"));
            }

            if (ppgVO.asBigDecimal("CODCTABCOINT") != null) {
                financeiroVO.setProperty("CODCTABCOINT", ppgVO.asBigDecimal("CODCTABCOINT"));
            }

            if (ppgVO.asBigDecimal("CODTIPTITPAD") != null) {
                financeiroVO.setProperty("CODTIPTIT", ppgVO.asBigDecimal("CODTIPTITPAD"));
            }
        }

        this.dwfEntityFacade.createEntity("Financeiro", (EntityVO)financeiroVO);
    }


    private String getProvisao(DynamicVO topVO, DynamicVO cabVO) throws Exception {
        if ("P".equals(topVO.asString("TIPATUALFIN"))) {
            return "S";
        } else {
            return ("V".equals(cabVO.asString("TIPMOV")) || "C".equals(cabVO.asString("TIPMOV")) || "D".equals(cabVO.asString("TIPMOV"))) && !"L".equals(cabVO.asString("STATUSNOTA")) ? "S" : "N";
        }
    }

    private void updateInfoImportacao(final String field, final Object value) throws Exception {
        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    PersistentLocalEntity xmlNotasImportacaoEntity = dwfEntityFacade.findEntityByPrimaryKey("ImportacaoXMLNotas", ImportacaoXmlNotaCompraZapHelper.this.nuArquivoImportacao);
                    DynamicVO xmlNotasImportacaoVO = (DynamicVO)xmlNotasImportacaoEntity.getValueObject();
                    xmlNotasImportacaoVO.setProperty(field, value);
                    xmlNotasImportacaoVO.setProperty("CODUSUPROC", AuthenticationInfo.getCurrent().getUserID());
                    xmlNotasImportacaoVO.setProperty("DHPROCESS", TimeUtils.getNow());
                    xmlNotasImportacaoEntity.setValueObject((EntityVO)xmlNotasImportacaoVO);
                }
            });
        } finally {
            JapeSession.close(hnd);
        }
    }

    private DynamicVO incluirItensCTeTerceiros(DynamicVO notaVO, CteXmlNotaCompraZap cteXmlNotaCompra) throws Exception {
        DynamicVO itemVO = (DynamicVO)this.dwfEntityFacade.getDefaultValueObjectInstance("ItemNota");
        DynamicVO servicoVO = (DynamicVO)this.dwfEntityFacade.findEntityByPrimaryKeyAsVO("Servico", cteXmlNotaCompra.getPreferenciaCteZap().getCodServCabCTeTerc());
        itemVO.setProperty("CODPROD", servicoVO.asBigDecimal("CODPROD"));
        itemVO.setProperty("CODVOL", this.montarUnidadeProduto(servicoVO.asString("CODVOL")));
        itemVO.setProperty("VLRTOT", cteXmlNotaCompra.getDiversos().getVlrDesdobramento());
        itemVO.setProperty("VLRUNIT", cteXmlNotaCompra.getDiversos().getVlrDesdobramento());
        itemVO.setProperty("USOPROD", servicoVO.asString("USOPROD"));
        itemVO.setProperty("NUNOTA", notaVO.asBigDecimal("NUNOTA"));
        itemVO.setProperty("CODEMP", notaVO.asBigDecimal("CODEMP"));
        itemVO.setProperty("QTDNEG", BigDecimal.ONE);
        itemVO.setProperty("CONTROLE", " ");

        if  (this.codEmp.intValue() == CodigoEmpresa.ZAP_GRAFICA.getValue()) {
            itemVO.setProperty("CODTRIB", BigDecimal.valueOf(90));
        } else {
            itemVO.setProperty("CODTRIB", cteXmlNotaCompra.getImpostosCteZap().getCodTrib());
        }

        String tipoCalculoICMS = notaVO.asString("TipoOperacao.CALCICMS");
        if ("B".equals(tipoCalculoICMS)) {
            itemVO.setProperty("ALIQICMS", cteXmlNotaCompra.getImpostosCteZap().getAliqIcms());
            itemVO.setProperty("BASEICMS", cteXmlNotaCompra.getImpostosCteZap().getBaseIcms());
            itemVO.setProperty("VLRICMS", cteXmlNotaCompra.getImpostosCteZap().getVlrIcms());
        }

        String oldStatusNotaItem = itemVO.asString("STATUSNOTA");
        itemVO.setProperty("STATUSNOTA", "L");
        ComercialUtils.atualizarAtualEstoque(itemVO, notaVO);
        itemVO.setProperty("STATUSNOTA", oldStatusNotaItem);

        PreferenciasFinanceiroCte prefFinanceiroCte = cteXmlNotaCompra.getPreferenciaCteZap();
        if ("C".equals(prefFinanceiroCte.getTipoImportacaoCabCTeTerc())) {
            ImpostosXmlCte impostosXmlCte = cteXmlNotaCompra.getImpostosCteZap();
            if ("C".equals(prefFinanceiroCte.getObtencaoCFOP())) {
                itemVO.setProperty("CODCFO", impostosXmlCte.getCfop());
                this.cfopCalculado = true;
            }

            if ("T".equals(prefFinanceiroCte.getObtencaoCFOP()) || itemVO.asInt("CODCFO") == 0) {
                if (BigDecimalUtil.getValueOrZero(prefFinanceiroCte.getCodTop()).intValue() > 0) {
                    DynamicVO parcVO = (DynamicVO)this.dwfEntityFacade.findEntityByPrimaryKeyAsVO("Parceiro", new Object[]{this.codParceiroXML});
                    DynamicVO empVO = (DynamicVO)this.dwfEntityFacade.findEntityByPrimaryKeyAsVO("Empresa", new Object[]{this.codEmp});
                    DynamicVO munIni = this.munIniVO.asInt("CODCID") > 0 ? this.munIniVO : empVO.asDymamicVO("Cidade");
                    DynamicVO munFim = this.munFimVO.asInt("CODCID") > 0 ? this.munFimVO : parcVO.asDymamicVO("Cidade");
                    boolean ehCFOForaEstado = munIni.asInt("CODCID") != munFim.asInt("CODCID") && munIni.asDymamicVO("UnidadeFederativa").asInt("CODUF") != munFim.asDymamicVO("UnidadeFederativa").asInt("CODUF");
                    this.topVO = ComercialUtils.getTipoOperacao(prefFinanceiroCte.getCodTop());
                    if (ehCFOForaEstado) {
                        itemVO.setProperty("CODCFO", topVO.asBigDecimalOrZero("CODCFO_ENTRADA_FORA"));
                    } else {
                        itemVO.setProperty("CODCFO", topVO.asBigDecimalOrZero("CODCFO_ENTRADA"));
                    }
                }

                this.cfopCalculado = false;
            }
        } else {
            itemVO.setProperty("CODCFO", cteXmlNotaCompra.getDiversos().getCfop());
        }

        PersistentLocalEntity itemEntity = this.dwfEntityFacade.createEntity("ItemNota", (EntityVO)itemVO);
        itemEntity.setValueObject((EntityVO)itemVO);
        return itemVO;
    }

    private String montarUnidadeProduto(String codVol) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rs = null;

        String var6;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            boolean ignoraCaseSensitive = MGECoreParameter.getParameterAsBoolean("br.com.sankhya.ignora.case.sensitive.importacao");
            if (ignoraCaseSensitive) {
                sql = new NativeSql(jdbc);
                sql.appendSql("SELECT CODVOL FROM TGFVOL WHERE UPPER(CODVOL) = UPPER(:CODVOL)");
                sql.setNamedParameter("CODVOL", codVol);
                rs = sql.executeQuery();
                if (rs.next()) {
                    codVol = rs.getString("CODVOL");
                }
            }

            var6 = codVol;
        } catch (Exception var10) {
            throw (Exception)SKError.registry(TSLevel.ERROR, "CORE_E05728", new Exception("Erro ao tentar montar a unidade do produto: " + var10.getMessage()));
        } finally {
            JdbcUtils.closeResultSet(rs);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }

        return var6;
    }

    private String getObservacao(String obs) {
        return StringUtils.isNotEmpty(obs) && obs.length() > 4000 ? obs.trim().substring(0, 4000) : obs;
    }

    private Element buscaValidaMunicipioOrigDest(Document doc) throws PersistenceException, Exception {
        Element ide = null;
        Element validaErrosBuscaMun = new Element("erroCodMunIniFim");
        String msg = "";
        //Document doc = XMLUtils.buildDocumentFromString(new String(this.importacaoXMLNotaVO.asClob("XML")));
        String codMunicipioInicioStr = this.getElementText(XMLUtils.SimpleXPath.selectSingleNode(doc.getRootElement(), ".//ide"), "cMunIni");
        BigDecimal codMunicipioInicio = null;
        Collection munIniColVO = null;

        try {
            codMunicipioInicio = new BigDecimal(codMunicipioInicioStr);
            munIniColVO = this.dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper("Cidade", "this.CODMUNFIS = ?", new Object[]{codMunicipioInicio}));
        } catch (NumberFormatException var17) {
            msg = "Código Município início da tag &lt;cMunIni&gt; é inválido: " + codMunicipioInicioStr + ".<br>";
        }

        String codMunicipioFimStr = this.getElementText(XMLUtils.SimpleXPath.selectSingleNode(doc.getRootElement(), ".//ide"), "cMunFim");
        BigDecimal codMunicipioFim = null;
        Collection munFimColVO = null;

        try {
            codMunicipioFim = new BigDecimal(codMunicipioFimStr);
            munFimColVO = this.dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper("Cidade", "this.CODMUNFIS = ?", new Object[]{codMunicipioFim}));
        } catch (NumberFormatException var16) {
            msg = msg + "Código Município fim da tag &lt;cMunFim&gt; é inválido: " + codMunicipioFimStr + ".<br>";
        }

        boolean encontrouCidade = false;
        String nomeCidade;
        String cidadeXml;
        Iterator mun;
        DynamicVO municFimVO;
        if (munIniColVO != null && munIniColVO.size() != 0) {
            this.munIniVO = (DynamicVO)munIniColVO.iterator().next();
            if (munIniColVO.size() > 1) {
                mun = munIniColVO.iterator();

                while(mun.hasNext()) {
                    municFimVO = (DynamicVO)mun.next();
                    nomeCidade = NFeUtils.removeCaracteresEspeciais(StringUtils.trim(municFimVO.asString("NOMECID")).toUpperCase());
                    cidadeXml = NFeUtils.removeCaracteresEspeciais(StringUtils.trim(this.getElementText(XMLUtils.SimpleXPath.selectSingleNode(doc.getRootElement(), ".//ide"), "xMunIni"))).toUpperCase();
                    if (nomeCidade.contains(cidadeXml)) {
                        encontrouCidade = true;
                        this.munIniVO = municFimVO;
                        break;
                    }
                }

                if (!encontrouCidade) {
                    msg = msg + "Foi encontrado mais de um Município com o código de Mun. Ini: " + codMunicipioInicioStr + ", porém nenhum deles tem o nome igual ao do XML.<br>";
                }
            }
        } else if (codMunicipioInicio != null) {
            msg = msg + "Código Município início = " + codMunicipioInicioStr + " não encontrado.<br>";
        }

        if (munFimColVO != null && munFimColVO.size() != 0) {
            this.munFimVO = (DynamicVO)munFimColVO.iterator().next();
            if (munFimColVO.size() > 1) {
                encontrouCidade = false;
                mun = munFimColVO.iterator();

                while(mun.hasNext()) {
                    municFimVO = (DynamicVO)mun.next();
                    nomeCidade = NFeUtils.removeCaracteresEspeciais(StringUtils.trim(municFimVO.asString("NOMECID")).toUpperCase());
                    cidadeXml = NFeUtils.removeCaracteresEspeciais(StringUtils.trim(this.getElementText(XMLUtils.SimpleXPath.selectSingleNode(doc.getRootElement(), ".//ide"), "xMunFim"))).toUpperCase();
                    if (nomeCidade.contains(cidadeXml)) {
                        encontrouCidade = true;
                        this.munFimVO = municFimVO;
                        break;
                    }
                }

                if (!encontrouCidade) {
                    msg = msg + "Foi encontrado mais de um Município com o código de Mun. Fim = " + codMunicipioFimStr + ", porém nenhum deles tem o nome igual ao do XML.<br>";
                }
            }
        } else if (codMunicipioFim != null) {
            msg = msg + "Código Município fim = " + codMunicipioFimStr + " não encontrado.<br>";
        }

        return msg.isEmpty() ? null : validaErrosBuscaMun.addContent(XMLUtils.buildCDATAElement("msg", msg));
    }

    private String getElementText(Element base, String path) throws Exception {
        return XMLUtils.SimpleXPath.getNodeText(base, path);
    }

    public String getTipoCriterioRateio() {
        return tipoCriterioRateio;
    }

    public void setTipoCriterioRateio(String tipoCriterioRateio) {
        this.tipoCriterioRateio = tipoCriterioRateio;
    }
}
