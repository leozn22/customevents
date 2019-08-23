package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.sql.ResultSet;


public class ActiveService extends SnkIntegrationsApi implements EventoProgramavelJava, AcaoRotinaJava  {

	public void ativarServicosSite(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO iteVO = (DynamicVO) persistenceEvent.getVo();

		if (iteVO.asString("USOPROD").toUpperCase().equals("M")
				&& iteVO.asBigDecimal("ATUALESTOQUE").compareTo(BigDecimal.valueOf(1)) == 0) {
			String url = urlApi + "/service/products/active/" + iteVO.asBigDecimal("NUNOTA");
			IntegrationApi.send(url, "", "POST");
		}
	}

	public void verifyIfNeedToActiveServicesByProduct(PersistenceEvent persistenceEvent, boolean insert) throws Exception {
		
		//ModifingFields modifingFields = persistenceEvent.getModifingFields();
		DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
		Boolean confirmando = (Boolean) JapeSession.getProperty("CabecalhoNota.confirmando.nota",false);

		if( ( confirmando || "L".equals(cabVO.asString("STATUSNOTA")) || insert)
				&& cabVO.asString("TIPMOV").toUpperCase().equals("C")) {

			JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
			NativeSql sql = new NativeSql(jdbc);
			StringBuffer consulta = new StringBuffer();

			consulta.append("    SELECT SEQUENCIA");
			consulta.append("      FROM TGFITE ");
			consulta.append("     WHERE NUNOTA = :NUNOTA");
			consulta.append("       AND USOPROD = 'M'");
//			consulta.append("       AND ATUALESTOQUE = 1");

			sql.setNamedParameter("NUNOTA", cabVO.asBigDecimal("NUNOTA"));
			ResultSet result = sql.executeQuery(consulta.toString());
			if (true) {
				throw new Exception("Nota: " + cabVO.asBigDecimal("NUNOTA") + "\n" + " confirmando: "
						+ confirmando + " Status: " + cabVO.asString("STATUSNOTA")
				+ " result: "  + result.next());
			}

			if (result.next()) {
				String nunota = cabVO.getProperty("NUNOTA").toString();
				String url = urlApi + "/service/products/active/" + nunota;
				IntegrationApi.send(url, "", "POST");
			}
		}
	}

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		ativarServicosSite(arg0);
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		ModifingFields modifingFields = arg0.getModifingFields();
		if (modifingFields.isModifing("ATUALESTOQUE")) {
			ativarServicosSite(arg0);
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
