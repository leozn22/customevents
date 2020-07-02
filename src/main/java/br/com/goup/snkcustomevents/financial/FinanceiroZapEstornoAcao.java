package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

public class FinanceiroZapEstornoAcao extends SnkIntegrationsApi implements AcaoRotinaJava  {

    public FinanceiroZapEstornoAcao() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        for (Registro registro: contextoAcao.getLinhas()) {

            String url   = this.urlApi + "/v2/caixas/pagamentos/" + registro.getCampo("NUFIN").toString();
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            try {
                String json = "{ \"idUsuario\": " + contextoAcao.getUsuarioLogado() + "}";
                IntegrationApi.sendHttp(url, json, "DELETE", "Bearer " + token);
            } catch (Exception e) {
                throw new Exception("Falha: " + e.getMessage());
            }
            String mensagem = "Solicitacao enviada com sucesso!";
            contextoAcao.setMensagemRetorno(mensagem);
        }
    }
}
