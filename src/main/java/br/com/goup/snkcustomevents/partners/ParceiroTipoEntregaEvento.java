package br.com.goup.snkcustomevents.partners;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParceiroTipoEntregaEvento extends SnkIntegrationsApi implements EventoProgramavelJava {

    private int qtdException = 0;

    public ParceiroTipoEntregaEvento() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private static Map<String, String> CAMPOS_INTEGRACAO_PARCEIRO = new HashMap<String, String>() {{
        put("PADRAO", "padrao");
    }};

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

    private int localizarCodigoParceiro(Object pk) throws Exception {

        JapeWrapper parceiroDAO;
        try {
            parceiroDAO = JapeFactory.dao("Parceiro");
        } catch (Exception e) {
            throw new Exception("Instancia Parceiro");
        }

        DynamicVO parceiroVO;
        try {
            parceiroVO = parceiroDAO.findByPK(pk);
        } catch (Exception e) {
            throw new Exception("Find Parceiro");
        }

        if (parceiroVO.getProperty("AD_CODPARCEXT") != null && parceiroVO.asInt("AD_CODPARCEXT") > 0) {
           return parceiroVO.asInt("AD_CODPARCEXT");
        }
        return 0;
    }

    private String localizarNomeTipoEntrega(Object pk) throws Exception {

        JapeWrapper tipoEntregaDAO;
        try {
            tipoEntregaDAO = JapeFactory.dao("TxpZapTipoEntrega");
        } catch (Exception e) {
            throw new Exception("Instancia Tipo de Entrega");
        }

        DynamicVO tipoEntregaVO;
        try {
            tipoEntregaVO = tipoEntregaDAO.findByPK(pk);
        } catch (Exception e) {
            throw new Exception("Find Tipo de Entrega");
        }

        return tipoEntregaVO.getProperty("NOME").toString();
    }

    private void inserir(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO parceiroTipoEntregaVo = (DynamicVO) persistenceEvent.getVo();

        int codigoParceiro = this.localizarCodigoParceiro(parceiroTipoEntregaVo.getProperty("CODPARC"));

        if (codigoParceiro > 0) {
            String descricaoTipoEntrega = this.localizarNomeTipoEntrega(parceiroTipoEntregaVo.getProperty("CODTET"));

            String dataAlteracao = "";
            Calendar dtAlteracao = Calendar.getInstance();
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dataAlteracao = format.format(dtAlteracao.getTime());
            } catch (Exception e) {
                throw new Exception("Falha na Data Alteração");
            }

            ParceiroTipoEntrega parceiroTipoEntrega = new ParceiroTipoEntrega();
            parceiroTipoEntrega.setIdParceiroTipoEntrega(UUID.randomUUID().toString().toUpperCase());
            parceiroTipoEntrega.setCodigoParceiro(codigoParceiro);
            parceiroTipoEntrega.setNomeTipoEntrega(descricaoTipoEntrega);
            String valorIntegracao = parceiroTipoEntregaVo.getProperty("PADRAO") != null ? parceiroTipoEntregaVo.getProperty("PADRAO").toString(): "N";
            valorIntegracao = "S".equals(valorIntegracao) ? "SIM" : valorIntegracao;
            valorIntegracao = "N".equals(valorIntegracao) ? "NÃO" : valorIntegracao;
            parceiroTipoEntrega.setPadrao(valorIntegracao);
            parceiroTipoEntrega.setAtivo("SIM");
            parceiroTipoEntrega.setDataCadastro(dataAlteracao);
            parceiroTipoEntrega.setDataAlteracao(dataAlteracao);
            parceiroTipoEntrega.setIdCadastrador("8CCE8E13-9573-4128-9BF1-C740AD16347E");
            parceiroTipoEntrega.setIdAlterador("8CCE8E13-9573-4128-9BF1-C740AD16347E");
            parceiroTipoEntrega.setNomeUsuarioComputador(AuthenticationInfo.getCurrent().getName());
            parceiroTipoEntrega.setIp(AuthenticationInfo.getCurrent().getIpRequest());

            Gson gson = new Gson();
            String json = gson.toJson(parceiroTipoEntrega);
            String url = this.urlApi + "/v2/parceiros/tipos-entrega?esperar=true&usuario=" + AuthenticationInfo.getCurrent().getName();
            this.enviarDados("POST", url, "[" + json + "]");
        }
    }

    private void remover(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO ParceiroTipoEntregaVo = (DynamicVO) persistenceEvent.getVo();

        int codigoParceiro = this.localizarCodigoParceiro(ParceiroTipoEntregaVo.getProperty("CODPARC"));

        if (codigoParceiro > 0) {
            String descricaoTipoEntrega = this.localizarNomeTipoEntrega(ParceiroTipoEntregaVo.getProperty("CODTET"));

            String dataAlteracao = "";
            Calendar dtAlteracao = Calendar.getInstance();
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dataAlteracao = format.format(dtAlteracao.getTime());
            } catch (Exception e) {
                throw new Exception("Falha na Data Alteração");
            }

            ParceiroTipoEntrega parceiroTipoEntrega = new ParceiroTipoEntrega();
            parceiroTipoEntrega.setCodigoParceiro(codigoParceiro);
            parceiroTipoEntrega.setNomeTipoEntrega(descricaoTipoEntrega);
            parceiroTipoEntrega.setPadrao("NÃO");
            parceiroTipoEntrega.setAtivo("NÃO");
            parceiroTipoEntrega.setDataAlteracao(dataAlteracao);
            parceiroTipoEntrega.setIdAlterador("8CCE8E13-9573-4128-9BF1-C740AD16347E");
            parceiroTipoEntrega.setNomeUsuarioComputador(AuthenticationInfo.getCurrent().getClientName());
            parceiroTipoEntrega.setIp(AuthenticationInfo.getCurrent().getClientIP());

            Gson gson = new Gson();
            String json = gson.toJson(parceiroTipoEntrega);
            String url = this.urlApi + "/v2/parceiros/tipos-entrega?esperar=true&usuario="+ AuthenticationInfo.getCurrent().getName();
            this.enviarDados("PUT", url, "[" + json + "]");
        }
    }

    private void atualizar(PersistenceEvent persistenceEvent) throws Exception {

        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        DynamicVO ParceiroTipoEntregaVo          = (DynamicVO) persistenceEvent.getVo();

        int codigoParceiro = this.localizarCodigoParceiro(ParceiroTipoEntregaVo.getProperty("CODPARC"));

        if (codigoParceiro > 0 && modifingFields.isModifing("PADRAO")) {
            String descricaoTipoEntrega = this.localizarNomeTipoEntrega(ParceiroTipoEntregaVo.getProperty("CODTET"));
            ParceiroTipoEntrega parceiroTipoEntrega = new ParceiroTipoEntrega();
            parceiroTipoEntrega.setCodigoParceiro(codigoParceiro);
            parceiroTipoEntrega.setNomeTipoEntrega(descricaoTipoEntrega);

            String valorIntegracao = modifingFields.getNewValue("PADRAO").toString();
            valorIntegracao = "S".equals(valorIntegracao) ? "SIM" : valorIntegracao;
            valorIntegracao = "N".equals(valorIntegracao) ? "NÃO" : valorIntegracao;
            parceiroTipoEntrega.setPadrao(valorIntegracao);

            String dataAlteracao = "";
            Calendar dtAlteracao = Calendar.getInstance();
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dataAlteracao = format.format(dtAlteracao.getTime());
            } catch (Exception e) {
                throw new Exception("Falha na Data Alteração");
            }

            parceiroTipoEntrega.setDataAlteracao(dataAlteracao);
            parceiroTipoEntrega.setIdAlterador("8CCE8E13-9573-4128-9BF1-C740AD16347E");
            parceiroTipoEntrega.setNomeUsuarioComputador(AuthenticationInfo.getCurrent().getClientName());
            parceiroTipoEntrega.setIp(AuthenticationInfo.getCurrent().getClientIP());

            Gson gson = new Gson();
            String json = gson.toJson(parceiroTipoEntrega);
            String url = this.urlApi + "/v2/parceiros/tipos-entrega?esperar=true&usuario="+ AuthenticationInfo.getCurrent().getName();
            this.enviarDados("PUT", url, "[" + json + "]");
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
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.inserir(persistenceEvent);
        }
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.atualizar(persistenceEvent);
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.remover(persistenceEvent);
        }
    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
