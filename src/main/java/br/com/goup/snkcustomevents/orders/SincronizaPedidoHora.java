package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

public class SincronizaPedidoHora extends SnkIntegrationsApi implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        try {
            scheduledActionContext.info("Iniciando rotina\n");

            String json = "{"
                    + "'processo': 'processo.integrarPedidosAsync',"
                    + "'parametros': {"
                    + "  'tipo': 'hora'"
                    + "  }"
                    + "}";

            String token = IntegrationApi.getToken(this.getUrl() + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            scheduledActionContext.info(IntegrationApi.sendHttp(this.getUrl() + "/v2/processos",  json, "POST", "Bearer " + token));
            scheduledActionContext.info("Fim da execução!\n");

        } catch (Exception e) {
            scheduledActionContext.info("Erro: ");
            scheduledActionContext.info(e.getMessage() + "\n");
        }
    }
}
