package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Locale;

public class UpdateTef extends SnkIntegrationsApi implements EventoProgramavelJava{
	
	public UpdateTef() {
		this.exigeAutenticacao = true;
		this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}

	private boolean isCredito = true;

	private BigDecimal valorTotalTransacao = BigDecimal.ZERO;


	private void sendDataTef(PersistenceEvent persistenceEvent) throws Exception {
		
		try {
			DynamicVO tefVO   = (DynamicVO) persistenceEvent.getVo();
			String confirmado = "N";
			
			if(tefVO.asString("CONFIRMADO") != null && !tefVO.asString("CONFIRMADO").isEmpty()) { confirmado = tefVO.asString("CONFIRMADO"); }
			
			if(confirmado.equals("S")) {
				String url = this.urlApi+"/financial/sankhya/tef/"+tefVO.asBigDecimal("NUFIN").toString();
				IntegrationApi.send(url, "", "POST");
			}
		}
		catch(Exception e) {
			throw new Exception("Mensagem de erro: "+e.getMessage());
		}
	}

	private String gerarJsonCartao(PersistenceEvent persistenceEvent, BigDecimal idNota) throws Exception {

		DynamicVO tefVO    = (DynamicVO) persistenceEvent.getVo();
		String comprovante = tefVO.asString("COMPROVANTE");
		String bandeira    = tefVO.getProperty("BANDEIRA").toString();
		String json 	   = "";

		JSONObject jsonObjectCr = new JSONObject(comprovante);

		JSONArray cupomEstabelecimento = jsonObjectCr.getJSONArray("cupomEstabelecimento");

		String auxTipoNegociacao = cupomEstabelecimento.get(15).toString();
		jsonObjectCr = new JSONObject(auxTipoNegociacao);
		String tipoNegociacao = jsonObjectCr.getString("linha").trim();

		String auxNumeroCartao = cupomEstabelecimento.get(14).toString();
		jsonObjectCr = new JSONObject(auxNumeroCartao);
		String numeroCartao = jsonObjectCr.getString("linha");
		numeroCartao = numeroCartao.replaceAll(".+\\s+([\\*0-9]+)", "$1").trim();

		String auxValorFinal = cupomEstabelecimento.get(17).toString();
		jsonObjectCr = new JSONObject(auxValorFinal);
		String valorFinal = jsonObjectCr.getString("linha").trim();
		valorFinal = valorFinal.replaceAll(".+\\s?([0-9]+)\\s+.*", "$1").trim();
		valorFinal = valorFinal.substring(valorFinal.indexOf("$") + 1, valorFinal.length()).trim();
		valorFinal.replace(",", ".");
		valorFinal.replace(", ", ".");
		valorFinal.replace(" ,", ".");
		valorFinal.replace(" , ", ".");

		this.valorTotalTransacao = new BigDecimal(valorFinal.replaceAll("\\.", "").replace(",","."));

		String auxNsu = cupomEstabelecimento.get(11).toString();
		jsonObjectCr = new JSONObject(auxNsu);
		String nsu = jsonObjectCr.getString("linha");
		nsu = nsu.replaceAll(".+DOC:\\s?([0-9]+)\\s+.*", "$1").trim();

		JSONObject jsonObjectCe = new JSONObject(comprovante);
//		JSONArray cupomEstabelecimento = jsonObjectCe.getJSONArray("cupomEstabelecimento");

		String auxNumeroEstabelecimento = cupomEstabelecimento.get(8).toString();
		jsonObjectCe = new JSONObject(auxNumeroEstabelecimento);
		String numeroEstabelecimento = jsonObjectCe.getString("linha");
		numeroEstabelecimento = numeroEstabelecimento.replaceAll("\\s*([0-9]+).*", "$1").trim();

		// Usado em ambiente de teste
		if (numeroEstabelecimento.equals("823982346832235")) {
			numeroEstabelecimento = "1023441710";
		}

		String tipoVenda = "D";
		this.isCredito   = !tipoNegociacao.equalsIgnoreCase("VENDA DEBITO A VISTA");
		if (tipoNegociacao.equalsIgnoreCase("VENDA CREDITO PARC.LOJA")) {
			tipoVenda = "P";
		} else if (tipoNegociacao.equalsIgnoreCase("VENDA CREDITO A VISTA")) {
			tipoVenda = "C";
		}

		if (bandeira.equals("DEMOCARD") && tipoNegociacao.equalsIgnoreCase("VENDA CREDITO A VISTA")) {
			bandeira = "MASTERCARD";
		} // Usado em ambiente de teste
		else if (bandeira.equals("DEMOCARD") && tipoNegociacao.equalsIgnoreCase("VENDA CREDITO PARC.LOJA")) {
			bandeira = "MASTERCARD";
		} // Usado em ambiente de teste
		else if (bandeira.equals("DEMOCARD") && tipoNegociacao.equalsIgnoreCase("VENDA DEBITO A VISTA")) {
			bandeira = "MAESTRO";
		} // Usado em ambiente de teste

		JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
		NativeSql sql = new NativeSql(jdbc);
		sql.appendSql(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPROD LIKE '%" + bandeira
				+ "%' AND TIPOVENDA = '" + tipoVenda+  "'");

		BigDecimal idProduto = null;
		BigDecimal idBandeira = null;

		ResultSet r1 = sql.executeQuery();
		if (r1.next()) {
			idProduto = r1.getBigDecimal("NUPRO");
			idBandeira = r1.getBigDecimal("NUBAN");
		}
		r1.getStatement().close();


		json = "\"cartaoSankhya\": {"
//					+ "\"idCartao\": " + tefVO.asBigDecimal("NUFIN").toString() + ","
					+ "\"idNota\": " + idNota + ","
					+ "\"idTipoProduto\": " + idProduto + ","
					+ "\"idBandeira\": " + idBandeira + ","
					+ "\"idPagamento\": \"0\", "
					+ "\"idEstabelecimento\": \"" + numeroEstabelecimento + "\","
					+ "\"descricaoProduto\": \"" + bandeira + "\","
					+ "\"codigoAutorizacao\": \"" + tefVO.getProperty("AUTORIZACAO").toString() + "\","
					+ "\"nsu\": \"" + nsu + "\","
					+ "\"tid\": \"0\","
					+ "\"taxaAdministrativa\": " + tefVO.getProperty("VLRTAXA").toString() + ","
					+ "\"numeroCartao\": \"" + numeroCartao + "\","
					+ "\"dataPagamento\": \"" + tefVO.getProperty("DTTRANSACAO") + "\","
					+ "\"valorTransacao\": " + tefVO.getProperty("VLRTRANSACAO").toString()
				+ "}";


		return json;
	}

	private String gerarJsonTef(PersistenceEvent persistenceEvent) {

		DynamicVO tefVO = (DynamicVO) persistenceEvent.getVo();
		String json = "\"tefSankhya\": {"
						+ "\"idTef\": " + tefVO.getProperty("IDENTIFICACAOTEF") + ","
						+ "\"idFinanceiro\": " + tefVO.getProperty("NUFIN") + ","
						+ "\"rede\": " + tefVO.getProperty("REDE") + ","
						+ "\"tipoDocumento\": " + tefVO.getProperty("TIPODOC") + ","
						+ "\"numeroComprovante\": \"" + tefVO.getProperty("NUMCV") + "\","
						+ "\"numeroDocumento\": \"" + tefVO.getProperty("NUMDOC") + "\","
						+ "\"nsu\": \"" + tefVO.getProperty("NUMNSU") + "\","
						+ "\"numeroPontoVenda\": \"" + tefVO.getProperty("NUMPV") + "\","
						+ "\"autorizacao\": \"" + tefVO.getProperty("AUTORIZACAO") + "\","
						+ "\"desdobramento\": " + tefVO.getProperty("DESDOBRAMENTO") + ","
						+ "\"dataTransacao\": \"" + tefVO.getProperty("DTTRANSACAO") + "\","
						+ "\"valorTransacao\": \"" + tefVO.getProperty("VLRTRANSACAO") + "\","
						+ "\"valorTaxa\": \"" + tefVO.getProperty("VLRTAXA") + "\","
						+ "\"bandeira\": \"" + tefVO.getProperty("BANDEIRA") + "\","
						+ "\"confirmado\": \"" + tefVO.getProperty("CONFIRMADO") + "\","
						+ "\"idUsuario\": \"" + tefVO.getProperty("CODUSU") + "\","
						+ "\"processo\": \"" + tefVO.getProperty("PROCESSO") + "\","
						+ "\"comprovante\": \"" + tefVO.asString("COMPROVANTE").replace("\"", "'")+ "\","
						+ "\"nomeRede\": \"" + tefVO.getProperty("NOMEREDE") + "\","
						+ "\"operacao\": \"" + tefVO.getProperty("OPERACAOTEF") + "\""
				+ "}";

		return json;
	}

	private String gerarJsonFinanceiro(ResultSet tgffin, String idUsuario) throws SQLException {

		String json = "\"financeiroSankhya\": {"
						+ "\"idFinanceiro\": " + tgffin.getBigDecimal("NUFIN").toString()+ ","
						+ "\"idEmpresa\": " + tgffin.getBigDecimal("CODEMP").toString() + ","
						+ "\"idNota\": " + tgffin.getBigDecimal("NUNOTA") + ","
						+ "\"numeroNota\": " + tgffin.getBigDecimal("NUMNOTA").toString() + ","
						+ "\"idParceiro\": " + tgffin.getBigDecimal("CODPARC").toString() + ","
						+ "\"idTipoOperacao\": " + tgffin.getBigDecimal("CODTIPOPER").toString() + ","
						+ "\"idUsuarioBaixa\": " + idUsuario + ","
						+ "\"idTipoTitulo\": " + (this.isCredito ? "7" : "28") + ","
						+ "\"valorDesdobramento\": \"" + this.valorTotalTransacao.toString() + "\","
						+ "\"recebimentoCartao\": \"S\" ,"
						+ "\"dataPrazo\": \"" + tgffin.getString("DTPRAZO") + "\" "
				+ "}";

		return json;
	}

	private void enviarDadosCartao(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO tefVO   = (DynamicVO) persistenceEvent.getVo();
		String confirmado = "N";

		if(tefVO.asString("CONFIRMADO") != null && !tefVO.asString("CONFIRMADO").isEmpty()) {
			confirmado = tefVO.asString("CONFIRMADO");
		}

		int parcela = 1;

		if (tefVO.getProperty("DESDOBRAMENTO") != null) {
			parcela = tefVO.asInt("DESDOBRAMENTO");
		}

//		PRIMEIRA PARCELA NÃO ESTÁ CONFIRMANDO
		if((confirmado.trim().equals("N") && parcela == 1) || confirmado.equals("S")) {

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
			consulta.append(" 	VLRDESDOB, ");
			consulta.append(" 	DTPRAZO ");
			consulta.append(" FROM  ");
			consulta.append(" 	TGFFIN  ");
			consulta.append(" WHERE ");
			consulta.append(" 	NUFIN = :NUFIN ");

			sql.setNamedParameter("NUFIN", tefVO.asBigDecimal("NUFIN"));
			ResultSet result = sql.executeQuery(consulta.toString());
			if (result.next()) {

				String jsonCartao = "";
				String jsonFin 	  = "";
				String jsonTef    = "";

				try {
					jsonTef = this.gerarJsonTef(persistenceEvent);
				}catch (Exception e) {
					throw new Exception("Falha ao gerar Tef");
				}

				try {
					jsonCartao = this.gerarJsonCartao(persistenceEvent, result.getBigDecimal("NUNOTA"));
				} catch (Exception e) {
					throw new Exception("Falha ao gerar Cartão " + e.getMessage());
				}

				try {
					jsonFin 	  = this.gerarJsonFinanceiro(result, tefVO.getProperty("CODUSU").toString());
				} catch (Exception e) {
					throw new Exception("Falha ao gerar Financeiro");
				}

				String json       = "{"
									+ jsonFin + ", "
									+ jsonTef + ", "
									+ jsonCartao
									+ "} ";

//				PROCESSO V2
				String url = this.urlApi + "/v2/caixas/cartoes";
				String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
				IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);

//				if (true) {
//					throw new Exception(jsonTef);
//				}
			}
		}
	}
	
	public void actionSendDataTef() throws Exception {
		
		String url = this.urlApi+"/financial/sankhya/tef/sincronize";
		IntegrationApi.send(url, "", "POST");
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
//		sendDataTef(arg0);
		this.enviarDadosCartao(arg0);
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
//		sendDataTef(arg0);
		this.enviarDadosCartao(arg0);
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