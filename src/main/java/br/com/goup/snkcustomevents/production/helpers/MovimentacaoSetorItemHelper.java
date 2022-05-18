package br.com.goup.snkcustomevents.production.helpers;

import br.com.goup.snkcustomevents.domain.SetorAtividade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;

public class MovimentacaoSetorItemHelper {

    JdbcWrapper jdbc;

    public MovimentacaoSetorItemHelper(JdbcWrapper jdbc) {
        this.jdbc = jdbc;
    }

    public void atualizarSetorProducaoApontamento(BigDecimal nuapo, BigDecimal idiatv) {
        NativeSql sql = new NativeSql(this.jdbc);
        try {
            sql.setNamedParameter("NUAPO", nuapo);
            sql.setNamedParameter("IDIATV", idiatv);

            ResultSet result = sql.executeQuery("SELECT APONTAMENTO.NUAPO, APONTAMENTO.TZANUITEM, APONTAMENTO.IDIATV,\n" +
                    "APONTAMENTO.IDIPROC\n" +
                    "FROM TZAAPONTAMENTO APONTAMENTO\n" +
                    "INNER JOIN AD_TGFFINSAL SALDO ON APONTAMENTO.TZANUITEM = SALDO.ITEM \n" +
                    "WHERE APONTAMENTO.NUAPO = :NUAPO\n" +
                    "AND APONTAMENTO.IDIATV = :IDIATV");

            SetorAtividade setorItem = null;

            while (result.next()) {
                //Como todos os itens estão no mesmo apontamento não preciso buscar o setor novamente
                if (setorItem == null) {
                    setorItem = this.buscarSetorAtividadeItem(result.getBigDecimal("TZANUITEM"), result.getBigDecimal("IDIPROC"));
                }

                if (setorItem != null) {
                    this.atualizarClienteSaldo(result.getBigDecimal("TZANUITEM"), setorItem.getIdiatv(), setorItem.getDescricaoAtividade());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizarSetorProducaoAtividade(DynamicVO instanciaAtividadeVO) {
        if (instanciaAtividadeVO.asBigDecimal("IDIPROC").compareTo(BigDecimal.ZERO) > 0) {
            String setorAtividade = this.buscarDescricaoSetorAtividade(instanciaAtividadeVO.asInt("IDEFX"));

            if ("".equals(setorAtividade)){
                setorAtividade = "Setor nao identificado (OP.:" + instanciaAtividadeVO.asBigDecimal("IDIPROC") + ")";
            }

            NativeSql sql = new NativeSql(this.jdbc);

            try {
                sql.setNamedParameter("IDIPROC", instanciaAtividadeVO.asInt("IDIPROC"));
                ResultSet result = sql.executeQuery("SELECT ITEM, IDIPROC FROM AD_TGFFINSAL at2 \n" +
                        "WHERE IDIPROC = :IDIPROC");

                boolean opIntermediaria = true;

                while (result.next()) {
                    if (opIntermediaria) {
                        opIntermediaria = false;
                    }

                    SetorAtividade setorItem = this.buscarSetorAtividadeItem(result.getBigDecimal("ITEM"), result.getBigDecimal("IDIPROC"));

                    if (setorItem != null) {
                        this.atualizarClienteSaldo(setorItem.getCodigoItem(), setorItem.getIdiatv(), setorItem.getDescricaoAtividade());
                    }
                }

                if (opIntermediaria) {
                    result = sql.executeQuery("SELECT ITEM FROM TPRIPROC OP\n" +
                            "INNER JOIN AD_TGFFINSAL SALDO ON OP.AD_NULOP = SALDO.NULOP \n" +
                            "WHERE OP.IDIPROC = :IDIPROC");

                    while (result.next()) {
                        this.atualizarClienteSaldo(result.getBigDecimal("ITEM"), null, setorAtividade);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void atualizarClienteSaldo(BigDecimal codigoItem, BigDecimal codigoAtividade, String setorAtividade) {
        JapeWrapper clienteSaldoDAO;
        try {
            clienteSaldoDAO = JapeFactory.dao("AD_TGFFINSAL");

            Collection<DynamicVO> listPedidoVO = clienteSaldoDAO.find("ITEM = ?", codigoItem);

            for (DynamicVO pedidoPacote: listPedidoVO) {
                clienteSaldoDAO.prepareToUpdate(pedidoPacote)
                        .set("IDIATV", codigoAtividade)
                        .set("STATUSPRODUCAO", setorAtividade)
                        .update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SetorAtividade buscarSetorAtividadeItem(BigDecimal codigoItem, BigDecimal idiproc) {
        SetorAtividade setorAtividade = null;

        NativeSql consulta = new NativeSql(this.jdbc);
        consulta.appendSql("SELECT atv_atual.IDIATV, TPREFX.DESCRICAO\n" +
                "FROM\n" +
                "(SELECT MIN(TPRIATV.IDIATV) IDIATV FROM TPRIATV\n" +
                "INNER JOIN AD_TGFFINSAL SALDO ON TPRIATV.IDIPROC = SALDO.IDIPROC\n" +
                "WHERE SALDO.ITEM = :TZANUITEM\n" +
                "AND TPRIATV.IDIATV NOT IN (\n" +
                "SELECT COALESCE(MAX(IDIATV), 0) FROM TZAAPONTAMENTO WHERE TZANUITEM = :TZANUITEM\n" +
                ")) atv_atual\n" +
                "INNER JOIN TPRIATV ON atv_atual.IDIATV = TPRIATV.IDIATV\n" +
                "INNER JOIN TPREFX ON TPRIATV.IDEFX = TPREFX.IDEFX");

        try {
            consulta.setNamedParameter("TZANUITEM", idiproc);
            ResultSet result = consulta.executeQuery();

            if (result.next()) {
                setorAtividade = new SetorAtividade(result.getBigDecimal("IDIATV"),
                        codigoItem, result.getString("DESCRICAO"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return setorAtividade;
    }

    private String buscarDescricaoSetorAtividade(int idefx) {
        String setorAtividade = "";

        if (idefx == 0) {
            return setorAtividade;
        }

        NativeSql consulta = new NativeSql(this.jdbc);
        consulta.appendSql("SELECT SUBSTR(fluxo.DESCRICAO,1,100) setor \n" +
                "FROM TPREFX fluxo\n" +
                "WHERE fluxo.IDEFX = :IDEFX");

        try {
            consulta.setNamedParameter("IDEFX", idefx);
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
}
