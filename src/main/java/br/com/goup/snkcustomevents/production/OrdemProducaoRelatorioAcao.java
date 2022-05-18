package br.com.goup.snkcustomevents.production;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.motazan.model.helper.ImpressaoHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabela: TPRIPROC - (Dicionário de Dados)
 * Descrição: Relatório de Produção
 * Parâmetros: (0)
 */
public class OrdemProducaoRelatorioAcao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        if (ctx.getLinhas() == null || ctx.getLinhas().length == 0) {
            ctx.mostraErro("Registro n\u00e3o selecionado!");
        }

        for (Registro registro: ctx.getLinhas()) {
            Map<String,Object> pk = new HashMap<String, Object>();
            pk.put("IDIPROC", registro.getCampo("IDIPROC"));

            ImpressaoHelper.imprimirRelatorio(BigDecimal.valueOf(101), pk, null);
        }
    }
}
