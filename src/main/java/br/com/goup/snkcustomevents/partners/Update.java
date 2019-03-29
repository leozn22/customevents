package br.com.goup.snkcustomevents.partners;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.domain.Parceiro;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class Update extends SnkIntegrationsApi implements EventoProgramavelJava {
	
	public Update() {
		//this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}
	
	public void partner(PersistenceEvent persistenceEvent) throws Exception {

		Parceiro parceiro    = new Parceiro();
		DynamicVO parceiroVO = (DynamicVO) persistenceEvent.getVo();
		String json          = parceiro.getJsonUpdatePartner(parceiroVO);
		String url           = this.urlApi+"/register/partners";
		IntegrationApi.send(url, json, "PUT");
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
		partner(arg0);
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
