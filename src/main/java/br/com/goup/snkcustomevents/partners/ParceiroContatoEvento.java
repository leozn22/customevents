package br.com.goup.snkcustomevents.partners;

import br.com.goup.snkcustomevents.SnkIntegrationsApi;
import br.com.goup.snkcustomevents.utils.IntegrationApi;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ParceiroContatoEvento extends SnkIntegrationsApi implements EventoProgramavelJava {

    private int qtdException = 0;
    private final String TIPO_CONTATO = "AD_TIPOCTT"; //P = Principal | O = Outros


    public ParceiroContatoEvento() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private static Map<String, String> CAMPOS_INTEGRACAO_CONTATO = new HashMap<String, String>() {{
        put("NOMECONTATO", "nome");
        put("DTNASC", "dataNascimento");
        put("EMAIL", "email");
        put("TELEFONE", "telefone");
        put("CELULAR", "celular");
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

    private void validarContatoPrincipal(PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO contatoVO = (DynamicVO) persistenceEvent.getVo();

        JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();

        NativeSql sql = new NativeSql(jdbc);

        StringBuffer consulta = new StringBuffer();

        consulta.append("    SELECT CODCONTATO,");
        consulta.append("	        NOMECONTATO");
        consulta.append("      FROM TGFCTT ");
        consulta.append("     WHERE CODCONTATO <> :CODCONTATO");
        consulta.append("       AND CODPARC = :CODPARC");
        consulta.append("       AND nullValue(AD_TIPOCTT, 'O') = 'P'");

        sql.setNamedParameter("CODCONTATO", contatoVO.asBigDecimal("CODCONTATO"));
        sql.setNamedParameter("CODPARC", contatoVO.asBigDecimal("CODPARC"));
        ResultSet result = sql.executeQuery(consulta.toString());

        if (result.next()) {
            throw new Exception("<font size='12'><b>Já existe um contato principal para esse parceiro: " + result.getBigDecimal("CODCONTATO") + " - " + result.getString("NOMECONTATO") + "</b></font>");
        }
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

    private void inserir(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO parceiroContatoVo = (DynamicVO) persistenceEvent.getVo();

        if (parceiroContatoVo.getProperty(TIPO_CONTATO) == null || !"P".equals(parceiroContatoVo.getProperty(TIPO_CONTATO).toString())) {
            return;
        }

        this.validarContatoPrincipal(persistenceEvent);

        boolean temIntegracao = false;

        int codigoParceiro = this.localizarCodigoParceiro(parceiroContatoVo.getProperty("CODPARC"));

        if (codigoParceiro > 0) {
            Parceiro parceiro = new Parceiro();
            parceiro.setCodigoParceiro(String.valueOf(codigoParceiro));
            ParceiroContato parceiroContato = new ParceiroContato();
            
            try {
                String valorIntegracao = null;

                for (Map.Entry<String, String> campo : CAMPOS_INTEGRACAO_CONTATO.entrySet()) {
                    valorIntegracao = parceiroContatoVo.getProperty(campo.getKey()) != null ? parceiroContatoVo.getProperty(campo.getKey()).toString() : null;

                    Field field = parceiroContato.getClass().getDeclaredField(campo.getValue());
                    field.setAccessible(true);

                    if (valorIntegracao != null) {
                        temIntegracao = true;

                        if (campo.getValue().contains("data")) {
                            Calendar data = Calendar.getInstance();
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                data.setTime(format.parse(valorIntegracao));
                                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                valorIntegracao = format.format(data.getTime());
                            } catch (Exception e) {
                                throw new Exception("Falha de Campo Data");
                            }
                        } else {
                            if (campo.getKey().equals("CELULAR")) {
                                if (valorIntegracao.substring(0, 2).equalsIgnoreCase("55")) {
                                    valorIntegracao = valorIntegracao.substring(2);
                                }

                                if (valorIntegracao.length() <= 10) {
                                    valorIntegracao = null;
                                }
                            }

                            if (campo.getKey().equals("TELEFONE")) {
                                if (valorIntegracao.substring(0, 2).equalsIgnoreCase("55")) {
                                    valorIntegracao = valorIntegracao.substring(2);
                                }
                            }
                        }

                        if (valorIntegracao != null && !valorIntegracao.isEmpty()) {
                            field.set(parceiroContato, valorIntegracao);
                        }
                    }
                }

                parceiro.setContatoPrincipal(parceiroContato);

                if (temIntegracao) {
                    Gson gson = new Gson();

                    String json = gson.toJson(parceiro);
                    String url = this.urlApi + "/v2/parceiros?esperar=true&usuario="+AuthenticationInfo.getCurrent().getName();
                    this.enviarDados("PUT", url, "[" + json + "]");
                }
            } catch (Exception e) {
                throw new Exception("Erro: " + e.getMessage() );
            }
        }
    }

    private void remover(PersistenceEvent persistenceEvent) throws Exception {
        return;
    }

    private void atualizar(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO parceiroContatoVo = (DynamicVO) persistenceEvent.getVo();

        if (parceiroContatoVo.getProperty(TIPO_CONTATO) == null || !"P".equals(parceiroContatoVo.getProperty(TIPO_CONTATO).toString())) {
            return;
        }

        this.validarContatoPrincipal(persistenceEvent);

        boolean temIntegracao = false;
        int codigoParceiro = this.localizarCodigoParceiro(parceiroContatoVo.getProperty("CODPARC"));

        if (codigoParceiro > 0) {
            Parceiro parceiro = new Parceiro();
            parceiro.setCodigoParceiro(String.valueOf(codigoParceiro));
            ParceiroContato parceiroContato = null;

            try {
                ModifingFields modifingFields = persistenceEvent.getModifingFields();

                for (Map.Entry<String, Object[]> campoSnk : modifingFields.entrySet()) {

                    String campo = CAMPOS_INTEGRACAO_CONTATO.get(campoSnk.getKey());
                    if (campo != null) {
                        temIntegracao = true;

                        if (parceiroContato == null) {
                            parceiroContato = new ParceiroContato();
                        }

                        String valorIntegracao = null;
                        if (modifingFields.getNewValue(campoSnk.getKey()) != null) {
                            valorIntegracao = modifingFields.getNewValue(campoSnk.getKey()).toString();
                        }

                        Field field = parceiroContato.getClass().getDeclaredField(campo);
                        field.setAccessible(true);

                        if (valorIntegracao != null) {
                            if (campo.contains("data")) {
                                Calendar data = Calendar.getInstance();
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                    data.setTime(format.parse(valorIntegracao));
                                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    valorIntegracao = format.format(data.getTime());
                                } catch (Exception e) {
                                    throw new Exception("Falha de Campo Data");
                                }
                            } else {
                                if (campoSnk.getKey().equals("CELULAR")) {
                                    if (valorIntegracao.substring(0, 2).equalsIgnoreCase("55")) {
                                        valorIntegracao = valorIntegracao.substring(2);
                                    }

                                    if (valorIntegracao.length() <= 10) {
                                        valorIntegracao = null;
                                    }
                                }

                                if (campoSnk.getKey().equals("TELEFONE")) {
                                    if (valorIntegracao.substring(0, 2).equalsIgnoreCase("55")) {
                                        valorIntegracao = valorIntegracao.substring(2);
                                    }
                                }
                            }

                            field.set(parceiroContato, valorIntegracao);
                        } else {
                            field.set(parceiroContato, "");
                        }
                    }
                }

                if (parceiroContato != null) {
                    parceiro.setContatoPrincipal(parceiroContato);
                }

                if (temIntegracao) {
                    Gson gson = new Gson();
                    String json = gson.toJson(parceiro);
                    String url = this.urlApi + "/v2/parceiros?esperar=true&usuario=" + AuthenticationInfo.getCurrent().getName();
                    this.enviarDados("PUT", url, "[" + json + "]");
                }
            } catch (Exception e) {
                throw new Exception("Erro: " + e.getMessage());
            }
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
