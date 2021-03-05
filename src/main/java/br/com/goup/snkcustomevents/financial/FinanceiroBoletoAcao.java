package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;

import java.math.BigDecimal;

public class FinanceiroBoletoAcao extends SnkIntegrationsApi implements AcaoRotinaJava  {

    private void registrarBoleto(ContextoAcao contextoAcao, Registro registro) throws Exception {

        Integer numeroPedido = Integer.parseInt(registro.getCampo("NOSSONUM").toString());
        QueryExecutor tgfCab = contextoAcao.getQuery();

        StringBuffer consulta = new StringBuffer();

        consulta.append("    SELECT CAB.NUNOTA");
        consulta.append("      FROM TGFCAB CAB ");
        consulta.append("     WHERE CAB.NUMNOTA = " + numeroPedido.toString());
        consulta.append("       AND CAB.TIPMOV = 'P'");

       tgfCab.nativeSelect(consulta.toString());

        if (!tgfCab.next()) {
            throw new Exception("<font size='12'><b>Não existe pedido de venda com o Nro. Nota: " + numeroPedido + "!</b></font>");
        }

        if (!registro.getCampo("RECDESP").toString().equals("1")) {
            throw new Exception("<font size='12'><b>O registro não é uma receita!</b></font>");
        }

        JapeWrapper adFinDAO = JapeFactory.dao("AD_TGFFIN");

        FluidCreateVO creAdFIn = adFinDAO.create();
        creAdFIn.set("NUFIN", registro.getCampo("NUFIN"));
        creAdFIn.set("DHBAIXA", registro.getCampo("DHBAIXA"));
        creAdFIn.set("VLRBAIXA", registro.getCampo("VLRBAIXA"));
        creAdFIn.set("NOSSONUM", registro.getCampo("NOSSONUM"));
        creAdFIn.set("CODTIPTIT", registro.getCampo("CODTIPTIT"));
        creAdFIn.set("CODTIPOPER", new BigDecimal(3106));
        creAdFIn.set("CODEMP", registro.getCampo("CODEMP"));
        creAdFIn.set("CODCTABCOINT", registro.getCampo("CODCTABCOINT"));
        creAdFIn.save();

    }

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        for (Registro registro: contextoAcao.getLinhas()) {

            this.registrarBoleto(contextoAcao, registro);
            contextoAcao.setMensagemRetorno("Solicitacao enviada com sucesso!");
        }

    }
}
