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
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import org.json.JSONArray;
import org.json.JSONObject;

public class FinanceiroAcao extends SnkIntegrationsApi implements AcaoRotinaJava {

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
		}

		String tipoVenda = "D";
		this.isCredito   = !tipoNegociacao.contains("DEBITO");
		if (tipoNegociacao.contains("PARC.LOJA")) {
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
		adTccCab.nativeSelect(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPROD LIKE '%" + bandeira
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

			adTccCab.nativeSelect(" SELECT  NUPRO, NUBAN, DESCRPROD FROM AD_TCCPRO WHERE DESCRPROD LIKE '%" + valor
					+ "%' AND TIPOVENDA = '" + tipoVenda+  "'");
			if (adTccCab.next()) {
				idProduto  = adTccCab.getBigDecimal("NUPRO");
				idBandeira = adTccCab.getBigDecimal("NUBAN");
				descrProd  = adTccCab.getString("DESCRPROD");
			}
			adTccCab.close();
		}

		adTccCab.nativeSelect("SELECT NUCTRL FROM AD_TCCCAB WHERE NUNOTA = " + registro.getCampo("NUNOTA"));
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
			creCartao.set("NUESTABELECIMENTO", numeroEstabelecimento.trim().substring(2, numeroEstabelecimento.trim().length() - 4));
			creCartao.set("DTPAGCARTAO", tgfTef.getTimestamp("DTTRANSACAO"));
			creCartao.set("CODAUT", tgfTef.getString("AUTORIZACAO"));
			creCartao.set("NUNOTA", registro.getCampo("NUNOTA"));
			creCartao.set("VLRTRANSACAO", tgfTef.getBigDecimal("VLRTRANSACAO"));
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
				+ "\"idEstabelecimento\": \"" +  numeroEstabelecimento.trim().substring(2, numeroEstabelecimento.trim().length() - 4) + "\","
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

		String idUsuario = (registro.getCampo("CODUSUBAIXA") != null
				? registro.getCampo("CODUSUBAIXA").toString() : registro.getCampo("CODUSU").toString());

		String json = "{"
				+ "\"idFinanceiro\": " + registro.getCampo("NUFIN").toString() + ","
				+ "\"idEmpresa\": " + registro.getCampo("CODEMP").toString() + ","
				+ "\"idNota\": " + registro.getCampo("NUNOTA") + ","
				+ "\"numeroNota\": " + registro.getCampo("NUMNOTA").toString() + ","
				+ "\"idParceiro\": " + registro.getCampo("CODPARC").toString() + ","
				+ "\"idTipoOperacao\": " + registro.getCampo("CODTIPOPER").toString() + ","
				+ "\"idUsuarioBaixa\": " + idUsuario + ","
				 
				+ "\"idTipoTitulo\": " + (registro.getCampo("NUCOMPENS") != null
										&& !registro.getCampo("CODTIPOPER").toString().equals("4401") ? "26"
				: registro.getCampo("CODTIPTIT").toString()) + ","

				+ "\"dataBaixa\": " + dataBaixa + ","

				+ "\"valorBaixa\": \"" + (registro.getCampo("VLRBAIXA") != null ? registro.getCampo("VLRBAIXA").toString(): "") + "\","

				+ "\"valorDesdobramento\": \"" + this.valorTotalTransacao.toString() + "\","

				+ "\"recebimentoCartao\": \"" + (registro.getCampo("RECEBCARTAO") != null ? registro.getCampo("RECEBCARTAO").toString() : "") + "\" ,"
				+ "\"dataPrazo\": \"" + dataPrazo + "\" "
				+ "}";

		return json;
	}
	
	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		 String json = "";

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

				json = this.gerarJsonFinanceiro(registro);

	        	if (!jsonCartao.trim().isEmpty()) {

	        		json       = "{" + "\"financeiroSankhya\": "
							+ json + ", "
							+ jsonCartao
							+ "} ";
				}

			 	if (!json.isEmpty()) {
	 	            String url   = this.urlApi + "/v2/caixas/pagamentos";
	 	            if (registro.getCampo("RECEBCARTAO") != null && registro.getCampo("RECEBCARTAO").toString().equalsIgnoreCase("S")) {
						url   = this.urlApi + "/v2/caixas/cartoes";
					}
	 	            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
	 	            try {
	 	            	IntegrationApi.sendHttp(url, json, "POST", "Bearer " + token);
	 	            } catch (Exception e) {
	 	            	throw new Exception("Falha: " + e.getMessage() + "\n" + json);
					}
	 	            String mensagem = "Solicitacao enviada com sucesso!";
	 	            arg0.setMensagemRetorno(mensagem);
	 	        }
	        }       
		
	}

}
