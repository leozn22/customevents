package br.com.goup.snkcustomevents.financial;

import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
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
		//this.forceUrl("ProductionTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}

	private void financial(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO financialVO = (DynamicVO) persistenceEvent.getVo();
		
		//int codUsuarioBaixa = Integer.parseInt(financialVO.getProperty("CODUSUBAIXA").toString());
		
		//verifica se o usu�rio que est� fazendo a modifica��o � o usu�rio da integra��o
		//Se for n�o atualiza o financeiro nos bancos de dados
		//if(codUsuarioBaixa != codUsuarioIntegracaoHomologacao)
		//{
			if(persistenceEvent.getEntityProperty("DHBAIXA") != null && financialVO.asInt("RECDESP")==1)
			{
				int negotiationType = this.getNegotiationType(persistenceEvent, financialVO.asInt("NUNOTA"));
				
				if(negotiationType >= 200 && negotiationType <= 213)
				{
					String url = this.urlApi+"/financial/sankhya/"+financialVO.asBigDecimal("NUFIN").toString();
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
		financial(arg0);
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