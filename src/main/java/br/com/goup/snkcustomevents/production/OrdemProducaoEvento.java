package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.production.helpers.MovimentacaoSetorItemHelper;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Tabela: TPRIPROC - (Dicionario de Dados)
 * Instância: CabecalhoInstanciaProcesso
 *
 * (EVENTO)
 * Descrição: Atualização do setor dos itens Produzidos
 */
public class OrdemProducaoEvento  implements EventoProgramavelJava  {

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
        DynamicVO cabecalhoOP = (DynamicVO) persistenceEvent.getVo();

        if ("F".equals(cabecalhoOP.asString("STATUSPROC")) || "C".equals(cabecalhoOP.asString("STATUSPROC"))) {
            MovimentacaoSetorItemHelper movimentacao = new MovimentacaoSetorItemHelper(persistenceEvent.getJdbcWrapper());
            movimentacao.atualizarStatusOrdemProducao(cabecalhoOP.asBigDecimal("IDIPROC"), cabecalhoOP.asString("STATUSPROC"));
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
