package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import java.math.BigDecimal;

/**
 * Tabela: TGFCAB - (Dicionário de Dados)
 * Descrição: Alterar tipo de entrega
 *
 * Descrição: Tipo Entrega
 * Nome: CODTETALTER
 * Tipo de parâmetro: Pesquisa Instância: TxpZapTipoEntrega
 * Obrigatório: true
 */
public class TipoEntregaAcao extends SnkIntegrationsApi implements AcaoRotinaJava {

    public TipoEntregaAcao() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        final Registro[] linhas = contextoAcao.getLinhas();

        if (linhas == null || linhas.length == 0) {
            contextoAcao.mostraErro("Registro n\u00e3o selecionado!");
            return;
        }

        JapeWrapper tipoEntregaDAO;
        try {
            tipoEntregaDAO = JapeFactory.dao("TxpZapTipoEntrega");
        } catch (Exception e) {
            throw new Exception("Inst\u00E2ncia Tipo de Entrega");
        }

        for (Registro linha: linhas) {
            BigDecimal pedido = (BigDecimal) linha.getCampo("NUMNOTA");
            BigDecimal tipoEntregaOld = (BigDecimal) linha.getCampo("TZACODTET");
            DynamicVO tetVO;
            try {
                tetVO = tipoEntregaDAO.findByPK(tipoEntregaOld);
            } catch (Exception e) {
                throw new Exception("Find Tipo de Entrega " + tipoEntregaOld);
            }

            contextoAcao.confirmar("Tipo de Entrega a Ser Alterado: " + tipoEntregaOld,
                    "Deseja Prosseguir com a Altera\u00E7\u00E3o? " + tetVO.asString("NOME"), 1);

            BigDecimal tipoEntregaNew = new BigDecimal(contextoAcao.getParam("CODTETALTER").toString());

            QueryExecutor query = contextoAcao.getQuery();
            query.setParam("P_CODTET", tipoEntregaNew);
            query.setParam("P_NUMNOTA", pedido);

            try {
                query.update("UPDATE TGFCAB SET TZACODTET = {P_CODTET} WHERE NUMNOTA = {P_NUMNOTA}");
            } catch (Exception e) {
                e.printStackTrace();
            }
            query.close();

            query = contextoAcao.getQuery();
            query.setParam("P_CODTET", tipoEntregaNew);
            query.setParam("P_NUMNOTA", pedido);

            try {
                query.update("UPDATE TZAPCT SET CODTET = {P_CODTET} WHERE NUMNOTA = {P_NUMNOTA}");
            } catch (Exception e) {
                e.printStackTrace();
            }
            query.close();

            String json = "{ " +
                    "'codigoPedido': " + pedido + ", " +
                    "'codigoTipoEntrega': " + tipoEntregaNew + ", " +
                    "'codigoUsuario': " + AuthenticationInfo.getCurrent().getUserID().intValue() + ", " +
                    "'nomeUsuario': " + AuthenticationInfo.getCurrent().getName() + " " +
                    "}";

            String url = this.urlApi + "/v2/snk/pedidos/ajustetipoentrega";
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
        }
        String mensagem = "Solicitacao enviada com sucesso!";
        contextoAcao.setMensagemRetorno(mensagem);
    }
}
