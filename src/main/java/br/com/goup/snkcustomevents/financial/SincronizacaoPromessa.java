package br.com.goup.snkcustomevents.financial;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.TimeUtils;

public class SincronizacaoPromessa extends SnkIntegrationsApi implements EventoProgramavelJava, AcaoRotinaJava {

	private int qtdException = 0;

	private String nufinAdiant = "";

	public SincronizacaoPromessa() {
		this.exigeAutenticacao = true;
		this.forceUrl("Production"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}
	
	private void enviarDados(PersistenceEvent persistenceEvent) throws Exception {
		
		try {
			DynamicVO dynVO     = (DynamicVO) persistenceEvent.getVo();
			String sincronizado = "N";
			this.nufinAdiant    = "";
			
			try {
				sincronizado = dynVO.asString("AD_TZACONF");
			} 
			catch (Exception e) { sincronizado = "N"; }
			
			if(sincronizado != null && !sincronizado.equals("S")) {
				this.validarParametros(dynVO);
				String acao = this.validarAcao(persistenceEvent);
				
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
	
	private String validarAcao(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO dynVO =  (DynamicVO) persistenceEvent.getVo();
		ModifingFields modifingFields = persistenceEvent.getModifingFields();
		String acao              = "";
		try {

			String numeroDeposito    = dynVO.asString("NRODESPOSITO");
			boolean temDeposito      = (numeroDeposito != null && !numeroDeposito.equals("") && !numeroDeposito.equals("null")) ? true : false;
			BigDecimal valorDeposito = BigDecimal.ZERO;
			if (modifingFields.isModifing("VALORDEPOSITO")) {
				valorDeposito = modifingFields.getNewValue("VALORDEPOSITO") != null ? new BigDecimal(modifingFields.getNewValue("VALORDEPOSITO").toString()) : BigDecimal.ZERO;
			} else {
				valorDeposito = dynVO.asBigDecimal("VALORDEPOSITO");
			}

			String statusPromessa = "";
			if (modifingFields.isModifing("STATUSPROMESSA")) {
				statusPromessa = modifingFields.getNewValue("STATUSPROMESSA").toString();
			}

			Integer numeroAcerto     = dynVO.asInt("NUACERTO");
			boolean compensado       = (numeroAcerto != null && numeroAcerto > 0) ? true : false;

			if (
					dynVO.asString("STATUSPEDIDO").equals("LI")
					&& (
							statusPromessa.equals("CO")
							|| statusPromessa.equals("PA")
//							|| (
//									statusPromessa.equals("PE")
//								&& valorDeposito != null
//								&& valorDeposito.compareTo(BigDecimal.ZERO) > 0
//							)
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

	private String gerarJsonFinanceiro(ResultSet tgffin, Integer idUsuario, String dataBaixa, BigDecimal valorPromessa) throws Exception {

		String dataPrazo = tgffin.getString("DTPRAZO");

		Calendar dtPrazo = null;
		if (dataPrazo != null) {
			dtPrazo = Calendar.getInstance();
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				dtPrazo.setTime(format.parse(dataPrazo));
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dataPrazo = format.format(dtPrazo.getTime());
			} catch (Exception e) {
				throw new Exception("Falha na Data prazo");
			}
		}

		if (dataBaixa != null) {
			try {
				Calendar dtBaixa = Calendar.getInstance();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				dtBaixa.setTime(format.parse(dataBaixa));
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dataBaixa = format.format(dtBaixa.getTime());
			} catch (Exception e) {
				throw new Exception("Falha na Data Baixa");
			}
		}

		Integer tipoTitulo = tgffin.getBigDecimal("CODTIPTIT").intValue() > 0
				? tgffin.getBigDecimal("CODTIPTIT").intValue(): 15;


//		BigDecimal valor = tgffin.getBigDecimal("VLRBAIXA") != null
//				&& tgffin.getBigDecimal("VLRBAIXA").compareTo(BigDecimal.ZERO) > 0
//				? tgffin.getBigDecimal("VLRBAIXA") : valorPromessa;

		String json = " {"
				+ "\"idFinanceiro\": " + tgffin.getBigDecimal("NUFIN").toString()+ ","
				+ "\"idEmpresa\": " + tgffin.getBigDecimal("CODEMP").toString() + ","
				+ "\"idNota\": " + tgffin.getBigDecimal("NUNOTA") + ","
				+ "\"numeroNota\": " + tgffin.getBigDecimal("NUMNOTA").toString() + ","
				+ "\"idParceiro\": " + tgffin.getBigDecimal("CODPARC").toString() + ","
				+ "\"idTipoOperacao\": " + tgffin.getBigDecimal("CODTIPOPER").toString() + ","
				+ "\"idUsuarioBaixa\": " + idUsuario + ","
				+ "\"idTipoTitulo\": " + tipoTitulo.toString() + ","
				+ "\"valorDesdobramento\": \"" + valorPromessa.toString() + "\","
				+ "\"valorBaixa\": \"" + valorPromessa.toString() + "\","
				+ "\"recebimentoCartao\": \"N\" ,"
				+ "\"dataBaixa\": \"" +  dataBaixa + "\", "
				+ "\"dataPrazo\": \"" +  dataPrazo + "\" "
				+ "}";

		return json;
	}

	private String gerarJsonPromessa(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO prmVO =  (DynamicVO) persistenceEvent.getVo();

		ModifingFields modifingFields = persistenceEvent.getModifingFields();

		String idContaBancaria = "0";
		if (modifingFields.isModifing("CODCTABCOINT")) {
			idContaBancaria = modifingFields.getNewValue("CODCTABCOINT").toString();
		} else {
			idContaBancaria = prmVO.asBigDecimal("CODCTABCOINT").toString();
		}

		String numeroDeposito = "";

		if (modifingFields.isModifing("NRODESPOSITO")) {
			if (modifingFields.getNewValue("NRODESPOSITO") != null) {
				numeroDeposito = modifingFields.getNewValue("NRODESPOSITO").toString();
			}
		} else if (prmVO.getProperty("NRODESPOSITO") != null){
			numeroDeposito = prmVO.getProperty("NRODESPOSITO").toString();
		}

		BigDecimal idAdiantamento = BigDecimal.ZERO;

		if (prmVO.getProperty("NUADIANTAMENTO") != null) {
			idAdiantamento = new BigDecimal(prmVO.getProperty("NUADIANTAMENTO").toString());
		}

		if (prmVO.getProperty("NUFINDESPADIANT") != null) {
			idAdiantamento =  new BigDecimal(prmVO.getProperty("NUFINDESPADIANT").toString());
		}


		String json = "\"promessaSankhya\": {"
				+ "\"idNota\": " + prmVO.asBigDecimal("NUNOTA") + ","
				+ "\"idAdiantamento\": " + idAdiantamento + ","
				+ "\"numeroDeposito\": \"" +  numeroDeposito + "\", "
				+ "\"idContaBancaria\": " + idContaBancaria
				+ "}";

		return json;
	}

	private void enviarDadosV2(String verboHttp, String url, String json) throws Exception {
		this.qtdException++;
		String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
		try {
			IntegrationApi.sendHttp(url, json, verboHttp, "Bearer " + token);
		} catch (Exception e) {
			if (this.qtdException < 2) {
				enviarDadosV2(verboHttp, url, json);
			} else {
				throw new Exception("Falha: " + e.getMessage() + "\n" + json);
			}
		}
		this.qtdException = 0;
	}

	private DynamicVO gerarCredito(ResultSet tgffin, BigDecimal valorCredito) throws Exception {

		//		if (true) {
//			throw new Exception("teste");
//		}
//		StringBuffer consulta = new StringBuffer();
//
//		consulta.append(" SELECT ");
//		consulta.append(" 	SNK_GET_NUFIN() FROM DUAL ");
//
//		BigDecimal nufin = null;
//		ResultSet result = sql.executeQuery(consulta.toString());
//		if (result.next()) {
//			nufin = result.getBigDecimal(1);
//
//
//		}

		Calendar dataVencimento  = Calendar.getInstance();
		dataVencimento.add(Calendar.YEAR, 1);
		JapeWrapper finDAO = JapeFactory.dao("Financeiro");
		FluidCreateVO tgffinCre = finDAO.create();
		tgffinCre.set("CODPARC", tgffin.getBigDecimal("CODPARC"));
		tgffinCre.set("DESDOBRAMENTO", "0");
		tgffinCre.set("CODTIPOPER", new BigDecimal("4106"));
		tgffinCre.set("RECDESP", new BigDecimal("-1"));
		tgffinCre.set("ORIGEM", "F");
		tgffinCre.set("CODEMP", tgffin.getBigDecimal("CODEMP"));
		tgffinCre.set("NUMNOTA", tgffin.getBigDecimal("NUMNOTA"));
		tgffinCre.set("DTNEG", tgffin.getTimestamp("DTNEG"));
		tgffinCre.set("VLRDESDOB", valorCredito);
		tgffinCre.set("DTVENC", TimeUtils.buildData(dataVencimento.get(Calendar.DAY_OF_MONTH),
													dataVencimento.get(Calendar.MONTH),
													dataVencimento.get(Calendar.YEAR)));
		tgffinCre.set("CODNAT", new BigDecimal("4040100"));
		tgffinCre.set("CODTIPTIT", new BigDecimal("26"));
		tgffinCre.set("PRAZO", new BigDecimal("0"));
		tgffinCre.set("CODBCO", new BigDecimal("0"));
		tgffinCre.set("CODUSU", AuthenticationInfo.getCurrent().getUserID());
		tgffinCre.set("DHMOV", TimeUtils.getNow());

		return tgffinCre.save();
	}

//	private BigDecimal valorCredito(NativeSql sql) {
//
//		StringBuffer consulta = new StringBuffer();
//
//		consulta.append(" SELECT ");
//		consulta.append(" 	COALESCE(ITE.VLRTOT) AS VALOR ");
//		consulta.append(" FROM  ");
//		consulta.append(" 	TGFCAB CAB  ");
//		consulta.append(" INNER JOIN  ");
//		consulta.append(" 	TGFITE ITE ");
//		consulta.append(" WHERE ");
//		consulta.append(" 	NUNOTA = :NUNOTA ");
//		consulta.append(" 	AND TZASTATUS = 'CA' ");
//
//
//	}

	private boolean executarAcao(String acao, PersistenceEvent persistenceEvent) throws Exception {
		boolean retorno = false;
		String url      = "";
		String json     = "";
		String metodo   = "POST";

		DynamicVO prmVO = (DynamicVO) persistenceEvent.getVo();

		int codigoUsuario;

		try {
			codigoUsuario = prmVO.asBigDecimal("AD_CODUSU").intValue();
		}
		catch (Exception e) { codigoUsuario = 0; }

		switch (acao) {
			case "ConfirmarDeposito":

				JdbcWrapper jdbc 	  = persistenceEvent.getJdbcWrapper();
				NativeSql sql		  = new NativeSql(jdbc);
				StringBuffer consulta = new StringBuffer();

				consulta.append(" SELECT ");
				consulta.append(" 	NUFIN, ");
				consulta.append(" 	CODEMP, ");
				consulta.append(" 	NUNOTA, ");
				consulta.append(" 	NUMNOTA, ");
				consulta.append(" 	CODPARC, ");
				consulta.append(" 	CODTIPOPER, ");
				consulta.append(" 	CODTIPTIT, ");
				consulta.append(" 	VLRDESDOB, ");
				consulta.append(" 	VLRBAIXA, ");
				consulta.append(" 	DTPRAZO, ");
				consulta.append(" 	DTNEG ");
				consulta.append(" FROM  ");
				consulta.append(" 	TGFFIN  ");
				consulta.append(" WHERE ");
				consulta.append(" 	NUNOTA = :NUNOTA ");
				consulta.append(" 	AND CODTIPTIT IN (15, 0, 26) ");

				sql.setNamedParameter("NUNOTA", prmVO.getProperty("NUNOTA"));
				ResultSet result = sql.executeQuery(consulta.toString());
				if (result.next()) {


					if (result.getBigDecimal("CODTIPOPER").intValue() == 3118) {

						consulta = new StringBuffer();
						consulta.append(" SELECT NUFIN FROM TGFFIN WHERE RECDESP =-1 AND CODTIPTIT = 26 " +
								" AND CODTIPOPER = 4106 AND NUMNOTA = " + result.getBigDecimal("NUMNOTA").intValue());

						ResultSet fin = sql.executeQuery(consulta.toString());
						if (!fin.next()) {

							BigDecimal valor = result.getBigDecimal("VLRBAIXA");
							if (valor == null) {
								valor = prmVO.asBigDecimal("VALORDEPOSITO");
							}
							if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {

								DynamicVO finDespVo = this.gerarCredito(result, valor);
								this.nufinAdiant = finDespVo.getPrimaryKey().toString().replaceAll("\\D", "");
								prmVO.setProperty("NUADIANTAMENTO", this.nufinAdiant);
							}
						}
					}

					String jsonFin = this.gerarJsonFinanceiro(result, codigoUsuario, prmVO.getProperty("DATADEPOSITO").toString(), prmVO.asBigDecimal("VALORDEPOSITO"));

					json = jsonFin;

					metodo = "POST";

					if (result.getBigDecimal("CODTIPTIT").intValue() == 26) {
						url = this.urlApi + "/v2/caixas/pagamentos";
					} else {

						url = this.urlApi + "/v2/caixas/depositos";
						String jsonPromessa = this.gerarJsonPromessa(persistenceEvent);
						json = "{\"financeiroSankhya\":" + json
								+  ", "
								+ jsonPromessa
								+ "} ";
					}
				} else {
					throw new Exception("Pedido sem financeiro localizado! \n" + consulta.toString() + "\n Nro " + prmVO.getProperty("NUNOTA"));
				}
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
				json = this.getJsonDeposito(persistenceEvent, "");
				url = this.urlApi + "/v2/promessas/pedido";
				break;
	
			default:
				retorno = false;
				break;
		}
		
		if(!url.isEmpty()) {
			this.enviarDadosV2(metodo, url, json);

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

	@Override
	public void doAction(ContextoAcao contextoAcao) throws Exception {

//		JdbcWrapper jdbc 	  = contextoAcao.getQuery().getJdbcWrapper();
		for (Registro registro: contextoAcao.getLinhas()) {
			QueryExecutor tgffin = contextoAcao.getQuery();
//		NativeSql sql		  = new NativeSql(jdbc);
			StringBuffer consulta = new StringBuffer();

			consulta.append(" SELECT ");
			consulta.append(" 	NUFIN, ");
			consulta.append(" 	CODEMP, ");
			consulta.append(" 	NUNOTA, ");
			consulta.append(" 	NUMNOTA, ");
			consulta.append(" 	CODPARC, ");
			consulta.append(" 	CODTIPOPER, ");
			consulta.append(" 	CODTIPTIT, ");
			consulta.append(" 	VLRDESDOB, ");
			consulta.append(" 	VLRBAIXA, ");
			consulta.append(" 	DTPRAZO, ");
			consulta.append(" 	DTNEG ");
			consulta.append(" FROM  ");
			consulta.append(" 	TGFFIN  ");
			consulta.append(" WHERE ");
			consulta.append(" 	NUNOTA =  " + registro.getCampo("NUNOTA"));
			consulta.append(" 	AND CODTIPTIT IN (15, 0, 26) ");

			tgffin.nativeSelect(consulta.toString());
			tgffin.next();

//			this.gerarCredito(tgffin);
		}

	}
}