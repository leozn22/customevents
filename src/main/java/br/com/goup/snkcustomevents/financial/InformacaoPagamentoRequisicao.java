package br.com.goup.snkcustomevents.financial;


import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class InformacaoPagamentoRequisicao {

    private void enviarSite(String tipoPagamento, String codigoPedido) throws IOException {
        String url =  "https://www.zapgrafica.com.br/loja/pagamento/PagamentoRealizado";

//        tipoPagamento: pix, deposito, boleto, transferencia

        String json = "{\"tipoPagamento\": \"" + tipoPagamento + "\", \"codigoPedidoSnk\": \"" + codigoPedido + "\" }";

        HttpPost request = new HttpPost(url);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");

        request.setEntity(new StringEntity(json));

        HttpClient httpClient = HttpClientBuilder.create().build();

        httpClient.execute(request);
    }

    public void informarPagamentoSite(String tipoPagamento, String codigoPedido) throws IOException {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    enviarSite(tipoPagamento, codigoPedido);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

}
