package br.com.goup.snkcustomevents.financial;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;

public class UpdateTef extends SnkIntegrationsApi implements EventoProgramavelJava{
	
	public UpdateTef() {
		//this.forceUrl("ProductionTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}
	
	private void sendDataTef(PersistenceEvent persistenceEvent) throws Exception {
		DynamicVO tefVO = (DynamicVO) persistenceEvent.getVo();
		String url      = this.urlApi+"/financial/sankhya/"+tefVO.asBigDecimal("NUFIN").toString();
		IntegrationApi.send(url, "", "POST");
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		sendDataTef(arg0);
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		sendDataTef(arg0);
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
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
}