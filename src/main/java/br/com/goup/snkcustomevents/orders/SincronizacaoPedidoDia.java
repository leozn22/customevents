package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SincronizacaoPedidoDia extends SnkIntegrationsApi implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        try {
            scheduledActionContext.info("Iniciando rotina\n");

            String json = this.getJsonProcessoPedido();

            String token = IntegrationApi.getToken(this.getUrl() + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            scheduledActionContext.info(IntegrationApi.sendHttp(this.getUrl() + "/v2/processos",  json, "POST", "Bearer " + token));
            scheduledActionContext.info("Fim da execução!\n");

        } catch (Exception e) {
            scheduledActionContext.info("Erro: ");
            scheduledActionContext.info(e.getMessage() + "\n");
        }
    }


    private String getJsonProcessoPedido() {

        SimpleDateFormat formatoData = new SimpleDateFormat("yyyy-MM-dd");
        Calendar diaAnterior = Calendar.getInstance();
        diaAnterior.add(Calendar.DAY_OF_YEAR, -1);
        String data = formatoData.format(diaAnterior.getTime());

        String retorno =  "{"
                + "'processo': 'processo.integrarPedidosAsync',"
                + "'parametros': {"
                + "  'tipo': 'dia',"
                + "  'dia': '" + data + "'"
                + "  }"
                + "}";

        return  retorno;
    }
}
