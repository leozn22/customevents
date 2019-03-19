package br.com.goup.snkcustomevents.financial;

import java.math.BigDecimal;
import java.sql.ResultSet;

import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.goup.snkcustomevents.utils.IntegrationApi;

public class UpdateSql implements EventoProgramavelJava{
	
	private String urlTeste 	  = "http://127.0.0.1:8080/api/financial/sankhya/";
	private String urlHomologacao = "http://app.zapgrafica.com.br:8081/api/financial/sankhya/";
	private String urlProducao 	  = "http://snk-integrations-api-dev.sa-east-1.elasticbeanstalk.com:8080/api/financial/sankhya/";
	private int codUsuarioIntegracaoHomologacao = 0;
	private int codUsuarioIntegracaoProducao = 0;
	private void financial(PersistenceEvent persistenceEvent) throws Exception 
	{
		DynamicVO financialVO = (DynamicVO) persistenceEvent.getVo();
		
		int codUsuarioBaixa = Integer.parseInt(financialVO.getProperty("CODUSUBAIXA").toString());
		
		//verifica se o usuário que está fazendo a modificação é o usuário da integração
		//Se for não atualiza o financeiro nos bancos de dados
		//if(codUsuarioBaixa != codUsuarioIntegracaoHomologacao)
		//{
			if(persistenceEvent.getEntityProperty("DHBAIXA") != null && financialVO.asInt("RECDESP")==1)
			{
				int negotiationType = this.getNegotiationType(persistenceEvent, financialVO.asInt("NUNOTA"));
				
				if(negotiationType >= 200 && negotiationType <= 213)
				{
					String url = urlHomologacao+financialVO.asBigDecimal("NUFIN").toString();
					IntegrationApi.send(url, "", "POST");
				}
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