package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.Enumerator.IdTipoOperacaoPedidoVenda;
import br.com.goup.snkcustomevents.Enumerator.IdTipoTitulo;
import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

public class RetornoBoletoSite  extends SnkIntegrationsApi implements EventoProgramavelJava {

    public RetornoBoletoSite() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private boolean eBoletoSiteBaixado(DynamicVO financeiro) throws Exception {

        boolean eOperacaoVendaIntegracao = false;

        for (IdTipoOperacaoPedidoVenda idTop:
             IdTipoOperacaoPedidoVenda.values()) {
            eOperacaoVendaIntegracao = (eOperacaoVendaIntegracao ? eOperacaoVendaIntegracao : financeiro.asInt("CODTIPOPER") == idTop.getValue());
        }

        return  (financeiro.asInt("RECDESP") == 1 // 1 = receita / 0 = inativo / -1 = despesa
                && financeiro.asInt("CODTIPTIT") == IdTipoTitulo.BOLETO.getValue()
                && financeiro.getProperty("DHBAIXA") != null
                && eOperacaoVendaIntegracao);
    }

    private String getJsonProcessoRetornoBoleto(Integer idFinanceiro) {

        String retorno = "{"
                + "'processo': 'processo.sincronizarRetornoBoleto',"
                + "'parametros': {"
                + "  'idFinanceiro': '" + idFinanceiro.toString() + "'"
                + "  }"
                + "}";

        return retorno;
    }

    private void enviarDados(PersistenceEvent persistenceEvent) throws Exception {

        try {
            DynamicVO financeiro = (DynamicVO) persistenceEvent.getVo();
            ModifingFields modifingFields = persistenceEvent.getModifingFields();

            if (modifingFields.isModifing("DHBAIXA")
                    && modifingFields.getNewValue("DHBAIXA") != null
                    && this.eBoletoSiteBaixado(financeiro)) {
                String json  = this.getJsonProcessoRetornoBoleto(financeiro.asBigDecimal("NUFIN").intValue());
                String url   = this.urlApi + "/v2/processos";
                String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
                IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
            }
        }
        catch(Exception e) {
            throw new Exception("Falha ao sincronizar pagamento! \n Mensagem de erro: " +  e.getMessage());
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
        this.enviarDados(persistenceEvent);
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
