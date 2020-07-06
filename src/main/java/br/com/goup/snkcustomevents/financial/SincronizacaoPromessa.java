package br.com.goup.snkcustomevents.financial;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class SincronizacaoPromessa extends SnkIntegrationsApi implements EventoProgramavelJava{
	
	public SincronizacaoPromessa() {
		this.exigeAutenticacao = true;
		this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}
	
	private void enviarDados(PersistenceEvent persistenceEvent) throws Exception {
		
		try {
			DynamicVO dynVO     = (DynamicVO) persistenceEvent.getVo();
			String sincronizado = "N";
			
			try {
				sincronizado = dynVO.asString("AD_TZACONF");
			} 
			catch (Exception e) { sincronizado = "N"; }
			
			if(sincronizado != null && !sincronizado.equals("S")) {
				this.validarParametros(dynVO);
				String acao = this.validarAcao(dynVO);
				
				if(acao.isEmpty()) {
					System.out.println("Não foi possível identificar a ação de sincronização desejada!");
				}

				this.executarAcao(acao, persistenceEvent);	
			}
		}
		catch(Exception e) {
			throw new Exception("Mensagem de erro: "+e.getMessage());
		}
	}
	
	private void validarParametros(DynamicVO dynVO) throws Exception {
		if(dynVO.asString("STATUSPEDIDO") == null || dynVO.asString("STATUSPEDIDO").isEmpty()) {
			throw new Exception("O status do pedido está vazio!");
		}

		if(dynVO.asString("STATUSPROMESSA") == null || dynVO.asString("STATUSPROMESSA").isEmpty()) {
			throw new Exception("O status da promessa está vazio!");
		}
		
		if(dynVO.asBigDecimal("NUNOTA") == null || dynVO.asBigDecimal("NUNOTA").compareTo(BigDecimal.ZERO) <= 0) {
			throw new Exception("Número único inválido!");
		}
	}
	
	private String validarAcao(DynamicVO dynVO) throws Exception {
		String acao              = "";
		try {

			String numeroDeposito    = dynVO.asString("NRODESPOSITO");
			boolean temDeposito      = (numeroDeposito != null && !numeroDeposito.equals("") && !numeroDeposito.equals("null")) ? true : false;
			BigDecimal valorDeposito = dynVO.asBigDecimal("VALORDEPOSITO");
			Integer numeroAcerto     = dynVO.asInt("NUACERTO");
			boolean compensado       = (numeroAcerto != null && numeroAcerto > 0) ? true : false;

			if (
					dynVO.asString("STATUSPEDIDO").equals("LI")
					&& (
							dynVO.asString("STATUSPROMESSA").equals("CO") 
							|| (
								dynVO.asString("STATUSPROMESSA").equals("PE")
								&& valorDeposito != null
								&& valorDeposito.compareTo(BigDecimal.ZERO) > 0
							)
						)
					&& (temDeposito || compensado)) {
				acao = "ConfirmarDeposito";
			}

			if (dynVO.asString("STATUSPEDIDO").equals("CA") && dynVO.asString("STATUSPROMESSA").equals("CA")) {
				acao = "CancelarDeposito";
			}

			if (dynVO.asString("STATUSPEDIDO").equals("PE") && dynVO.asString("STATUSPROMESSA").equals("NE")) {
				acao = "NegarDeposito";
			}

			// Na tela de promessa antiga liberava o pedido se o status da promessa estivesse pendente, clicando no botão Liberar Pedido.
			// Agora, o nome do botão é Liberar Boleto, só que o status que chega é confirmado, como na confirmação de depósito, a diferença
			// é que no caso do boleto não tem o número do depósito.
			if (dynVO.asString("STATUSPEDIDO").equals("LI") && dynVO.asString("STATUSPROMESSA").equals("CO") && !temDeposito && !compensado) {
				acao = "LiberarPedido";
			}

//			if(true) {
//				String msg = "GERAL\\n - Ação "+acao+" \\n - STATUSPEDIDO: "+dynVO.asString("STATUSPEDIDO")+"\\n - STATUSPROMESSA: "+dynVO.asString("STATUSPROMESSA")+"\\n - NRODESPOSITO: "+numeroDeposito+"\\n - temDeposito: "+temDeposito;
//				throw new Exception(msg+"\n - dynVO: "+dynVO);
//			}
		} catch (Exception e) {
			throw new Exception("Falha ao definir acao\n" + e.getMessage());
		}
		
		return acao;
	}
	
	private boolean executarAcao(String acao, PersistenceEvent persistenceEvent) throws Exception {
		boolean retorno = false;
		String url      = "";
		String json     = "";
		String metodo   = "POST";

		switch (acao) {
			case "ConfirmarDeposito":
				metodo = "PUT";
				json   = this.getJsonDeposito(persistenceEvent, "");
				url    = this.urlApi+"/v2/promessas/deposito";
				break;

			case "CancelarDeposito":
				metodo = "DELETE";
				json   = this.getJsonDeposito(persistenceEvent, "CANCELADO");
				url    = this.urlApi+"/v2/promessas/deposito";
				break;

			case "NegarDeposito":
				metodo = "DELETE";
				json   = this.getJsonDeposito(persistenceEvent, "NEGADO");
				url    = this.urlApi+"/v2/promessas/deposito";
				break;

			case "LiberarPedido":
				metodo = "PUT";
				json   = this.getJsonDeposito(persistenceEvent, "");
				url    = this.urlApi+"/v2/promessas/pedido";
				break;
	
			default:
				retorno = false;
				break;
		}
		
		if(!url.isEmpty()) {
			String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
			IntegrationApi.sendHttp(url, json, metodo, "Bearer " + token);
			retorno = true;
			//throw new Exception(token);
			//throw new Exception("url: "+url+" - json: "+json+" - metodo: "+metodo+" - Autenticacao: Bearer " + token);
		}
		
		return retorno;
	}
	
	private String getJsonDeposito(PersistenceEvent persistenceEvent, String situacaoComprovante) throws Exception {

		try {
			DynamicVO dynVO   = (DynamicVO) persistenceEvent.getVo();
			BigDecimal nunota = dynVO.asBigDecimal("NUNOTA");
			String retorno    = "{}";
			String sql        = "" +
					"SELECT " + 
					"    C.NUMNOTA, " + 
					"    PRC.AD_CODPARCEXT " + 
					"FROM " + 
					"    TGFCAB C " + 
					"    INNER JOIN TGFPAR PRC ON PRC.CODPARC = C.CODPARC " + 
					"WHERE " + 
					"    C.NUNOTA = :nunota";
			
			
			JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
			NativeSql query  = new NativeSql(jdbc);
			query.setNamedParameter("nunota", nunota);
			ResultSet result = query.executeQuery(sql);
	
			if (result.next()) {
				BigDecimal numnota        = result.getBigDecimal("NUMNOTA");
				BigDecimal codigoParceiro = result.getBigDecimal("AD_CODPARCEXT");
				
				retorno = "{"
						+ "    'numnota': "+numnota+", "
						+ "    'situacaoComprovante': '"+situacaoComprovante+"', "
						+ "    'codigoParceiro': "+codigoParceiro+""
						+ "}";
			}
			
			return retorno;
		} catch (Exception e) {
			throw new Exception("Erro ao gerar Json\n" + e.getMessage());
		}
	}
	
	private boolean validarNumeroComprovante(PersistenceEvent persistenceEvent) throws Exception {
		DynamicVO dynVO    = (DynamicVO) persistenceEvent.getVo();
		BigDecimal nunota  = dynVO.asBigDecimal("NUNOTA");
		String nrodeposito = dynVO.asString("NRODESPOSITO");
		boolean retorno    = false;
		String sql         = "" +
				"SELECT " + 
				"	COUNT(NUFIN) AS TOT " + 
				"FROM  " + 
				"	TGFFIN F " + 
				"	INNER JOIN " + 
				"	( " + 
				"		SELECT DISTINCT NUMNOTA FROM TGFFIN F WHERE F.NUNOTA = :nunota " + 
				"	) F2 ON F2.NUMNOTA = F.NUMNOTA " + 
				"WHERE " + 
				"	F.BH_NRODEPOSITO = :nrodeposito";

		JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
		NativeSql query  = new NativeSql(jdbc);
		query.setNamedParameter("nunota", nunota);
		query.setNamedParameter("nrodeposito", nrodeposito);
		ResultSet result = query.executeQuery(sql);

		if (result.next()) {
			Integer total = result.getInt("TOT");

			if(total > 1) {
				retorno = true;
			}
		}
		
		return retorno;
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		try {
			DynamicVO dynVO = (DynamicVO) arg0.getVo();
			boolean comprovanteExistente = this.validarNumeroComprovante(arg0);
			
			if(comprovanteExistente) {
				throw new Exception("Este número de comprovante já foi utilizado neste pedido!");
			}

			BigDecimal valorDeposito = dynVO.asBigDecimal("VALORDEPOSITO");
			if (!(dynVO.asString("STATUSPEDIDO").equals("LI") && dynVO.asString("STATUSPROMESSA").equals("PE")
					&& valorDeposito != null
					&& valorDeposito.compareTo(BigDecimal.ZERO) == 0)) {
				enviarDados(arg0);
			}
		} catch (Exception e) {
			throw new Exception("Falha Geral\n" + e.getMessage());
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