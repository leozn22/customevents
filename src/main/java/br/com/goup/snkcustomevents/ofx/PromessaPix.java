package br.com.goup.snkcustomevents.ofx;

import br.com.goup.snkcustomevents.utils.AcessoBanco;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.financeiro.helper.BaixaHelper;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.ParameterUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class PromessaPix {
    private BigDecimal contaBancaria;
    public BigDecimal contadorBaixados;
    public BigDecimal contadorConciliados;

    protected PromessaPix(){
        contaBancaria = BigDecimal.ZERO;
        contadorBaixados = BigDecimal.ZERO;
        contadorConciliados = BigDecimal.ZERO;
    }

    // Conciliar financeiro
    protected void conciliar(BigDecimal nuImp) throws Exception {
        AcessoBanco bd = new AcessoBanco();
        try {

//            ResultSet promessas = bd.find(Constantes.SQL_GET_PROMESSAS_PIX, nuImp, nuImp);
            ResultSet promessas = bd.findCriteria(String.format(Constantes.SQL_GET_PROMESSAS_PIX, nuImp, nuImp));


            while (promessas.next()) {

                contaBancaria = promessas.getBigDecimal("CODCTABCOINT");

                BigDecimal saldoConciliacao = promessas.getBigDecimal("VALORDEPOSITO");

                ResultSet financeirosConciliar;
                try {
                financeirosConciliar = bd.find(Constantes.SQL_GET_FINANCEIROS_CONCILIAR,
                        promessas.getTimestamp("DATADEPOSITO"),
                        promessas.getString("NRODESPOSITO"),
                        promessas.getBigDecimal("NUMNOTA"));
                } catch (Exception e) {
                    throw new Exception("Erro de dados: \n"
                            + promessas.getDate("DATADEPOSITO").toString()
                            + "\n" +  promessas.getString("NRODESPOSITO")
                            + "\n" +  promessas.getBigDecimal("NUMNOTA"));
                }

                while (financeirosConciliar.next()) {

                    // realiza concilia��o controlando saldo baixado

                    JapeWrapper bancoDAO;
                    try {
                        bancoDAO = JapeFactory.dao(DynamicEntityNames.MOVIMENTO_BANCARIO);
                    } catch (Exception e) {
                        throw new Exception("Instancia movimento bancario");
                    }

                    DynamicVO movVO;
                    try {
                        movVO = bancoDAO.findByPK(financeirosConciliar.getBigDecimal("NUBCO"));
                    } catch (Exception e) {
                        throw new Exception("Find Movimento");
                    }

                    boolean temValorConcilair = saldoConciliacao.compareTo(financeirosConciliar.getBigDecimal("VLRDESDOB")) >= 0;

                    if (temValorConcilair && !movVO.getProperty("CONCILIADO").toString().equals("S")) {

                        try {
                            bancoDAO.prepareToUpdate(movVO)
                                    .set("CONCILIADO", "S")
                                    .set("DHCONCILIACAO", financeirosConciliar.getTimestamp("DHBAIXA"))
                                    .set("CODUSU", AuthenticationInfo.getCurrent().getUserID())
                                    .update();

                            addUmConciliados();
                        } catch (Exception e) {
                            throw new Exception("Update");
                        }
                    }

//                    if (!movVO.getProperty("CONCILIADO").toString().equals("S")) {
                        saldoConciliacao = saldoConciliacao.subtract(financeirosConciliar.getBigDecimal("VLRDESDOB"));

                        // retirar da tela de concilia��o do extrato banc�rio nativo
                        BigDecimal nuImport = promessas.getBigDecimal("NUIMPORT");
                        BigDecimal nuExc = promessas.getBigDecimal("NUEXB");

                        JapeWrapper exbDAO = JapeFactory.dao(DynamicEntityNames.EXTRATO_BANCARIO);

                        DynamicVO exbVO = exbDAO.findOne(" NUIMPORT = ? AND NUEXB = ? ", nuImport, nuExc);
                        try {
                            JapeFactory.dao(DynamicEntityNames.EXTRATO_BANCARIO)
                                    .prepareToUpdate(exbVO)
                                    .set("CONCILIADO", "S")
                                    .set("HIST", "Conciliado na tela de OFX")
                                    .set("AD_HIST2", !"Conciliado na tela de OFX".equals(exbVO.asString("HIST")) ? exbVO.asString("HIST") : exbVO.asString("AD_HIST2"))
                                    .set("NUBCO", financeirosConciliar.getBigDecimal("NUBCO"))
                                    .update();

                        } catch (Exception e) {
                            throw new Exception("Update extrato");
                        }
                    }
//                }

            }
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            bd.closeSession();
        }
    }

    // Baixar financeiro
    private void baixar(BigDecimal valorBaixa, ResultSet financeiro, ResultSet promessa) throws Exception {
        BaixaHelper baixaHelper = null;
        try {


            BigDecimal codtipoperBaixarReceita = new BigDecimal(ParameterUtils.getParameter("GZATIPOPERBR").toString());

            baixaHelper = new BaixaHelper(financeiro.getBigDecimal("NUFIN"), AuthenticationInfo.getCurrent().getUserID());
            DadosBaixa dadosBaixa = baixaHelper.montaDadosBaixa(promessa.getTimestamp("DATADEPOSITO"), false, false, (Timestamp) null);

            dadosBaixa.getDadosBancarios().setCodConta(contaBancaria);
            dadosBaixa.getDadosAdicionais().setCodTipoOperacao(codtipoperBaixarReceita);
            dadosBaixa.getDadosBancarios().setNumDocumento(promessa.getBigDecimal("NRODESPOSITO"));
            dadosBaixa.getDadosBancarios().setCodLancamento(BigDecimal.ONE);

            dadosBaixa.getValoresBaixa().setVlrJuros(0);
            dadosBaixa.getValoresBaixa().setVlrMulta(0);
            dadosBaixa.getValoresBaixa().setVlrDesconto(0);
            dadosBaixa.getValoresBaixa().setVlrTotal(valorBaixa.doubleValue());

            // se o valor a baixar dor menor, gerar pend�ncia
            if(financeiro.getBigDecimal("VLRDESDOB").compareTo(valorBaixa) > 0){
                BigDecimal diferenca = financeiro.getBigDecimal("VLRDESDOB").subtract(valorBaixa);
                dadosBaixa.getDadosPendencia().setVlrTotal(diferenca.doubleValue());
                dadosBaixa.getDadosPendencia().setVlrDesconto(0);
                dadosBaixa.getDadosPendencia().setDtVencimento(TimeUtils.getNow());
                DadosBaixa.DescisaoBaixa descisaoBaixa = dadosBaixa.getDescisaoBaixa();
                descisaoBaixa.setDescisao(Constantes.CODIGO_DECISAO_BAIXA_PARCIAL);
                descisaoBaixa.setDataVctoPendencia(TimeUtils.getNow());
            }

            baixaHelper.baixar(dadosBaixa);

            addUmBaixados();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void addUmBaixados(){
        contadorBaixados = contadorBaixados.add(BigDecimal.ONE);
    }

    private void addUmConciliados(){
        contadorConciliados = contadorConciliados.add(BigDecimal.ONE);
    }
}
