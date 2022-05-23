package br.com.goup.snkcustomevents.notas;

import br.com.goup.snkcustomevents.Enumerator.IdTipoOperacaoPedidoVenda;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

public class CteImportacaoEvento implements EventoProgramavelJava {

    public void verificarDocumento(PersistenceEvent persistenceEvent) {
        try {
            DynamicVO cabecalhoVO = (DynamicVO) persistenceEvent.getVo();

            if (cabecalhoVO.asInt("CODTIPOPER") == IdTipoOperacaoPedidoVenda.TOP_AQUISICAO_FRETE_CTE.getValue() &&
                cabecalhoVO.asTimestamp("DTNEG") != null) {
                cabecalhoVO.setProperty("DTMOV", cabecalhoVO.asTimestamp("DTNEG"));
                cabecalhoVO.setProperty("DTENTSAI", cabecalhoVO.asTimestamp("DTNEG"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.verificarDocumento(persistenceEvent);
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
