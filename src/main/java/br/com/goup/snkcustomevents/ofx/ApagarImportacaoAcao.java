package br.com.goup.snkcustomevents.ofx;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;

public class ApagarImportacaoAcao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto){
        try{
            if(contexto.getLinhas().length <= 0)
                contexto.mostraErro("Selecione um registro na grade.");

            BigDecimal nuImport = (BigDecimal) contexto.getLinhas()[0].getCampo("NUIMPORT");

            JapeFactory.dao(DynamicEntityNames.EXTRATO_BANCARIO)
                    .deleteByCriteria(" NUIMPORT = ? ",nuImport);

            JapeFactory.dao(DynamicEntityNames.IMPORTACAO_EXTRATO_BANCARIO)
                    .deleteByCriteria(" NUIMPORT = ? ",nuImport);

            contexto.setMensagemRetorno("Processamento Finalizado. ");

        }catch (Exception e){
            contexto.setMensagemRetorno(e.getMessage());
        }
    }
}
