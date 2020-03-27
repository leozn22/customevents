package br.com.goup.snkcustomevents.financial;

import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;

public class UpdateSql extends SnkIntegrationsApi implements EventoProgramavelJava{

	//private int codUsuarioIntegracaoHomologacao = 0;
	//private int codUsuarioIntegracaoProducao = 0;
	
	public UpdateSql() {
		this.forceUrl("ProductionTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}

	private String gerarJson(PersistenceEvent persistenceEvent) {

		DynamicVO financialVO 		  = (DynamicVO) persistenceEvent.getVo();
		ModifingFields modifingFields = persistenceEvent.getModifingFields();

		String json = "{"
				+ "\"financialId\": " + financialVO.asBigDecimal("NUFIN").toString() + ","
				+ "\"companyId\": " + financialVO.asBigDecimal("CODEMP").toString() + ","
				+ "\"noteId\": " + financialVO.asBigDecimal("NUNOTA").toString() + ","
				+ "\"noteNumber\": " + financialVO.asBigDecimal("NUMNOTA").toString() + ","
				+ "\"usuarioBaixaId\": " + (modifingFields.isModifing("CODUSUBAIXA")
											? modifingFields.getNewValue("CODUSUBAIXA").toString()
											: financialVO.asBigDecimal("CODUSUBAIXA").toString()) + ","

				+ "\"typeTitleId\": " + (modifingFields.isModifing("CODTIPTIT")
										? modifingFields.getNewValue("CODTIPTIT").toString()
										: financialVO.asBigDecimal("CODTIPTIT").toString()) + ","

				+ "\"paymentDate\": \"" + (modifingFields.isModifing("DHBAIXA")
										? modifingFields.getNewValue("DHBAIXA").toString()
										: financialVO.asBigDecimal("DHBAIXA").toString()) + "\","

				+ "\"paidValue\": \"" + (modifingFields.isModifing("VLRBAIXA")
										? modifingFields.getNewValue("VLRBAIXA").toString()
										: financialVO.asBigDecimal("VLRBAIXA").toString()) + "\","

				+ "\"receiptCard\": \"" +  (financialVO.getProperty("RECEBCARTAO") != null ? financialVO.asString("RECEBCARTAO") : "") + "\" ,"
				+ "\"deadline\": \"" + (modifingFields.isModifing("DTPRAZO")
						? modifingFields.getNewValue("DTPRAZO").toString()
						: financialVO.getProperty("DTPRAZO")) + "\" "
				+ "}";

		return json;

	}

	private void financial(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO financialVO = (DynamicVO) persistenceEvent.getVo();
		
		//int codUsuarioBaixa = Integer.parseInt(financialVO.getProperty("CODUSUBAIXA").toString());
		
		//verifica se o usu�rio que est� fazendo a modifica��o � o usu�rio da integra��o
		//Se for n�o atualiza o financeiro nos bancos de dados
		//if(codUsuarioBaixa != codUsuarioIntegracaoHomologacao)
		//{
			ModifingFields modifingFields = persistenceEvent.getModifingFields();
			if(modifingFields.isModifing("DHBAIXA")
					&& modifingFields.getNewValue("DHBAIXA") != null
					&& financialVO.asInt("RECDESP") == 1
					&& financialVO.asInt("CODTIPOPER") == 3117)
			{
//				String url  = this.urlApi+"/financial/sankhya/"+financialVO.asBigDecimal("NUFIN").toString() + "/" + modifingFields.getNewValue("CODUSUBAIXA").toString();
				String json = this.gerarJson(persistenceEvent);
				String url  = this.urlApi+"/financial/sankhya";

				IntegrationApi.send(url, json, "POST");

//				int negotiationType = this.getNegotiationType(persistenceEvent, financialVO.asInt("NUNOTA"));
//
//				if(negotiationType >= 200 && negotiationType <= 213)
//				{
//					String url = this.urlApi+"/financial/sankhya/"+financialVO.asBigDecimal("NUFIN").toString();
//					IntegrationApi.send(url, "", "POST");
//				}
			}
		//}//Fim IF usuarioIntegracao
	}
	
	private int getNegotiationType(PersistenceEvent persistenceEvent, int nunota) throws Exception
	{
		int negotiationType = 0;
	
		JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
		
		NativeSql sql = new NativeSql(jdbc);
		
		StringBuffer consulta = new StringBuffer();
		
		consulta.append("SELECT CAB.CODTIPVENDA");
		consulta.append(" FROM TGFCAB CAB");
		consulta.append(" WHERE CAB.NUNOTA = :NUNOTA");
		sql.setNamedParameter("NUNOTA", nunota);
		ResultSet result = sql.executeQuery(consulta.toString());

		if (result.next()) {
			negotiationType = result.getInt("CODTIPVENDA");
		}
		
		return negotiationType;
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
//		financial(arg0);
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		financial(arg0);
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