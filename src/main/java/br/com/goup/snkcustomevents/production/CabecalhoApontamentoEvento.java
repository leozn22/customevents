package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.production.helpers.MovimentacaoSetorItemHelper;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Tabela: TPRAPO - (Dicionario de Dados)
 * Instância: CabecalhoApontamento
 *
 * (EVENTO)
 * Descrição: Atualização do setor dos itens Produzidos
 */
public class CabecalhoApontamentoEvento implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO apontamentoCabecalho = (DynamicVO) persistenceEvent.getVo();

        if ("C".equals(apontamentoCabecalho.asString("SITUACAO"))) {
            MovimentacaoSetorItemHelper movimentacao = new MovimentacaoSetorItemHelper(persistenceEvent.getJdbcWrapper());
            movimentacao.atualizarSetorProducaoApontamento(apontamentoCabecalho.asBigDecimal("NUAPO"),
                                                           apontamentoCabecalho.asBigDecimal("IDIATV"));
        }
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
