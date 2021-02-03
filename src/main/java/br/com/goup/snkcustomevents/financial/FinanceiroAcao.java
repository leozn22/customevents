package br.com.goup.snkcustomevents.financial;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import org.json.JSONArray;
import org.json.JSONObject;

public class FinanceiroAcao extends SnkIntegrationsApi implements AcaoRotinaJava {

	private int qtdException = 0;

	public FinanceiroAcao() {
        this.exigeAutenticacao = true;	
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

	private boolean isCredito = true;

	private BigDecimal valorTotalTransacao = BigDecimal.ZERO;

	private String gerarJsonCartao(ContextoAcao arg0, Registro registro) throws Exception {

		QueryExecutor tgfTef = arg0.getQuery();

		StringBuffer consulta = new StringBuffer();
		consulta.append("    SELECT *");
		consulta.append("      FROM TGFTEF  ");
		consulta.append("     WHERE NUFIN = " + registro.getCampo("NUFIN").toString());

		tgfTef.nativeSelect(consulta.toString());
		tgfTef.next();

		String comprovante 			 = tgfTef.getString("COMPROVANTE");
		String bandeira    			 = tgfTef.getString("BANDEIRA");
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
			if (valor.contains("POS=") || valor.contains("823982346832235")) {

				numeroEstabelecimento = valor.replaceAll("\\s*([0-9]+).*", "$1").trim();
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

		// Usado em ambiente de teste
		if (numeroEstabelecimento.equals("823982346832235") || numeroEstabelecimento == null || numeroEstabelecimento.equals("")) {
			numeroEstabelecimento = "1023441710";
		} else {
			numeroEstabelecimento = numeroEstabelecimento.trim().substring(1, numeroEstabelecimento.trim().length() - 4);
			if (numeroEstabelecimento.startsWith("0")) {
				numeroEstabelecimento = numeroEstabelecimento.substring(1);
			}
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

		BigDecimal idProduto  = null;
		BigDecimal idBandeira = null;
		String descrProd      = null;

		QueryExecutor adTccCab = arg0.getQuery();
		adTccCab.nativeSelect(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPRODLOJA LIKE '%" + bandeira
				+ "%' AND TIPOVENDA = '" + tipoVenda +  "'");

		if (adTccCab.next()) {
			idProduto  = adTccCab.getBigDecimal("NUPRO");
			idBandeira = adTccCab.getBigDecimal("NUBAN");
			descrProd  = adTccCab.getString("DESCRPROD");
		}
		adTccCab.close();

		if (idProduto == null) {
			String[] valores = bandeira.split(" ");
			String valor = valores[0].trim();

			adTccCab.nativeSelect(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPRODLOJA LIKE '%" + valor
					+ "%' AND TIPOVENDA = '" + tipoVenda+  "'");
			if (adTccCab.next()) {
				idProduto  = adTccCab.getBigDecimal("NUPRO");
				idBandeira = adTccCab.getBigDecimal("NUBAN");
				descrProd  = adTccCab.getString("DESCRPROD");
			}
			adTccCab.close();
		}

		adTccCab.nativeSelect("SELECT NUCTRL FROM AD_TCCCAB WHERE NUNOTA = " + registro.getCampo("NUNOTA")
				+ " AND NSU = '" + tgfTef.getString("NUMNSU") + "'"
				+ " AND CODAUT = '" + tgfTef.getString("AUTORIZACAO") + "'");
		boolean isAdTccCab = adTccCab.next();

		tgfTef.nativeSelect(consulta.toString());
		tgfTef.next();
		if (!isAdTccCab) {
			JapeWrapper cartaoDAO   = JapeFactory.dao("AD_TCCCAB");
			FluidCreateVO creCartao = cartaoDAO.create();

			creCartao.set("CODTIPPROD", idProduto);
			creCartao.set("DESCRTIPPROD", descrProd);
			creCartao.set("IDPAG", "0");
			creCartao.set("TID", "0");
			creCartao.set("TAXAADM", tgfTef.getBigDecimal("VLRTAXA"));
			creCartao.set("NUBAN", idBandeira);
			creCartao.set("NUESTABELECIMENTO", numeroEstabelecimento);
			creCartao.set("DTPAGCARTAO", tgfTef.getTimestamp("DTTRANSACAO"));
			creCartao.set("CODAUT", tgfTef.getString("AUTORIZACAO"));
			creCartao.set("NUNOTA", registro.getCampo("NUNOTA"));
//			creCartao.set("VLRTRANSACAO", tgfTef.getBigDecimal("VLRTRANSACAO"));
			creCartao.set("VLRTRANSACAO", this.valorTotalTransacao);
			creCartao.set("NSU", tgfTef.getString("NUMNSU"));
			creCartao.save();
		}

	    String dataTransacao = tgfTef.getString("DTTRANSACAO");
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
				+ "\"idNota\": " + registro.getCampo("NUNOTA") + ","
				+ "\"idTipoProduto\": " + idProduto + ","
				+ "\"idBandeira\": " + idBandeira + ","
				+ "\"idPagamento\": \"0\", "
				+ "\"idEstabelecimento\": \"" +  numeroEstabelecimento + "\","
				+ "\"descricaoProduto\": \"" + bandeira + "\","
				+ "\"codigoAutorizacao\": \"" + tgfTef.getString("AUTORIZACAO") + "\","
				+ "\"nsu\": \"" + tgfTef.getString("NUMNSU") + "\","
				+ "\"tid\": \"0\","
				+ "\"taxaAdministrativa\": " + tgfTef.getString("VLRTAXA") + ","
				+ "\"numeroCartao\": \"\","
				+ "\"dataPagamento\": \"" + dataTransacao + "\","
				+ "\"valorTransacao\": " + tgfTef.getString("VLRTRANSACAO")
				+ "}";

		String jsonTef = this.gerarJsonTef(tgfTef);

		return json + ", " + jsonTef;
	}

	private String gerarJsonTef(QueryExecutor tgfTef) throws Exception {

		String dataTransacao = tgfTef.getString("DTTRANSACAO");
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
				+ "\"idTef\": " + tgfTef.getString("IDENTIFICACAOTEF") + ","
				+ "\"idFinanceiro\": " + tgfTef.getString("NUFIN") + ","
				+ "\"rede\": " + tgfTef.getString("REDE") + ","
				+ "\"tipoDocumento\": " + tgfTef.getString("TIPODOC") + ","
				+ "\"numeroComprovante\": \"" + tgfTef.getString("NUMCV") + "\","
				+ "\"numeroDocumento\": \"" + tgfTef.getString("NUMDOC") + "\","
				+ "\"nsu\": \"" + tgfTef.getString("NUMNSU") + "\","
				+ "\"numeroPontoVenda\": \"" + tgfTef.getString("NUMPV") + "\","
				+ "\"autorizacao\": \"" + tgfTef.getString("AUTORIZACAO") + "\","
				+ "\"desdobramento\": " + tgfTef.getString("DESDOBRAMENTO") + ","
				+ "\"dataTransacao\": \"" + dataTransacao + "\","
				+ "\"valorTransacao\": \"" + tgfTef.getString("VLRTRANSACAO") + "\","
				+ "\"valorTaxa\": \"" + tgfTef.getString("VLRTAXA") + "\","
				+ "\"bandeira\": \"" + tgfTef.getString("BANDEIRA") + "\","
				+ "\"confirmado\": \"" + tgfTef.getString("CONFIRMADO") + "\","
				+ "\"idUsuario\": \"" + tgfTef.getString("CODUSU") + "\","
				+ "\"processo\": \"" + tgfTef.getString("PROCESSO") + "\","
				+ "\"comprovante\": \"" + tgfTef.getString("COMPROVANTE").replace("\"", "'")+ "\","
				+ "\"nomeRede\": \"" + tgfTef.getString("NOMEREDE") + "\","
				+ "\"operacao\": \"" + tgfTef.getString("OPERACAOTEF") + "\""
				+ "}";

		return json;
	}

	private String gerarJsonPromessa(ContextoAcao arg0, Registro registro) throws Exception {
//
		QueryExecutor tzaPrm = arg0.getQuery();

		StringBuffer consulta = new StringBuffer();
		consulta.append("    SELECT NUNOTA,");
		consulta.append("    		NUFINDESPADIANT,  ");
		consulta.append("    		NRODESPOSITO,  ");
		consulta.append("    		VALORDEPOSITO,  ");
		consulta.append("    		CODCTABCOINT  ");
		consulta.append("      FROM TZAPRM  ");
		consulta.append("     WHERE NUMNOTA = " + registro.getCampo("NUMNOTA").toString());
		consulta.append("     AND NRODESPOSITO = '" + registro.getCampo("BH_NRODEPOSITO").toString() + "'");

		tzaPrm.nativeSelect(consulta.toString());
		String idAdiantamento = "0";
		if (tzaPrm.next()) {
			idAdiantamento = tzaPrm.getString("NUFINDESPADIANT");
		}

//		this.valorTotalTransacao = new BigDecimal(tzaPrm.getString("VALORDEPOSITO"));
		this.valorTotalTransacao = new BigDecimal(registro.getCampo("BH_VLRDEPOSITO").toString());

		String json = "\"promessaSankhya\": {"
//				+ "\"idNota\": " + registro.getCampo("NUNOTA").toString() + ","
				+ "\"idAdiantamento\": " + idAdiantamento + ","
				+ "\"numeroDeposito\": \"" +  registro.getCampo("BH_NRODEPOSITO").toString() + "\", "
//				+ "\"idContaBancaria\": " + tzaPrm.getString("CODCTABCOINT")
				+ "\"idContaBancaria\": " + registro.getCampo("BH_CODCTABCOINTDEPOSITO").toString()
				+ "}";

		return json;
	}

	private String gerarJsonFinanceiro(Registro registro) throws Exception {

		String dataBaixa = registro.getCampo("DHBAIXA") != null ? registro.getCampo("DHBAIXA").toString() : null;
		Calendar dtBaixa = null;
		if (dataBaixa != null) {
			dtBaixa = Calendar.getInstance();
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				dtBaixa.setTime(format.parse(dataBaixa));
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dataBaixa = "\"" + format.format(dtBaixa.getTime()) + "\"" ;
			} catch (Exception e) {
				throw new Exception("Falha na Data de Baixa");
			}
		}

		String dataPrazo = (registro.getCampo("DTPRAZO") != null ? registro.getCampo("DTPRAZO").toString() : dataBaixa);

		Calendar dtPrazo = Calendar.getInstance();;
		if (dataPrazo != null) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				dtPrazo.setTime(format.parse(dataPrazo));
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dataPrazo = format.format(dtPrazo.getTime());
			} catch (Exception e) {
				throw new Exception("Falha na Data prazo");
			}
		}

		if (this.valorTotalTransacao.compareTo(BigDecimal.ZERO) <= 0) {
			this.valorTotalTransacao = new BigDecimal(registro.getCampo("VLRDESDOB").toString());
		}

		BigDecimal valorBaixa = (registro.getCampo("VLRBAIXA") != null ? new BigDecimal(registro.getCampo("VLRBAIXA").toString()) : BigDecimal.ZERO);

		if (registro.getCampo("CODTIPTIT").toString().equals("15")
				|| (registro.getCampo("RECEBCARTAO") != null
					&& registro.getCampo("RECEBCARTAO").toString().equals("S"))) {
			valorBaixa = this.valorTotalTransacao;
		}

		String idUsuario = (registro.getCampo("CODUSUBAIXA") != null
				? registro.getCampo("CODUSUBAIXA").toString() : registro.getCampo("CODUSU").toString());

		String tipoOperacao = registro.getCampo("CODTIPOPER").toString();
		if ((tipoOperacao.equalsIgnoreCase("4100") && (registro.getCampo("NUCOMPENS") != null)) || tipoOperacao.equalsIgnoreCase("4400") || tipoOperacao.equalsIgnoreCase("4107")) {
			tipoOperacao = "4401";
		}

		String tipoTitulo = (registro.getCampo("NUCOMPENS") != null
				&& !tipoOperacao.equals("4401") ? "26"
				: registro.getCampo("CODTIPTIT").toString());

		if (registro.getCampo("CODTIPOPER").toString().equalsIgnoreCase("4107")) {
			tipoTitulo = "15";
		}

		String json = "{"
				+ "\"idFinanceiro\": " + registro.getCampo("NUFIN").toString() + ","
				+ "\"idEmpresa\": " + registro.getCampo("CODEMP").toString() + ","
				+ "\"idNota\": " + registro.getCampo("NUNOTA") + ","
				+ "\"numeroNota\": " + registro.getCampo("NUMNOTA").toString() + ","
				+ "\"idParceiro\": " + registro.getCampo("CODPARC").toString() + ","
				+ "\"idTipoOperacao\": " + tipoOperacao + ","
				+ "\"idUsuarioBaixa\": " + idUsuario + ","
				 
				+ "\"idTipoTitulo\": " + tipoTitulo  + ","

				+ "\"dataBaixa\": " + dataBaixa + ","

				+ "\"valorBaixa\": \"" + valorBaixa + "\","

				+ "\"valorDesdobramento\": \"" + this.valorTotalTransacao.toString() + "\","

				+ "\"recebimentoCartao\": \"" + (registro.getCampo("RECEBCARTAO") != null ? registro.getCampo("RECEBCARTAO").toString() : "") + "\" ,"
				+ "\"dataPrazo\": \"" + dataPrazo + "\" "
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
			}
//			throw new Exception("Falha: " + e.getMessage() + "\n" + json);
		}
		this.qtdException = 0;
	}
	
	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		 String json = "";
		String mensagem = "";
	        for (Registro registro: arg0.getLinhas()) {

	        	this.valorTotalTransacao = BigDecimal.ZERO;

				String jsonCartao = "";
	        	if (registro.getCampo("RECEBCARTAO") != null && registro.getCampo("RECEBCARTAO").toString().equalsIgnoreCase("S")) {

					try {
						jsonCartao = this.gerarJsonCartao(arg0, registro);
					} catch (Exception e) {
						throw new Exception("Falha ao gerar Cartão " + e.getMessage());
					}

				}

				String jsonPromessa = "";
				if (registro.getCampo("CODTIPTIT").toString().equals("15")) {
					try {
						jsonPromessa = this.gerarJsonPromessa(arg0, registro);
					} catch (Exception e) {
						throw new Exception("Falha ao gerar promessa " + e.getMessage());
					}
				}

				json = this.gerarJsonFinanceiro(registro);

	        	if (!jsonCartao.trim().isEmpty()) {

	        		json       = "{" + "\"financeiroSankhya\": "
							+ json + ", "
							+ jsonCartao
							+ "} ";
				}

	        	if (!jsonPromessa.trim().isEmpty()) {

					json       = "{" + "\"financeiroSankhya\": "
							+ json + ", "
							+ jsonPromessa
							+ "} ";
				}

			 	if (!json.isEmpty()) {

	 	            String url   = this.urlApi + "/v2/caixas/pagamentos";
	 	            if (registro.getCampo("RECEBCARTAO") != null && registro.getCampo("RECEBCARTAO").toString().equalsIgnoreCase("S")) {
						url   = this.urlApi + "/v2/caixas/cartoes";
					}

					if (registro.getCampo("CODTIPTIT").toString().equals("15")) {
						url   = this.urlApi + "/v2/caixas/depositos";
					}

//	 	            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
//	 	            try {
//	 	            	IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
//	 	            } catch (Exception e) {
//	 	            	throw new Exception("Falha: " + e.getMessage() + "\n" + json);
//					}
//					if (true) {
//						throw new Exception(json);
//					}
					this.enviarDadosV2("POST", url, json);
	 	            mensagem = "Solicitacao enviada com sucesso!";

	 	        }
	        }

	        if (!mensagem.equals("")) {
				arg0.setMensagemRetorno(mensagem);
	        }
		
	}

}
