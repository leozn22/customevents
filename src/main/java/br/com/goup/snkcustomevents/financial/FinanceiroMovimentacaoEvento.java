package br.com.goup.snkcustomevents.financial;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class FinanceiroMovimentacaoEvento implements EventoProgramavelJava {


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

        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        if(modifingFields.isModifing("DHBAIXA") && modifingFields.getNewValue("DHBAIXA") != null) {

            DynamicVO financeiroVo = (DynamicVO) persistenceEvent.getVo();

            if (financeiroVo.asInt("CODTIPOPER") == 3106 && financeiroVo.asInt("CODTIPTIT") == 15 && financeiroVo.getProperty("BH_VLRDEPOSITO") == null) {
                throw new Exception("Não é permitido baixar depósito sem dados da promessa!");
            }
        }

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
