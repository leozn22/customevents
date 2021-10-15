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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ParceiroEvento extends SnkIntegrationsApi implements EventoProgramavelJava  {

    private int qtdException = 0;

    public ParceiroEvento() {
        this.exigeAutenticacao = true;
        this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
    }

    private static Map<String, String> CAMPOS_INTEGRACAO_PARCEIRO = new HashMap<String, String>() {{
//        put("CODPARC", "iddetpedido");
//        put("TIPPESSOA", "registro");
//        put("CLASSIFICMS", "pgtof");
        put("AD_CODPARCEXT", "codigoParceiro");
        put("RAZAOSOCIAL", "razaoSocial");
        put("NOMEPARC", "nomeFantasia");
        put("CGC_CPF", "cnpj");
        put("IDENTINSCESTAD", "inscricaoEstadual");
        put("INSCMUN", "inscricaoMunicipal");
//        put("ATIVO", "status");
        put("RETEMISS", "retemIss");
        put("CLIENTE", "cliente");
        put("USUARIO", "funcionario");
        put("FORNECEDOR", "fornecedor");
        put("TRANSPORTADORA", "transportadora");
        put("FAX", "fax");
        put("LIMCRED", "limiteCredito");
        put("DTULTCONTATO", "dataUltimoContato");
        put("AD_CLASSE_PARCEIRO", "classificacao");
        put("OBSERVACOES", "observacao");
        put("DTCAD", "dataCadastro");
        put("DTALTER", "dataAlteracao");
        put("AD_FRAUDULENTO", "clienteDesonesto");
        put("AD_FRAUDECART", "bloqueioCartao");
        put("CODTIPPARC", "idPerfilSankhya");
        put("HOMEPAGE", "site");
    }};

    private static  Map<String, String>  CAMPOS_BLOQUEADO_SNK = new HashMap<String, String>(){{
        put("CGC_CPF", "O campo CNPJ / CPF não pode ser alterado");
        //put("CODEND", "O campo ENDEREÇO não pode ser alterado");
        //put("NUMEND", "O campo NUMERO não pode ser alterado");
        //put("CODBAI", "O campo BAIRRO não pode ser alterado");
        //put("CODCID", "O campo CIDADE não pode ser alterado");
        //put("AD_CODUFS2", "O campo UF não pode ser alterado");
        //put("CEP", "O campo CEP não pode ser alterado");
    }};

    private static Map<String, String> CAMPOS_INTEGRACAO_CONTATO = new HashMap<String, String>() {{

//       tabela contato
        put("EMAIL", "email");
        put("TELEFONE", "telefone");
        put("FAX", "celular");
        put("AD_SMS", "aceitaSms");
        put("AD_EMAIL", "aceitaEmail");

    }};

    private static Map<String, String> CAMPOS_INTEGRACAO_ENDERECO = new HashMap<String, String>() {{
//        tabela enderero
        put("CODEND", "logradouro");
        put("NUMEND", "numero");
        put("COMPLEMENTO", "complemento");
        put("CODBAI", "bairro");
        put("CODCID", "municipio");
//        put("CODREG", "pgtof");
        put("CEP", "cep");
        put("AD_PONTOREFERENCIA", "pontoReferencia");
//        put("CODTIPPARC", "pgtof");
    }};

    private void integrarParceiro(PersistenceEvent persistenceEvent) throws Exception {

        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        DynamicVO ParceiroVo          = (DynamicVO) persistenceEvent.getVo();
        boolean temIntegracao         = false;
        if (ParceiroVo.getProperty("AD_CODPARCEXT") != null) {
            Parceiro parceiro = new Parceiro();
            ParceiroContato parceiroContato = null;
            ParceiroEndereco parceiroEndereco = null;
            String dataAlteracao = "";
            Calendar dtAlteracao = Calendar.getInstance();
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dataAlteracao = format.format(dtAlteracao.getTime());
            } catch (Exception e) {
                throw new Exception("Falha na Data Alteração");
            }
            parceiro.setCodigoParceiro(ParceiroVo.getProperty("AD_CODPARCEXT").toString());
            parceiro.setDataAlteracao(dataAlteracao);
            parceiro.setIdAlterador("8CCE8E13-9573-4128-9BF1-C740AD16347E");
            parceiro.setNomeUsuarioComputador(AuthenticationInfo.getCurrent().getClientName());
            parceiro.setIp(AuthenticationInfo.getCurrent().getClientIP());

            String ativo = "NÃO";
            String status = "PENDENTE";
            String ativoSnk = (ParceiroVo.getProperty("ATIVO") != null) ? ParceiroVo.getProperty("ATIVO").toString() : "";
            String statusSnk = (ParceiroVo.getProperty("AD_LIBERADO") != null) ? ParceiroVo.getProperty("AD_LIBERADO").toString() : "";

            if (modifingFields.isModifing("ATIVO")) {
                if("S".equals(modifingFields.getNewValue("ATIVO").toString().toUpperCase()) && "S".equals(statusSnk)){
                    status = "ATIVO";
                    ativo = "SIM";
                }
                else if("S".equals(modifingFields.getNewValue("ATIVO").toString().toUpperCase()) && !"S".equals(statusSnk)){
                    ativo = "SIM";
                }
                else if(!"S".equals(modifingFields.getNewValue("ATIVO").toString().toUpperCase()) && "S".equals(statusSnk)){
                    status = "ATIVO";
                }
                else if(!"S".equals(modifingFields.getNewValue("ATIVO").toString().toUpperCase()) && !"S".equals(statusSnk)){
                    status = "BLOQUEADO";
                }
            }
            else if (modifingFields.isModifing("AD_LIBERADO")) {
                if("S".equals(modifingFields.getNewValue("AD_LIBERADO").toString().toUpperCase()) && "S".equals(ativoSnk)){
                    status = "ATIVO";
                    ativo = "SIM";
                }
                else if(!"S".equals(modifingFields.getNewValue("AD_LIBERADO").toString().toUpperCase()) && "S".equals(ativoSnk)){
                    ativo = "SIM";
                }
                else if("S".equals(modifingFields.getNewValue("AD_LIBERADO").toString().toUpperCase()) && !"S".equals(statusSnk)){
                    status = "ATIVO";
                }
                else if(!"S".equals(modifingFields.getNewValue("AD_LIBERADO").toString().toUpperCase()) && !"S".equals(ativoSnk)){
                    status = "BLOQUEADO";
                }
            }

            parceiro.setStatus(status);
            parceiro.setAtivo(ativo);

            StringBuilder erros = null;

            for (Map.Entry<String, Object[]> campoSnk : modifingFields.entrySet()) {
                if (CAMPOS_BLOQUEADO_SNK.containsKey(campoSnk.getKey())) {
                    if (erros == null) {
                        erros = new StringBuilder();
                    }

                    erros.append("<font size='12'><b>" + CAMPOS_BLOQUEADO_SNK.get(campoSnk.getKey()) + "</b></font><br>");
                }

                String campo = CAMPOS_INTEGRACAO_PARCEIRO.get(campoSnk.getKey());
                if (campo != null) {
                    temIntegracao = true;

                    String valorIntegracao = null;
                    if (modifingFields.getNewValue(campoSnk.getKey()) != null) {
                        valorIntegracao = modifingFields.getNewValue(campoSnk.getKey()).toString();
                    }

                    Field field = parceiro.getClass().getDeclaredField(campo);
                    field.setAccessible(true);

                    if (valorIntegracao != null) {

                        if (campoSnk.getKey().equals("FAX")) {
                            if (valorIntegracao.substring(0, 2).equalsIgnoreCase("55")) {
                                valorIntegracao = valorIntegracao.substring(2);
                            }

                            if (valorIntegracao.length() > 10) {
                                valorIntegracao = null;
                            }
                        }

                        if (campo.contains("data")) {
                            Calendar data = Calendar.getInstance();
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                                data.setTime(format.parse(valorIntegracao));
                                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                valorIntegracao = format.format(data.getTime());
                            } catch (Exception e) {
                                throw new Exception("Falha de Campo Data");
                            }
                            field.set(parceiro, valorIntegracao);
                        } else {
                            if (valorIntegracao != null) {
                                valorIntegracao = "S".equals(valorIntegracao) ? "SIM" : valorIntegracao;
                                valorIntegracao = "N".equals(valorIntegracao) ? "NÃO" : valorIntegracao;
                            }

//                            if (campoSnk.getKey().equals("CODTIPPARC")) {
//
//                                JapeWrapper perfilDAO;
//                                try {
//                                    perfilDAO = JapeFactory.dao("Perfil");
//                                } catch (Exception e) {
//                                    throw new Exception("Instancia Perfil");
//                                }
//
//                                DynamicVO perfilVO;
//                                try {
//                                    perfilVO = perfilDAO.findByPK(modifingFields.getNewValue(campoSnk.getKey()).toString());
//                                } catch (Exception e) {
//                                    throw new Exception("Find Perfil");
//                                }
//
//                                valorIntegracao = perfilVO.getProperty("DESCRTIPPARC").toString();
//                            }

                            field.set(parceiro, valorIntegracao);
                        }
                    } else {
                        field.set(parceiro, "");
                    }
                }

                campo = CAMPOS_INTEGRACAO_CONTATO.get(campoSnk.getKey());
                if (campo != null) {
                    temIntegracao = true;
                    if (parceiroContato == null) {
                        parceiroContato = new ParceiroContato();
                    }

                    Field field = parceiroContato.getClass().getDeclaredField(campo);
                    field.setAccessible(true);

                    if (modifingFields.getNewValue(campoSnk.getKey()) != null) {
                        String valorIntegracao = modifingFields.getNewValue(campoSnk.getKey()).toString();

                        if (campoSnk.getKey().equals("FAX")) {
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

                        field.set(parceiroContato, valorIntegracao);
                    } else {
                        field.set(parceiroContato, "");
                    }

                }

                campo = CAMPOS_INTEGRACAO_ENDERECO.get(campoSnk.getKey());
                if (campo != null) {
                    temIntegracao = true;
                    if (parceiroEndereco == null) {
                        parceiroEndereco = new ParceiroEndereco();
                    }

                    Field field = parceiroEndereco.getClass().getDeclaredField(campo);
                    field.setAccessible(true);

                    String valorIntegracao = null;
                    if (modifingFields.getNewValue(campoSnk.getKey()) != null) {
                        valorIntegracao = modifingFields.getNewValue(campoSnk.getKey()).toString();
                    }

                    if ("CODEND".equals(campoSnk.getKey())) {
                        JapeWrapper enderecoDAO;
                        try {
                            enderecoDAO = JapeFactory.dao("Endereco");
                        } catch (Exception e) {
                            throw new Exception("Instancia Endereco");
                        }

                        DynamicVO enderecoVO;
                        try {
                            enderecoVO = enderecoDAO.findByPK(modifingFields.getNewValue(campoSnk.getKey()).toString());
                        } catch (Exception e) {
                            throw new Exception("Find Endereco");
                        }

                        valorIntegracao = enderecoVO.getProperty("NOMEEND").toString();
                        parceiroEndereco.setTipoLogradouro(enderecoVO.getProperty("TIPO").toString());

                    }

                    if ("CODBAI".equals(campoSnk.getKey().toString())) {
                        JapeWrapper bairroDAO;
                        try {
                            bairroDAO = JapeFactory.dao("Bairro");
                        } catch (Exception e) {
                            throw new Exception("Instancia Bairro");
                        }

                        DynamicVO bairroVO;
                        try {
                            bairroVO = bairroDAO.findByPK(modifingFields.getNewValue(campoSnk.getKey()).toString());
                        } catch (Exception e) {
                            throw new Exception("Find Endereco");
                        }

                        valorIntegracao = bairroVO.getProperty("NOMEBAI").toString();

                    }

                    if ("CODCID".equals(campoSnk.getKey().toString())) {
                        JapeWrapper cidadeDAO;
                        try {
                            cidadeDAO = JapeFactory.dao("Cidade");
                        } catch (Exception e) {
                            throw new Exception("Instancia Bairro");
                        }

                        DynamicVO cidadeVO;
                        try {
                            cidadeVO = cidadeDAO.findByPK(modifingFields.getNewValue(campoSnk.getKey()).toString());
                        } catch (Exception e) {
                            throw new Exception("Find Endereco");
                        }

                        valorIntegracao = cidadeVO.getProperty("NOMECID").toString();
                        parceiroEndereco.setUf(cidadeVO.getProperty("UF").toString());
                        parceiroEndereco.setCodigoIbge(cidadeVO.getProperty("CODMUNFIS").toString());
                    }

                    valorIntegracao = valorIntegracao == null ? "" : valorIntegracao;

                    field.set(parceiroEndereco, valorIntegracao);
                }

                if (parceiroContato != null) {
                    parceiro.setContatoPrincipal(parceiroContato);
                }

                if (parceiroEndereco != null) {
                    parceiro.setEnderecoPadrao(parceiroEndereco);
                }
            }

            if (erros != null) {
                throw new Exception(erros.toString());
            }

            if (temIntegracao) {
                Gson gson = new Gson();
                String json = gson.toJson(parceiro);
                String url = this.urlApi + "/v2/parceiros?esperar=true&usuario="+AuthenticationInfo.getCurrent().getName();
                this.enviarDados("PUT", url, "[" + json + "]");
            }

//            if (true) {
//                Gson gson                  = new Gson();
//                throw new Exception(gson.toJson(parceiro));
//
////                /v2/parceiros?esperar=true
//            }
        }
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
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if (AuthenticationInfo.getCurrent().getUserID().intValue() != 139) {
            this.integrarParceiro(persistenceEvent);
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
