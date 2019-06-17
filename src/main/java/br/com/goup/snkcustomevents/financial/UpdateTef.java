package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class UpdateTef extends SnkIntegrationsApi implements EventoProgramavelJava{
	
	public UpdateTef() {
		//this.forceUrl("ProductionTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}
	
	private void sendDataTef(PersistenceEvent persistenceEvent) throws Exception {
		
		try {
			DynamicVO tefVO   = (DynamicVO) persistenceEvent.getVo();
			String confirmado = "N";
			
			if(tefVO.asString("CONFIRMADO") != null && !tefVO.asString("CONFIRMADO").isEmpty()) { confirmado = tefVO.asString("CONFIRMADO"); }
			
			if(confirmado.equals("S")) {
				String url = this.urlApi+"/financial/sankhya/tef/"+tefVO.asBigDecimal("NUFIN").toString();
				IntegrationApi.send(url, "", "POST");
			}
		}
		catch(Exception e) {
			throw new Exception("Mensagem de erro: "+e.getMessage());
		}
	}
	
	public void actionSendDataTef() throws Exception {
		
		String url = this.urlApi+"/financial/sankhya/tef/sincronize";
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