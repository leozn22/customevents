package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateTef extends SnkIntegrationsApi implements EventoProgramavelJava{
	
	public UpdateTef() {
		this.exigeAutenticacao = true; 
		// QUANDO ALTERAR O PARÂMETRO ABAIXO, DEVE ALTERAR DA MESMA FORMA NOS ARQUIVOS: UpdateSql.java e ItemAcao.java
		this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}

	private boolean isCredito = true;

	private int qtdException = 0;

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

	private String gerarJsonCartao(PersistenceEvent persistenceEvent, BigDecimal idNota, BigDecimal idAcerto, BigDecimal numeroNota) throws Exception {

		DynamicVO tefVO    			 = (DynamicVO) persistenceEvent.getVo();
		String comprovante 			 = tefVO.asString("COMPROVANTE");
		String bandeira    			 = tefVO.getProperty("BANDEIRA").toString();
		String json 	   			 = "";
		String numeroEstabelecimento = "";
		String tipoNegociacao 		 = "";
		boolean parcelado 		 	 = false;

		JSONObject jsonObjectCr        = new JSONObject(comprovante);
		JSONArray cupomEstabelecimento = jsonObjectCr.getJSONArray("cupomEstabelecimento");


		for (int i = 0; i < cupomEstabelecimento.length() - 1; i++) {
			String jsonPesquisa = cupomEstabelecimento.get(i).toString();

			JSONObject jsonLinha = new JSONObject(jsonPesquisa);
			String valor         =	jsonLinha.getString("linha").trim();

//			ESTABELECIMENTO
			if (valor.contains("/POS") || valor.contains("823982346832235")) {

				numeroEstabelecimento = valor.replaceAll("\\s*([0-9]+).*", "$1").trim();
				numeroEstabelecimento = numeroEstabelecimento.trim().substring(1, numeroEstabelecimento.trim().length() - 4);
				if (numeroEstabelecimento.startsWith("0")) {
					numeroEstabelecimento = numeroEstabelecimento.substring(1);
				}
			}

//			TIPO NEGOCIAÇÃO
			if (valor.contains("VENDA")) {
				tipoNegociacao = valor.trim();
			}

			if (valor.contains("PARCELADO LOJA EM")) {
				parcelado = true;
			}

//			VALOR TOTAL DA VENDA
			if (valor.contains("VALOR")) {
				valor = valor.replaceAll("[^0-9,.]+", "").trim();

				valor.replace(",", ".");
				valor.replace(", ", ".");
				valor.replace(" ,", ".");
				valor.replace(" , ", ".");

				this.valorTotalTransacao = new BigDecimal(valor.replaceAll("\\.", "").replace(",","."));
			}
		}

//		String auxTipoNegociacao = cupomEstabelecimento.get(12).toString(); //base treina 15

//		jsonObjectCr = new JSONObject(auxTipoNegociacao);
//		String tipoNegociacao = jsonObjectCr.getString("linha").trim();

//		REMOVIDO - NA PRODUÇÃO NÃO TEM
//		String auxNumeroCartao = cupomEstabelecimento.get(14).toString();
//		jsonObjectCr = new JSONObject(auxNumeroCartao);
//		String numeroCartao = jsonObjectCr.getString("linha");
//		numeroCartao = numeroCartao.replaceAll(".+\\s+([\\*0-9]+)", "$1").trim();

//		String auxValorFinal = cupomEstabelecimento.get(14).toString();//base treina 17
//		jsonObjectCr = new JSONObject(auxValorFinal);
//		String valorFinal = jsonObjectCr.getString("linha").trim();
//		valorFinal = valorFinal.replaceAll(".+\\s?([0-9]+)\\s+.*", "$1").trim();
//		valorFinal = valorFinal.substring(valorFinal.indexOf(":") + 1, valorFinal.length()).trim(); //BASE TREINA TEM O $
//		valorFinal.replace(",", ".");
//		valorFinal.replace(", ", ".");
//		valorFinal.replace(" ,", ".");
//		valorFinal.replace(" , ", ".");

//		this.valorTotalTransacao = new BigDecimal(valorFinal.replaceAll("\\.", "").replace(",","."));

//		String auxNsu = cupomEstabelecimento.get(10).toString(); //base treina 11
//		jsonObjectCr = new JSONObject(auxNsu);
//		String nsu = jsonObjectCr.getString("linha");
//		nsu = nsu.replaceAll(".+DOC:\\s?([0-9]+)\\s+.*", "$1").trim();

//		JSONObject jsonObjectCe = new JSONObject(comprovante);
//		JSONArray cupomEstabelecimento = jsonObjectCe.getJSONArray("cupomEstabelecimento");

//		String auxNumeroEstabelecimento = cupomEstabelecimento.get(9).toString(); //base treina 8
//		jsonObjectCe = new JSONObject(auxNumeroEstabelecimento);
//		String numeroEstabelecimento = jsonObjectCe.getString("linha");
//		numeroEstabelecimento = numeroEstabelecimento.replaceAll("\\s*([0-9]+).*", "$1").trim();


		// Usado em ambiente de teste
		if (numeroEstabelecimento.equals("823982346832235") || numeroEstabelecimento == null || numeroEstabelecimento.equals("")) {
			numeroEstabelecimento = "1023441710";
		}

		String tipoVenda = "D";
		this.isCredito   = !tipoNegociacao.contains("DEBITO");
		if (tipoNegociacao.contains("PARC.LOJA") || parcelado) {
			tipoVenda = "P";
		} else if (tipoNegociacao.contains("CREDITO")) {
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
		sql.appendSql(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPRODLOJA LIKE '%" + bandeira
				+ "%' AND TIPOVENDA = '" + tipoVenda+  "'");

		BigDecimal idProduto  = null;
		BigDecimal idBandeira = null;
		String descrProd      = null;

		ResultSet r1 = sql.executeQuery();
		if (r1.next()) {
			idProduto  = r1.getBigDecimal("NUPRO");
			idBandeira = r1.getBigDecimal("NUBAN");
			descrProd  = r1.getString("DESCRPROD");
		}
		r1.getStatement().close();

		if (idProduto == null) {
			String[] valores = bandeira.split(" ");
			String valor = valores[0].trim();

			r1 = sql.executeQuery(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPRODLOJA LIKE '%" + valor
					+ "%' AND TIPOVENDA = '" + tipoVenda+  "'");
			if (r1.next()) {
				idProduto = r1.getBigDecimal("NUPRO");
				idBandeira = r1.getBigDecimal("NUBAN");
				descrProd  = r1.getString("DESCRPROD");
			}
			r1.getStatement().close();
		}

		String textoNuNota = (idNota != null ? " AND NUNOTA = " + idNota : "");

		r1 = sql.executeQuery("SELECT NUCTRL FROM AD_TCCCAB WHERE "
				+ " NSU = '" + tefVO.getProperty("NUMNSU").toString() + "'"
				+ " AND CODAUT = '" + tefVO.getProperty("AUTORIZACAO").toString() + "'" + textoNuNota);
				
		if (!r1.next()) {
			JapeWrapper logDAO   = JapeFactory.dao("AD_TCCCAB");
			FluidCreateVO creLog = logDAO.create();
			
			creLog.set("CODTIPPROD", idProduto);
			creLog.set("DESCRTIPPROD", descrProd);
			creLog.set("IDPAG", "0");
			creLog.set("TID", "0");
			creLog.set("TAXAADM", tefVO.getProperty("VLRTAXA"));
			creLog.set("NUBAN", idBandeira);
			creLog.set("NUCOMPENS", idAcerto);
			creLog.set("NUESTABELECIMENTO", numeroEstabelecimento);
			creLog.set("DTPAGCARTAO", tefVO.getProperty("DTTRANSACAO"));
			creLog.set("CODAUT", tefVO.getProperty("AUTORIZACAO").toString());
			creLog.set("NUNOTA", idNota);
//			creLog.set("VLRTRANSACAO", new BigDecimal(tefVO.getProperty("VLRTRANSACAO").toString()));
			creLog.set("VLRTRANSACAO", this.valorTotalTransacao);
			creLog.set("NSU", tefVO.getProperty("NUMNSU").toString());
//			creLog.set("TIPTITULO", tefVO.getProperty("NUMNSU").toString());
			creLog.save();

			if (idNota != null) {
				InformacaoPagamentoRequisicao informacaoPagamentoRequisicao = new InformacaoPagamentoRequisicao();
				String tipoPagamento = (this.isCredito ? "cartaoCredito" : "cartaoDebito");
				informacaoPagamentoRequisicao.informarPagamentoSite(tipoPagamento, numeroNota.toString());
			}
		}
		r1.getStatement().close();


		String dataTransacao = tefVO.getProperty("DTTRANSACAO").toString();
		Calendar dtTransacao = null;
		if (dataTransacao != null) {
			dtTransacao = Calendar.getInstance();
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				dtTransacao.setTime(format.parse(dataTransacao));
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dataTransacao = format.format(dtTransacao.getTime());
			} catch (Exception e) {
				throw new Exception("Falha na Data de Baixa");
			}
		}

		json = "\"cartaoSankhya\": {"
//					+ "\"idCartao\": " + tefVO.asBigDecimal("NUFIN").toString() + ","
					+ "\"idNota\": " + idNota + ","
					+ "\"idTipoProduto\": " + idProduto + ","
					+ "\"idBandeira\": " + idBandeira + ","
					+ "\"idPagamento\": \"0\", "
					+ "\"idEstabelecimento\": \"" +  numeroEstabelecimento + "\","
					+ "\"descricaoProduto\": \"" + bandeira + "\","
					+ "\"codigoAutorizacao\": \"" + tefVO.getProperty("AUTORIZACAO").toString() + "\","
					+ "\"nsu\": \"" + tefVO.getProperty("NUMNSU").toString() + "\","
					+ "\"tid\": \"0\","
					+ "\"taxaAdministrativa\": " + tefVO.getProperty("VLRTAXA").toString() + ","
					+ "\"numeroCartao\": \"\","
					+ "\"dataPagamento\": \"" + dataTransacao + "\","
					+ "\"valorTransacao\": " + tefVO.getProperty("VLRTRANSACAO").toString()
				+ "}";

		return json;
	}

	private String gerarJsonTef(PersistenceEvent persistenceEvent) throws Exception {

		DynamicVO tefVO = (DynamicVO) persistenceEvent.getVo();

		String dataTransacao = tefVO.getProperty("DTTRANSACAO").toString();
		Calendar dtTransacao = null;
		if (dataTransacao != null) {
			dtTransacao = Calendar.getInstance();
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				dtTransacao.setTime(format.parse(dataTransacao));
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dataTransacao = format.format(dtTransacao.getTime());
			} catch (Exception e) {
				throw new Exception("Falha na Data da transação");
			}
		}

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
						+ "\"dataTransacao\": \"" + dataTransacao + "\","
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

	private String gerarJsonFinanceiro(ResultSet tgffin, String idUsuario) throws Exception {

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
						+ "\"dataPrazo\": \"" +  dataPrazo + "\" "
				+ "}";

		return json;
	}


	private void enviarDados(String json) throws Exception {

		this.qtdException++;
		try {
			String url = this.urlApi + "/v2/caixas/cartoes";
			String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
			IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
		} catch (Exception e) {
			if (this.qtdException < 2) {
				enviarDados(json);
			}
//			throw new Exception("Falha: " + e.getMessage() + "\n" + json);
		}
		this.qtdException = 0;
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
			consulta.append(" 	NUCOMPENS, ");
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
					jsonCartao = this.gerarJsonCartao(persistenceEvent,
													result.getBigDecimal("NUNOTA"),
													result.getBigDecimal("NUCOMPENS"),
													result.getBigDecimal("NUMNOTA"));
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

//				if (true) {
//					throw new Exception(json);
//				}

//				PROCESSO V2
				this.enviarDados(json);

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

	private void cancelarCartao(PersistenceEvent persistenceEvent) throws Exception {
		DynamicVO tefVO = (DynamicVO) persistenceEvent.getVo();
		String url   = this.urlApi + "/v2/caixas/cartoes/" + tefVO.asBigDecimal("IDENTIFICACAOTEF").toString();
		String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
		IntegrationApi.sendHttp(url, "{\"idUsuario\":" + tefVO.asBigDecimal("CODUSU").toString() + "}", "DELETE", "Bearer " + token);
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
		this.cancelarCartao(arg0);
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