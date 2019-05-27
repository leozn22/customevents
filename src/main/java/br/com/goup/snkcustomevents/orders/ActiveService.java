package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;


public class ActiveService extends SnkIntegrationsApi implements EventoProgramavelJava,AcaoRotinaJava  {

	public void verifyIfNeedToActiveServicesByProduct(PersistenceEvent persistenceEvent) throws Exception {			
		
		//ModifingFields modifingFields = persistenceEvent.getModifingFields();
		DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
		
		String tipoOperacao = "";
		String statusNota = "";
		
		if(cabVO.containsProperty("CODTIPOPER")) {
			tipoOperacao = cabVO.getProperty("CODTIPOPER").toString();
		}
		if(cabVO.containsProperty("STATUSNOTA")) { 	
			statusNota = cabVO.getProperty("STATUSNOTA").toString();
		}
		
		if(statusNota.equals("L") && tipoOperacao.equals("2100")) {
			String nunota = cabVO.getProperty("NUNOTA").toString();		
			String url = urlApi+"/service/products/active/"+nunota;
			IntegrationApi.send(url,"", "POST");
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

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {
		Registro[] linhas = contexto.getLinhas();
		Registro linha = linhas[0];
		
		String tipoOperacao = linha.getCampo("CODTIPOPER").toString();
		String statusNota = linha.getCampo("STATUSNOTA").toString();
		String nunota = linha.getCampo("NUNOTA").toString();
		if(statusNota.equals("L") && tipoOperacao.equals("2100")) {
			String url = urlApi+"/service/products/active/"+nunota;
			IntegrationApi.send(url,"", "POST");
			String mensagem = "Solicitacao de atualizacao de estoque do site enviada com sucesso!";
			//String mensagemUTF8 = new String(mensagem.getBytes(Charset.forName("UTF-8")));
			contexto.setMensagemRetorno(mensagem);
			
		}
		else
		{
			String mensagem = "Para ativar o servico no site o tipo de operacao precisa ser 2100 e a nota precisa estar confirmada!";
			//String mensagemUTF8 = new String(mensagem.getBytes(Charset.forName("UTF-8")));
			contexto.setMensagemRetorno(mensagem);
		}
	}
}
