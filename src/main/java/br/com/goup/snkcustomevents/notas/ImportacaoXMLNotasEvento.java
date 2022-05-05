package br.com.goup.snkcustomevents.notas;

import br.com.goup.snkcustomevents.domain.xml.*;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

public class ImportacaoXMLNotasEvento implements EventoProgramavelJava {
    /*
       Tabela: TGFIXN - (Construtor de tela)
       Instância: ImportacaoXMLNotas

       (EVENTO)
       Descrição: AJUSTE IMPORTAÇÃO XML
    */

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO dadosVO = (DynamicVO) persistenceEvent.getVo();

        //TIPIMPCTE | T = Terceiro | E = Emissão Própria | D = Documento Anterior | C = Cancelado
        if ("E".equals(dadosVO.asString("TIPIMPCTE")) &&
                dadosVO.asInt("NUNOTA") > 0 &&
                dadosVO.asInt("AD_NUNOTATERC") == 0) {

            ImportacaoXmlNotaCompraZapHelper importacaoHelper = new ImportacaoXmlNotaCompraZapHelper();
            RetornoGeracaoCTe retornoGeracaoCTe = importacaoHelper.gerarNotaCompra(dadosVO);

            if (retornoGeracaoCTe.getNumeroNota().compareTo(BigDecimal.ZERO) == 1) {
                dadosVO.setProperty("AD_NUNOTATERC", retornoGeracaoCTe.getNumeroNota());
            }

            dadosVO.setProperty("AD_MSGIMPXMLTER", retornoGeracaoCTe.getMsg());
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
