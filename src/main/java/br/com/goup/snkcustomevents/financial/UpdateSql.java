package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class UpdateSql implements EventoProgramavelJava{
	
	private String urlHomologacao = "http://app.zapgrafica.com.br:8081/api/financial/sankhya/";
	private String urlProducao 	  = "http://snk-integrations-api-dev.sa-east-1.elasticbeanstalk.com:8080/api/financial/sankhya/";
	private String urlTeste 	  = "http://127.0.0.1:8080/api/financial/sankhya/"; 
	
	public void financial(PersistenceEvent persistenceEvent) throws Exception 
	{
		DynamicVO financialVO = (DynamicVO) persistenceEvent.getVo();
		
		if(!financialVO.asString("NUFIN").isEmpty()) 
		{
			if(financialVO.asString("DHBAIXA").isEmpty() && !financialVO.asString("DHBAIXA").isBlank())
			{
				String url = this.urlHomologacao+financialVO.asString("NUFIN");
				IntegrationApi.send(url, "", "POST");
			}
		}
		else
		{
			throw new Exception("NUMNOTA is empty");
		}
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
		//financial(arg0);
		System.out.println("Hello Custom Events");
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
