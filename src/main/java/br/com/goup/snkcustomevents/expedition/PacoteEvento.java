package br.com.goup.snkcustomevents.expedition;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

public class PacoteEvento extends SnkIntegrationsApi implements EventoProgramavelJava {

    private int qtdException = 0;

    public PacoteEvento(){
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
                throw new Exception("Falha: " + e.getMessage() + "\n Url:" + url + "\n" + json);
            }
        }
        this.qtdException = 0;
    }

    private void sincronizarPacote(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO pacoteVo = (DynamicVO) persistenceEvent.getVo();

        int numeroPacote = pacoteVo.asInt("NUPCT");
        String status = pacoteVo.asString("STATUS");

        if ((numeroPacote > 0) && ("AE".equals(status))) {
            String url = this.urlApi + "/v2/snk/pacotes/sincronizacoes/" + numeroPacote +  "?assincrono=true";
            this.enviarDados("POST", url, "");
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
            this.sincronizarPacote(persistenceEvent);
        }
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.sincronizarPacote(persistenceEvent);
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
