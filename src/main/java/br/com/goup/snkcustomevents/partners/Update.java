package br.com.goup.snkcustomevents.partners;

import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.google.gson.Gson;

public class Update implements EventoProgramavelJava {
	
	public void partner(PersistenceEvent persistenceEvent) throws Exception {
		
		// TODO identificar a porta da URL automaticamente
		String url           = "";
		Gson gson            = new Gson();
		DynamicVO parceiroVO = (DynamicVO) persistenceEvent.getVo();
		String json          = gson.toJson(parceiroVO);

		// PRODUÇÃO
		url = "http://snk-integrations-api-dev.sa-east-1.elasticbeanstalk.com:8080/api/";
		// TESTE: Para colocar em desenvolvimento, descomente a linha abaixo.
		url = "http://127.0.0.1:8080/api/register/partners";
		url = "http://app.zapgrafica.com.br:8080/api/register/partners"; // Máquina Camilo
		
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
		// TODO Auto-generated method stub
		try {
			partner(arg0);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
