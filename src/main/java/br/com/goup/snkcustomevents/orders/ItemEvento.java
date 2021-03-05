package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

public class ItemEvento extends SnkIntegrationsApi implements EventoProgramavelJava {

    private int qtdException = 0;

    public ItemEvento() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private void enviarDados(String verboHttp, String url, String json) throws Exception {

        this.qtdException++;

        try {
            String autorizacao = "";
            if (this.exigeAutenticacao) {
                String token = IntegrationApi.getToken(this.urlApi
                        + "/oauth/token?grant_type=client_credentials",
                        "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
                autorizacao  = "Bearer " + token;
            }
            IntegrationApi.sendHttp(url, json, verboHttp, autorizacao);
        } catch (Exception e) {
            if (this.qtdException < 2) {
                enviarDados(verboHttp, url, json);
            } else {
                throw new Exception("Falha: " + e.getMessage() + "\n" + json);
            }
        }
        this.qtdException = 0;
    }

    private void enviarDesconto(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO itemVO = (DynamicVO) persistenceEvent.getVo();

        if (itemVO.getProperty("CONTROLE") != null
                && itemVO.getProperty("USOPROD").toString().equals("V")
                && persistenceEvent.getModifingFields().isModifing("VLRDESC")
                && AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {

            String url = this.urlApi + "/v2/pedidos/itens?esperar=true&usuario=" + AuthenticationInfo.getCurrent().getName();

            String json = "[{\"codigoItem\": "  + itemVO.getProperty("CONTROLE") + ",  \"valorDesconto\": "
                    + persistenceEvent.getModifingFields().getNewValue("VLRDESC").toString() +  "}]";

            this.enviarDados("PUT", url, json);

        }
    }

    private void travarAlteracaoLocal(PersistenceEvent persistenceEvent) {
        DynamicVO itemVO = (DynamicVO) persistenceEvent.getVo();
        if (itemVO.getProperty("USOPROD").toString().equals("M")) {

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

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        this.enviarDesconto(persistenceEvent);
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
