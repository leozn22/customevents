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
	
	public UpdateSql() {
		this.exigeAutenticacao = true;
		// QUANDO ALTERAR O PARÂMETRO ABAIXO, DEVE ALTERAR DA MESMA FORMA NOS ARQUIVOS: ItemAcao.java e UpdateTef.java
		this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
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

	private String gerarJsonDespesa(PersistenceEvent persistenceEvent) {

		DynamicVO financialVO 		  = (DynamicVO) persistenceEvent.getVo();

		String json = "{"
				+ "\"idFinanceiro\": " + financialVO.asBigDecimal("NUFIN").toString() + ","
				+ "\"idEmpresa\": " + financialVO.asBigDecimal("CODEMP").toString() + ","
				+ "\"idNota\": " + financialVO.asBigDecimal("NUNOTA") + ","
				+ "\"numeroNota\": " + financialVO.asBigDecimal("NUMNOTA").toString() + ","
				+ "\"idParceiro\": " + financialVO.asBigDecimal("CODPARC").toString() + ","
				+ "\"idTipoOperacao\": " + (financialVO.asBigDecimal("CODTIPOPER").intValue() == 4107 ? "4401" : financialVO.asBigDecimal("CODTIPOPER").toString()) + ","
				+ "\"idUsuarioBaixa\": " +  financialVO.asBigDecimal("CODUSU") + ","
				+ "\"idTipoTitulo\": " + "15" + ","
				+ "\"valorDesdobramento\": \"" + financialVO.asBigDecimal("VLRDESDOB").toString() + "\","
				+ "\"dataPrazo\": \"" + financialVO.getProperty("DTPRAZO") + "\" "
				+ "}";

		return json;
	}

	private String gerarJsonV2(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO financialVO 		  = (DynamicVO) persistenceEvent.getVo();
		ModifingFields modifingFields = persistenceEvent.getModifingFields();

		String json = "{"
				+ "\"idFinanceiro\": " + financialVO.asBigDecimal("NUFIN").toString() + ","
				+ "\"idEmpresa\": " + financialVO.asBigDecimal("CODEMP").toString() + ","
				+ "\"idNota\": " + financialVO.asBigDecimal("NUNOTA") + ","
				+ "\"numeroNota\": " + financialVO.asBigDecimal("NUMNOTA").toString() + ","
				+ "\"idParceiro\": " + financialVO.asBigDecimal("CODPARC").toString() + ","
				+ "\"idTipoOperacao\": " + (financialVO.asBigDecimal("CODTIPOPER").intValue() == 4104 ? "4401" : financialVO.asBigDecimal("CODTIPOPER").toString()) + ","
				+ "\"idUsuarioBaixa\": " + (modifingFields.isModifing("CODUSUBAIXA")
				? modifingFields.getNewValue("CODUSUBAIXA").toString()
				: financialVO.asBigDecimal("CODUSUBAIXA").toString()) + ","

				+ "\"idTipoTitulo\": " + (modifingFields.isModifing("CODTIPTIT")
				? modifingFields.getNewValue("CODTIPTIT").toString()
				: (modifingFields.isModifing("NUCOMPENS") && modifingFields.getNewValue("NUCOMPENS") != null
				? "26"
				: financialVO.asBigDecimal("CODTIPTIT").toString())) + ","

				+ "\"dataBaixa\": \"" + (modifingFields.isModifing("DHBAIXA")
				? modifingFields.getNewValue("DHBAIXA").toString()
				: financialVO.asBigDecimal("DHBAIXA").toString()) + "\","

				+ "\"valorBaixa\": \"" + (modifingFields.isModifing("VLRBAIXA")
				? modifingFields.getNewValue("VLRBAIXA").toString()
				: financialVO.asBigDecimal("VLRBAIXA").toString()) + "\","

				+ "\"valorDesdobramento\": \"" + (modifingFields.isModifing("VLRDESDOB")
				? modifingFields.getOldValue("VLRDESDOB").toString()
				: financialVO.asBigDecimal("VLRDESDOB").toString()) + "\","

				+ "\"recebimentoCartao\": \"" +  (financialVO.getProperty("RECEBCARTAO") != null ? financialVO.asString("RECEBCARTAO") : "") + "\" ,"
				+ "\"dataPrazo\": \"" + (modifingFields.isModifing("DTPRAZO")
				? modifingFields.getNewValue("DTPRAZO").toString()
				: financialVO.getProperty("DTPRAZO")) + "\" "
				+ "}";

		return json;

	}

	private void financial(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO financialVO = (DynamicVO) persistenceEvent.getVo();

		ModifingFields modifingFields = persistenceEvent.getModifingFields();
		if(modifingFields.isModifing("DHBAIXA")
				&& financialVO.asInt("RECDESP") == 1
				&& (financialVO.asInt("CODTIPTIT") == 2
					|| financialVO.asInt("CODTIPTIT") == 3
					|| financialVO.asInt("CODTIPTIT") == 4
					|| financialVO.asInt("CODTIPTIT") == 15
					|| financialVO.asInt("CODTIPTIT") == 26)
				&& (financialVO.asInt("CODTIPOPER") == 3117
					|| financialVO.asInt("CODTIPOPER") == 4401
					|| financialVO.asInt("CODTIPOPER") == 4104
					|| financialVO.asInt("CODTIPOPER") == 3107
				    || (financialVO.asInt("CODTIPOPER") == 3106
							&& (modifingFields.isModifing("CODCTABCOINT")
									? Integer.parseInt(modifingFields.getNewValue("CODCTABCOINT").toString())
									: financialVO.asInt("CODCTABCOINT")) == 21)))
		{
			if (modifingFields.getNewValue("DHBAIXA") != null) {
//			PROCESSO V1
//			String json = this.gerarJson(persistenceEvent);
//			String url  = this.urlApi+"/financial/sankhya";
//			IntegrationApi.send(url, json, "POST");

//			PROCESSO V2
				String json = this.gerarJsonV2(persistenceEvent);
				
//				if (true) {
//					throw new Exception(json);
//				}

				String url = this.urlApi + "/v2/caixas/pagamentos";
				String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
				IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
			} else {
				
//				if (true) {
//					throw new Exception(financialVO.asBigDecimal("NUFIN").toString());
//				}

				String url   = this.urlApi + "/v2/caixas/pagamentos/" + financialVO.asBigDecimal("NUFIN").toString();
				String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
				IntegrationApi.sendHttp(url, "{\"idUsuario\":" + financialVO.asBigDecimal("CODUSU").toString() + "}", "DELETE", "Bearer " + token);
			}
		}

		if (financialVO.asInt("RECDESP") == -1
				&& financialVO.asInt("CODTIPOPER") == 4107
				&& persistenceEvent.getModifingFields().isModifing("CODPARC")
				&& ((
				persistenceEvent.getModifingFields().getOldValue("CODPARC").toString().equals("638")
						&& !persistenceEvent.getModifingFields().getNewValue("CODPARC").toString().equals("638")
		)
				||
				(
						persistenceEvent.getModifingFields().getOldValue("CODPARC").toString().equals("656")
								&& !persistenceEvent.getModifingFields().getNewValue("CODPARC").toString().equals("656"))
		)
		) {

			String json = this.gerarJsonDespesa(persistenceEvent);
			
//			if (true) {
//				throw new Exception(json);
//			}

			String url = this.urlApi + "/v2/caixas/pagamentos";
			String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
			IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
		}
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