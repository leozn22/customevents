package br.com.goup.snkcustomevents.events;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class StockControlRawMaterialEvent implements EventoProgramavelJava{

    /**
     * Verify if item has stock available on company without check local of stock
//     * @param Context persistence instance of TGFITE
     * @throws Exception in case of stock less than or equal to zero
     */
    private void verifyStock(PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO iteVO = (DynamicVO) persistenceEvent.getVo();

        //JapeWrapper topDAO = JapeFactory.dao("TipoOperacao");
        JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");

        DynamicVO cabVO = cabDAO.findByPK(iteVO.asBigDecimal("NUNOTA"));
        //DynamicVO topVO = topDAO.findByPK(cabVO.asBigDecimal("CODTIPOPER"),cabVO.asTimestamp("DHTIPOPER"));

        if("S".equals(cabVO.asString("TipoOperacao.AD_VALIDAESTOQUE"))) {

            JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT SUM(ESTOQUE - RESERVADO) as ESTOQUE, (SUM(ESTOQUE - RESERVADO) - :QTDNEG) as DISPONIVEL " +
                    " FROM TGFEST WHERE CODPROD = :PRODUTO AND CODEMP = :EMPRESA AND CODLOCAL = :CODLOCAL");
            sql.setNamedParameter("PRODUTO", iteVO.asBigDecimal("CODPROD"));
            sql.setNamedParameter("EMPRESA", iteVO.asBigDecimal("CODEMP"));
            sql.setNamedParameter("QTDNEG", iteVO.asBigDecimal("QTDNEG"));
            sql.setNamedParameter("CODLOCAL", iteVO.asBigDecimal("CODLOCALORIG"));

            ResultSet r1 = sql.executeQuery();
            try {
                if (r1.next()) {
                    BigDecimal estoque = (r1.getBigDecimal("ESTOQUE") == null ? BigDecimal.ZERO : r1.getBigDecimal("ESTOQUE"));
                    BigDecimal disponivel = (r1.getBigDecimal("DISPONIVEL") == null ? BigDecimal.valueOf(-1) : r1.getBigDecimal("DISPONIVEL"));

                    if (disponivel.compareTo(BigDecimal.ZERO) < 0) {
                        throw new Exception("<font size='12'><b> Estoque insuficiente para atender a solicita????o. <br> Dispon??vel: "
                                + estoque + " para o local " + iteVO.asBigDecimal("CODLOCALORIG") + "<br><br> Favor verificar se h?? quantidade no local ou empresa solicitados. </b></font>");
                    }
                }
            } finally {
                r1.getStatement().close();
            }
        }
    }       
	
	@Override
	public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub	
	}

	@Override
	public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub	
	}

	@Override
	public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeCommit(TransactionContext persistenceEvent) throws Exception {
		// TODO Auto-generated method stub		
	}

	@Override
	public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
		verifyStock(persistenceEvent);		
	}

	@Override
	public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
		verifyStock(persistenceEvent);		
	}

}
