package br.com.goup.snkcustomevents.notas;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;

import java.math.BigDecimal;

public class RefazerFinanceiroAcao implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        final Registro[] linhas = contextoAcao.getLinhas();

        if (linhas == null || linhas.length == 0) {
            contextoAcao.mostraErro("Registro n\u00e3o selecionado!");
            return;
        }
        int ok = 0;
        int erro = 0;
        for (Registro linha: linhas) {
            try {
                CentralFinanceiro financeiroUtils = new CentralFinanceiro();
                financeiroUtils.inicializaNota((BigDecimal) linha.getCampo("NUNOTA"));
                financeiroUtils.refazerFinanceiro();
                ok++;
            } catch (Exception e) {
                erro++;
            }
        }

        String msg = ok + " financeiros regerados com sucesso!";
        if (erro > 0)
            msg += " " + erro + " com erro!";

        contextoAcao.setMensagemRetorno(msg);
    }
}
