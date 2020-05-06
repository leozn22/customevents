package br.com.goup.snkcustomevents.orders;

import java.math.BigDecimal;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

public class CreditoAcao extends SnkIntegrationsApi implements AcaoRotinaJava {

    public CreditoAcao() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        BigDecimal numeroNota = BigDecimal.ZERO;
        for (Registro registro: contextoAcao.getLinhas()) {
        	numeroNota = (BigDecimal) registro.getCampo("NUMNOTA");
        }
        
        Integer auxNumNota = numeroNota.intValue();

        if (auxNumNota > 0) {
            String url   = this.urlApi + "/v2/snk/pagamentos/creditos";
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            IntegrationApi.sendHttp(url, ""+auxNumNota, "POST", "Bearer " + token);

            String mensagem = "Solicitacao enviada com sucesso!";
            contextoAcao.setMensagemRetorno(mensagem);
        }
    }
}
