package br.com.goup.snkcustomevents.events;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class GridBlockEvent implements EventoProgramavelJava{
	
	private Boolean verifyGrid(PersistenceEvent persistenceEvent) throws Exception{
		
		DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
		
		JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
		
		NativeSql sql = new NativeSql(jdbc);
		
		StringBuffer consulta = new StringBuffer();
		
		consulta.append("    SELECT CAB.NUNOTA,");
		consulta.append("	        CAB.DTMOV");
		consulta.append("      FROM TGFCAB CAB ");
		consulta.append("INNER JOIN TGFTOP OPE");
		consulta.append("        ON OPE.CODTIPOPER = CAB.CODTIPOPER");
		consulta.append("       AND OPE.DHALTER = CAB.DHTIPOPER");
		consulta.append("     WHERE CAB.AD_GRADE = :GRADE");
		consulta.append("       AND nullValue(OPE.AD_VALIDAGRADE, 'N') = 'S'");
		
		
		sql.setNamedParameter("GRADE", cabVO.asBigDecimal("AD_GRADE"));
		ResultSet result = sql.executeQuery(consulta.toString());
		
		if (result.next()) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");						
			
			throw new Exception("<font size='12'><b>Já existe uma requisição de Nro. Único: " + result.getBigDecimal("NUNOTA") + " do dia " +  format.format(result.getDate("DTMOV")) + " para a grade informada!</b></font>");
		}
		
		return null;		
	}

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		verifyGrid(arg0);
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		verifyGrid(arg0);
		
	}
	
	

}
