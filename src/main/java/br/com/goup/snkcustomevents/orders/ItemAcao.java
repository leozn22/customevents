package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

public class ItemAcao extends SnkIntegrationsApi implements AcaoRotinaJava {

    public ItemAcao() {
        this.exigeAutenticacao = true;
		// QUANDO ALTERAR O PARÂMETRO ABAIXO, DEVE ALTERAR DA MESMA FORMA NOS ARQUIVOS: UpdateSql.java e UpdateTef.java
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        String json = "";
        for (Registro registro: contextoAcao.getLinhas()) {
            json += (json.isEmpty() ? registro.getCampo("CONTROLE"): ", " + registro.getCampo("CONTROLE"));
        }

        if (!json.isEmpty()) {
            json         = "[" + json + "]";
            String url   = this.urlApi + "/v2/snk/pedidos/itens/tops/3115/3117";
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);

            String mensagem = "Solicitacao enviada com sucesso!";
            contextoAcao.setMensagemRetorno(mensagem);
        }
    }
}
