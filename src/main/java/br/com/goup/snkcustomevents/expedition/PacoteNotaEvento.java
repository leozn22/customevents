package br.com.goup.snkcustomevents.expedition;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.google.gson.Gson;

import java.sql.ResultSet;

public class PacoteNotaEvento extends SnkIntegrationsApi implements EventoProgramavelJava {

    private int qtdException = 0;

    public PacoteNotaEvento(){
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private void enviarDados(String verboHttp, String url, String json) throws Exception {
        this.qtdException++;
        try {
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            IntegrationApi.sendHttp(url, json, verboHttp, "Bearer " + token);
        } catch (Exception e) {
            if (this.qtdException < 3) {
                enviarDados(verboHttp, url, json);
            } else {
                throw new Exception("Falha: " + e.getMessage() + "\n" + json);
            }
        }
        this.qtdException = 0;
    }

    private int retornaNumeroPacote(PersistenceEvent persistenceEvent) {
        int numeroPacote = 0;

        DynamicVO cabecalhoNotaVo = (DynamicVO) persistenceEvent.getVo();

        JdbcWrapper jdbc 	   = persistenceEvent.getJdbcWrapper();
        NativeSql sql		   = new NativeSql(jdbc);

        StringBuffer consulta = new StringBuffer();
        consulta.append("    SELECT NUPCT, ");
        consulta.append("	        NUNOTAREMESSA ");
        consulta.append("      FROM TZAPCT_TGFCAB ");
        consulta.append("     WHERE NUNOTAREMESSA = :NUNOTAREMESSA ");
        consulta.append("     AND ROWNUM = 1 ");
        consulta.append("     ORDER BY NUPCT DESC ");

        try {
            sql.setNamedParameter("NUNOTAREMESSA", cabecalhoNotaVo.asBigDecimal("NUNOTA").intValue());
            ResultSet result = sql.executeQuery(consulta.toString());

            if (result.next()) {
                numeroPacote = result.getBigDecimal("NUPCT").intValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            numeroPacote = 0;
        }

        return numeroPacote;
    }

    private void sincronizarNota(PersistenceEvent persistenceEvent) throws Exception {
        int numeroPacote = retornaNumeroPacote(persistenceEvent);

        if (numeroPacote > 0) {
            DynamicVO cabecalhoVo = (DynamicVO) persistenceEvent.getVo();

            PacoteNota pacoteNota = new PacoteNota();
            pacoteNota.setNuPct(numeroPacote);
            pacoteNota.setNuNota(cabecalhoVo.asInt("NUNOTA"));
            pacoteNota.setNumeroNfe(cabecalhoVo.asInt("NUMNOTA"));
            pacoteNota.setChaveAcesso(cabecalhoVo.asString("CHAVENFE"));
            pacoteNota.setSerieNfe(cabecalhoVo.asString("SERIENOTA"));
            pacoteNota.setStatusNfe(cabecalhoVo.asString("STATUSNFE"));

            Gson gson = new Gson();
            String json = gson.toJson(pacoteNota);
            String url = this.urlApi + "/v2/snk/pacotes/itens?assincrono=true";
            this.enviarDados("PUT", url, json);
        }
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.sincronizarNota(persistenceEvent);
        }
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
