package br.com.goup.snkcustomevents.ofx;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.JapeSessionContext;

import java.math.BigDecimal;

public class ConciliarPromessaPixAcao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try{
            if(contexto.getLinhas().length <= 0)
                contexto.mostraErro("Selecione um registro na grade.");
            BigDecimal nuImport = (BigDecimal) contexto.getLinhas()[0].getCampo("NUIMPORT");
            PromessaPix promessaPix = conciliar(nuImport);
            contexto.setMensagemRetorno("Processamento Finalizado<br>Baixados: "+ promessaPix.contadorBaixados+"<br>Conciliados: "+ promessaPix.contadorConciliados);
        }catch (Exception e){
            contexto.setMensagemRetorno(e.getMessage());
        }
    }

    private PromessaPix conciliar(BigDecimal nuImport) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try{
            hnd = JapeSession.open();
//            JapeSessionContext.putProperty("usuario_logado",BigDecimal.valueOf(187));
            PromessaPix promessaPix = new PromessaPix();
            promessaPix.conciliar(nuImport);
            return promessaPix;
        }catch (Exception e){
            e.printStackTrace();
            JapeSession.close(hnd);
            throw new Exception(e);
        }
    }
}
