package br.com.goup.snkcustomevents.expedition;

import br.com.goup.snkcustomevents.Enumerator.IdTipoOperacaoPedidoVenda;
import br.com.goup.snkcustomevents.Enumerator.TipoEntrega;
import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.lugh.performance.PerformanceMonitor;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.google.gson.Gson;

import java.sql.ResultSet;
import java.util.Collection;

public class PacoteNotaEvento extends SnkIntegrationsApi implements EventoProgramavelJava {

    private int qtdException = 0;

    public PacoteNotaEvento() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private void enviarDados(String verboHttp, String url, String json) {
        this.qtdException++;
        try {
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            IntegrationApi.sendHttp(url, json, verboHttp, "Bearer " + token);
        } catch (Exception e) {
            if (this.qtdException < 3) {
                enviarDados(verboHttp, url, json);
            }
        }
        this.qtdException = 0;
    }

    private void atualizaInformacaoPacoteDenegada(PacoteNota pacoteNota)  {
        if (pacoteNota.getNuPct() <= 0) {
            return;
        }

        JapeWrapper pedidoPacoteDAO;
        try {
            pedidoPacoteDAO = JapeFactory.dao("TzapctTgfcab");

            Collection<DynamicVO> listPedidoVO = pedidoPacoteDAO.find("NUNOTAREMESSA = ?", pacoteNota.getNuNota());

            for (DynamicVO pedidoPacote: listPedidoVO) {
                pedidoPacoteDAO.prepareToUpdate(pedidoPacote)
                        .set("AD_STATUS_DENEGADA", "1")
                        .update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PacoteNota retornaDadosPacote(PersistenceEvent persistenceEvent) {
        PacoteNota retorno = new PacoteNota();

        DynamicVO cabecalhoNotaVo = (DynamicVO) persistenceEvent.getVo();

        JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);

        StringBuilder consulta = new StringBuilder();
        consulta.append("SELECT * FROM ");
        consulta.append("( ");
        consulta.append("select 'NFE' AS TIPO, NUPCT, NUNOTAREMESSA AS NUNOTA FROM TZAPCT_TGFCAB ");
        consulta.append("where NUNOTAREMESSA = :NUNOTA ");
        consulta.append("UNION ");
        consulta.append("select 'NFSE' AS TIPO, NUPCT, NUNOTASERVICO AS NUNOTA FROM TZAPCT_TGFCAB ");
        consulta.append("where NUNOTASERVICO = :NUNOTA ");
        consulta.append("UNION ");
        consulta.append("select 'CTE' AS TIPO, NUPCT, NUNOTACTE AS NUNOTA FROM TZAPCT_TGFCAB ");
        consulta.append("where NUNOTACTE = :NUNOTA ");
        consulta.append(") TIPO_NOTA ");
        consulta.append("WHERE ROWNUM = 1 ");
        consulta.append("ORDER BY TIPO_NOTA.NUPCT DESC ");

        try {
            sql.setNamedParameter("NUNOTA", cabecalhoNotaVo.asBigDecimal("NUNOTA").intValue());
            ResultSet result = sql.executeQuery(consulta.toString());

            if (result.next()) {
                retorno.setNuPct(result.getBigDecimal("NUPCT").intValue());
                retorno.setNuNota(result.getBigDecimal("NUNOTA").intValue());
                retorno.setTipoDocumento(result.getString("TIPO"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            retorno.setNuPct(0);
        }

        return retorno;
    }

    private Boolean isNotaReferenteItem(PersistenceEvent persistenceEvent) {
        DynamicVO cabecalhoNotaVo = (DynamicVO) persistenceEvent.getVo();

        JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);

        StringBuilder consulta = new StringBuilder();
        consulta.append("SELECT * FROM ");
        consulta.append("( ");
        consulta.append("select 'NFE' AS TIPO, NUNOTAREMESSA AS NUNOTA FROM AD_TGFFINSAL ");
        consulta.append("where NUNOTAREMESSA = :NUNOTA ");
        consulta.append("UNION ");
        consulta.append("select 'NFSE' AS TIPO, NUNOTASERVICO AS NUNOTA FROM AD_TGFFINSAL ");
        consulta.append("where NUNOTASERVICO = :NUNOTA ");
        consulta.append(") TIPO_NOTA ");
        consulta.append("WHERE ROWNUM = 1 ");

        try {
            sql.setNamedParameter("NUNOTA", cabecalhoNotaVo.asBigDecimal("NUNOTA").intValue());
            ResultSet result = sql.executeQuery(consulta.toString());

            return result.next();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void sincronizarNota(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO pacoteVo = (DynamicVO) persistenceEvent.getVo();

        String statusNfe = pacoteVo.asString("STATUSNFE");
        String statusNfSe = pacoteVo.asString("STATUSNFSE");

        //boolean enviou_dados_zap = JapeSession.getPropertyAsBoolean("enviou_dados_nota_zap", false);

        if ("D".equals(statusNfe) || "A".equals(statusNfe) || "A".equals(statusNfSe)){
            PacoteNota pacoteNota = this.retornaDadosPacote(persistenceEvent);

            if (pacoteNota.getNuPct() > 0) {
                //JapeSession.putProperty("enviou_dados_nota_zap",true);

                if ("D".equals(statusNfe)) {
                    this.atualizaInformacaoPacoteDenegada(pacoteNota);
                } else {
                    PerformanceMonitor.INSTANCE.measureJava("integracaoNotaPacoteZap", () -> {
                        Gson gson = new Gson();
                        String json = gson.toJson(pacoteNota);
                        String url = this.urlApi + "/v2/snk/pacotes/notas?assincrono=true";

                        this.enviarDados("PUT", url, json);
                    });
                }
            } else {
                //Nota é referente a algum item da Zap
                if (this.isNotaReferenteItem(persistenceEvent)) {
                    PerformanceMonitor.INSTANCE.measureJava("integracaoPacoteZap", () -> {
                        String url = this.urlApi + "/v2/snk/notasfiscais/importarnotaoraclesqlserver/" + pacoteVo.asBigDecimal("NUNOTA").intValue() + "?assincrono=true";
                        this.enviarDados("POST", url, "");
                    });
                }
            }
        }
    }

    private void verificaTipoEntrega(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabecalhoVO = (DynamicVO) persistenceEvent.getVo();

        if (cabecalhoVO.asInt("CODTIPOPER") == IdTipoOperacaoPedidoVenda.TOP_NF_SIMPLES_REMESSA.getValue() &&
                cabecalhoVO.asInt("TZACODTET") > 0 && cabecalhoVO.asInt("CODCONTATOENTREGA") > 0 ) {

            JapeWrapper tipoEntregaDAO;
            try {
                tipoEntregaDAO = JapeFactory.dao("TxpZapTipoEntrega");
            } catch (Exception e) {
                throw new Exception("Instancia Tipo de Entrega");
            }

            DynamicVO tipoEntregaVO = tipoEntregaDAO.findByPK(cabecalhoVO.asInt("TZACODTET"));

            if (tipoEntregaVO != null) {
                if (tipoEntregaVO.asString("TIPO") != null && !TipoEntrega.PORTA_A_PORTA.getValue().equals(tipoEntregaVO.asString("TIPO"))) {
                    cabecalhoVO.setProperty("CODCONTATOENTREGA", null);
                }
            }
        }
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.verificaTipoEntrega(persistenceEvent);
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.sincronizarNota(persistenceEvent);
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
