package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.domain.ViewProducaoSaldoItem;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

public class AtividadeProducaoEvento implements EventoProgramavelJava {

    /*
        Tabela: TPRIATV - (Dicionario de Dados)
        Instância: InstanciaAtividade

        (EVENTO)
        Descrição: Atualização do setor dos itens Produzidos
    */

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
        this.atualizarSetorProducao(persistenceEvent);
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    private void atualizarSetorProducao(PersistenceEvent persistenceEvent) {
        DynamicVO atividadeVO = (DynamicVO) persistenceEvent.getVo();

        if (atividadeVO.asBigDecimal("IDIPROC").compareTo(BigDecimal.ZERO) > 0) {
            String setorAtividade = this.buscarSetorAtividade(persistenceEvent);

            if ("".equals(setorAtividade)){
                setorAtividade = "Setor nao identificado (OP.:" + atividadeVO.asBigDecimal("IDIPROC") + ")";
            }

            JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbc);

            try {
                sql.setNamedParameter("IDIPROC", atividadeVO.asInt("IDIPROC"));
                ResultSet result = sql.executeQuery("SELECT ITEM FROM AD_TGFFINSAL at2 \n" +
                        "WHERE IDIPROC = :IDIPROC");

                boolean opIntermediaria = true;

                while (result.next()) {
                    opIntermediaria = false;
                    this.atualizarClienteSaldo(result.getBigDecimal("ITEM"), setorAtividade);
                }

                if (opIntermediaria) {
                    result = sql.executeQuery("SELECT ITEM FROM TPRIPROC OP\n" +
                            "INNER JOIN AD_TGFFINSAL SALDO ON OP.AD_NULOP = SALDO.NULOP \n" +
                            "WHERE OP.IDIPROC = :IDIPROC");

                    while (result.next()) {
                        this.atualizarClienteSaldo(result.getBigDecimal("ITEM"), setorAtividade);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String buscarSetorAtividade(PersistenceEvent persistenceEvent) {
        String setorAtividade = "";
        DynamicVO atividadeVO = (DynamicVO) persistenceEvent.getVo();

        if (atividadeVO.asInt("IDEFX") == 0) {
            return setorAtividade;
        }

        JdbcWrapper jdbc = persistenceEvent.getJdbcWrapper();
        NativeSql consulta = new NativeSql(jdbc);
        consulta.appendSql("SELECT SUBSTR(fluxo.DESCRICAO,1,100) setor \n" +
                           "FROM TPREFX fluxo\n" +
                           "WHERE fluxo.IDEFX = :IDEFX");

        try {
            consulta.setNamedParameter("IDEFX", atividadeVO.asInt("IDEFX"));
            ResultSet result = consulta.executeQuery();

            if (result.next()) {
                if (result.getString("setor") != null) {
                    setorAtividade = result.getString("setor");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return setorAtividade;
    }

    private void atualizarClienteSaldo(BigDecimal codigoItem, String setorAtividade) {
        JapeWrapper clienteSaldoDAO;
        try {
            clienteSaldoDAO = JapeFactory.dao("AD_TGFFINSAL");

            Collection<DynamicVO> listPedidoVO = clienteSaldoDAO.find("ITEM = ?", codigoItem);

            for (DynamicVO pedidoPacote: listPedidoVO) {
                clienteSaldoDAO.prepareToUpdate(pedidoPacote)
                        .set("STATUSPRODUCAO", setorAtividade)
                        .update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
