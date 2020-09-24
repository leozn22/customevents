package br.com.goup.snkcustomevents.financial;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FinanceiroEvento extends SnkIntegrationsApi implements EventoProgramavelJava  {

    private int qtdException = 0;

    public FinanceiroEvento() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        if(modifingFields.isModifing("DHBAIXA")) {
            if (modifingFields.getNewValue("DHBAIXA") != null) {
                this.enviarFinanceiro(persistenceEvent);
            } else {
                this.estornoFinanceiro(persistenceEvent);
            }
        }

        this.enviarCredito(persistenceEvent);
    }

    private void enviarFinanceiro(PersistenceEvent persistenceEvent) throws Exception {

        if (this.eIntegracao(persistenceEvent)) {

            String json = this.gerarJsonV2(persistenceEvent);

            String url = this.urlApi + "/v2/caixas/pagamentos";
//
//            if (ePromessa) {
//                url = this.urlApi + "/v2/caixas/depositos";
//            }

            this.enviarDados("POST", url, json);
        }
    }

    private void estornoFinanceiro(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO financeiroVo = (DynamicVO) persistenceEvent.getVo();
        boolean ePromessa = financeiroVo.asInt("CODTIPTIT") == 15;

        if (this.eIntegracao(persistenceEvent) || ePromessa) {

            String url = this.urlApi + "/v2/caixas/pagamentos/" + financeiroVo.asBigDecimal("NUFIN").toString();
            this.enviarDados("DELETE", url, "{\"idUsuario\":"
                    + financeiroVo.asBigDecimal("CODUSU").toString() + "}");
        }
    }

    private void enviarCredito(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO financeiroVo = (DynamicVO) persistenceEvent.getVo();
        if (financeiroVo.asInt("RECDESP") == -1
                && financeiroVo.asInt("CODTIPOPER") == 4107
                && persistenceEvent.getModifingFields().isModifing("CODPARC")
                && (
                (
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
            String url = this.urlApi + "/v2/caixas/pagamentos";
            this.enviarDados("POST", url, json);
        }
    }


    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    private void enviarDados(String verboHttp, String url, String json) throws Exception {
        this.qtdException++;
        try {
            String token = IntegrationApi.getToken(this.urlApi + "/oauth/token?grant_type=client_credentials", "POST", "Basic c2Fua2h5YXc6U0Bua2h5QDJV");
            IntegrationApi.sendHttp(url, json, verboHttp, "Bearer " + token);
        } catch (Exception e) {
            if (this.qtdException < 3) {
                enviarDados(verboHttp, url, json);
            } else {
                throw new Exception("Falha: " + e.getMessage() + "\n" + json);
            }
        }
        this.qtdException = 0;
    }

    private boolean eTipoTituloValido(int idTipoTitulo) {

        return idTipoTitulo == 2        //DINHEIRO
                || idTipoTitulo == 3    //CHEQUE
                || idTipoTitulo == 4    //BOLETO
                || idTipoTitulo == 15   //DEPOSITO
                || idTipoTitulo == 26;  //CREDITO ZAP
    }

    private boolean eTipoOperacaoValida(int idTipoOperacao) {

        return  idTipoOperacao == 3117
                || idTipoOperacao == 4401
                || idTipoOperacao == 4104
                || idTipoOperacao == 3107;
    }

    private boolean eIntegracao(PersistenceEvent persistenceEvent) {

        DynamicVO financeiroVo = (DynamicVO) persistenceEvent.getVo();
        ModifingFields modifingFields = persistenceEvent.getModifingFields();

        int idCaixa = (modifingFields.isModifing("CODCTABCOINT")
                ? Integer.parseInt(modifingFields.getNewValue("CODCTABCOINT").toString())
                : financeiroVo.asInt("CODCTABCOINT"));

        boolean ePagamentoCaixa = financeiroVo.asInt("CODTIPOPER") == 3106
                && (AuthenticationInfo.getCurrent().getUserID().intValue() == 181
                    || AuthenticationInfo.getCurrent().getUserID().intValue() == 305);
//                && (idCaixa == 21 || idCaixa == 23);

        return  (financeiroVo.asInt("RECDESP") == 1 && (eTipoTituloValido(financeiroVo.asInt("CODTIPTIT"))
                && eTipoOperacaoValida(financeiroVo.asInt("CODTIPOPER"))) || ePagamentoCaixa);

    }

    private String gerarJsonV2(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO financialVO 		  = (DynamicVO) persistenceEvent.getVo();
        ModifingFields modifingFields = persistenceEvent.getModifingFields();

        String dataBaixa = (modifingFields.isModifing("DHBAIXA")
                ? modifingFields.getNewValue("DHBAIXA").toString()
                : financialVO.asBigDecimal("DHBAIXA").toString());
        Calendar dtBaixa = null;
        if (dataBaixa != null) {
            dtBaixa = Calendar.getInstance();
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                dtBaixa.setTime(format.parse(dataBaixa));
                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dataBaixa = format.format(dtBaixa.getTime());
            } catch (Exception e) {
                throw new Exception("Falha na Data de Baixa");
            }
        }

        String dataPrazo = (modifingFields.isModifing("DTPRAZO")
                ? modifingFields.getNewValue("DTPRAZO").toString()
                : (financialVO.getProperty("DTPRAZO") != null ? financialVO.getProperty("DTPRAZO").toString() : financialVO.getProperty("DHBAIXA").toString()));

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

        boolean ePromessa = (modifingFields.isModifing("BH_NRODEPOSITO")
                && modifingFields.getNewValue("BH_NRODEPOSITO") != null)
                || financialVO.getProperty("BH_NRODEPOSITO") != null;

        String idTipoTitulo = (modifingFields.isModifing("CODTIPTIT")
                ? modifingFields.getNewValue("CODTIPTIT").toString()
                : (modifingFields.isModifing("NUCOMPENS") && modifingFields.getNewValue("NUCOMPENS") != null
                ? "26"
                : financialVO.asBigDecimal("CODTIPTIT").toString()));

        String valorBaixa = (modifingFields.isModifing("VLRBAIXA")
                ? modifingFields.getNewValue("VLRBAIXA").toString()
                : financialVO.asBigDecimal("VLRBAIXA").toString());

        String valorDesdobramento = (modifingFields.isModifing("VLRDESDOB")
                ? modifingFields.getOldValue("VLRDESDOB").toString()
                : financialVO.asBigDecimal("VLRDESDOB").toString());

        if (ePromessa) {
            idTipoTitulo 	   = "15";
            valorBaixa   	   = financialVO.asBigDecimal("BH_VLRDEPOSITO").toString();
            valorDesdobramento = financialVO.asBigDecimal("BH_VLRDEPOSITO").toString();
        }

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

                + "\"idTipoTitulo\": " + idTipoTitulo + ","

                + "\"dataBaixa\": \"" + dataBaixa + "\","

                + "\"valorBaixa\": \"" + valorBaixa + "\","

                + "\"valorDesdobramento\": \"" + valorDesdobramento + "\","

                + "\"recebimentoCartao\": \"" +  (financialVO.getProperty("RECEBCARTAO") != null ? financialVO.asString("RECEBCARTAO") : "") + "\" ,"
                + "\"dataPrazo\": \"" + dataPrazo + "\" "
                + "}";

        if (ePromessa) {
            json = "{" + "\"financeiroSankhya\": " + json + ",";
            json = json + "\"promessaSankhya\": {"
                    + "\"idNota\": " + financialVO.asBigDecimal("NUNOTA") + ","
                    + "\"numeroDeposito\": \"" +  financialVO.asString("BH_NRODEPOSITO") + "\", "
                    + "\"idContaBancaria\": " + financialVO.asBigDecimal("BH_CODCTABCOINTDEPOSITO").toString()
                    + "} }";
        }

        return json;
    }

    private String gerarJsonDespesa(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO financialVO 		  = (DynamicVO) persistenceEvent.getVo();

        String dataPrazo = financialVO.getProperty("DTPRAZO").toString();

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
                + "\"dataPrazo\": \"" + dataPrazo + "\" "
                + "}";

        return json;
    }
}
