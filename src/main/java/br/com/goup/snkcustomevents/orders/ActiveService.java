package br.com.goup.snkcustomevents.orders;


import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class ActiveService implements EventoProgramavelJava {

	public void verifyIfNeedToActiveServicesByProduct(PersistenceEvent persistenceEvent) throws Exception {			
		
		ModifingFields modifingFields = persistenceEvent.getModifingFields();
		DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

//		if (cabVO.asString("STATUSNOTA").equals("L"))
		if (modifingFields.getOldValue("CODTIPOPER") != null)
		throw new Exception(cabVO.asString("STATUSNOTA") + " " + modifingFields.getOldValue("CODTIPOPER")  + " " + modifingFields.getNewValue("CODTIPOPER"));
//		if (modifingFields.isModifing("STATUSNOTA")) {
//		
//		throw new Exception("Valor CODTIPOPER : " + modifingFields.getNewValue("CODTIPOPER") 
//			+ "Valor STATUSNOTA old : " + modifingFields.getOldValue("STATUSNOTA") 
//			+ "Valor STATUSNOTA new : " + modifingFields.getNewValue("STATUSNOTA") 
//			+ "Valor NUNOTA new : " + modifingFields.getNewValue("NUNOTA"));
//		}
		
//        if ((modifingFields.getNewValue("CODTIPOPER").equals("2100")) && 
//        	(!modifingFields.getOldValue("STATUSNOTA").equals("L")) && 
//        	(modifingFields.getNewValue("STATUSNOTA").equals("L")))  {
//        	String url = "";
//    		url = "http://snk-integrations-api-dev.sa-east-1.elasticbeanstalk.com:8080/api/"; // PRODUÇÃO
//    		url = "http://127.0.0.1:8080/api/service/products/active/" + modifingFields.getNewValue("NUNOTA");                              // TESTE LOCAL
////    		url = "http://app.zapgrafica.com.br:8080/api/register/partners";                  // HOMOLOGACAO
//        	IntegrationApi.send(url, "", "GET");        	
//        }	
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
		verifyIfNeedToActiveServicesByProduct(arg0);
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
