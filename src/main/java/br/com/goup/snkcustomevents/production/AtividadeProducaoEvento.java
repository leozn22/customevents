package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.production.helpers.MovimentacaoSetorItemHelper;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Tabela: TPRIATV - (Dicionario de Dados)
 * Instância: InstanciaAtividade
 *
 * (EVENTO)
 * Descrição: Atualização do setor dos itens Produzidos
 */
public class AtividadeProducaoEvento implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

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
        MovimentacaoSetorItemHelper movimentacao = new MovimentacaoSetorItemHelper(persistenceEvent.getJdbcWrapper());
        movimentacao.atualizarSetorProducaoAtividade((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
