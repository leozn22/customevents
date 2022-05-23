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

    public void atualizarSetorProducaoApontamento(BigDecimal nuapo, BigDecimal idiatv) throws Exception {
        NativeSql sql = new NativeSql(this.jdbc);
        sql.setNamedParameter("NUAPO", nuapo);
        sql.setNamedParameter("IDIATV", idiatv);

        ResultSet result = null;
        try {
            result = sql.executeQuery("SELECT APONTAMENTO.NUAPO, APONTAMENTO.TZANUITEM, APONTAMENTO.IDIATV,\n" +
                    "APONTAMENTO.IDIPROC\n" +
                    "FROM TZAAPONTAMENTO APONTAMENTO\n" +
                    "INNER JOIN AD_TGFFINSAL SALDO ON APONTAMENTO.TZANUITEM = SALDO.ITEM \n" +
                    "WHERE APONTAMENTO.NUAPO = :NUAPO\n" +
                    "AND APONTAMENTO.IDIATV = :IDIATV");

            SetorAtividade setorItem;
            boolean apontamentoLocalizado = false;

            while (result.next()) {
                if (!apontamentoLocalizado) {
                    apontamentoLocalizado = true;
                }

                setorItem = this.buscarProximoSetorAtividadeItem(result.getBigDecimal("TZANUITEM"));

                if (setorItem != null) {
                    this.atualizarClienteSaldo(result.getBigDecimal("TZANUITEM"), setorItem.getIdiatv(), setorItem.getDescricaoAtividade());
                }
            }

            if (!apontamentoLocalizado) {
                result = sql.executeQuery("SELECT count(AD_TGFFINSAL.ITEM) QTD FROM TPRIATV\n" +
                        "INNER JOIN TPRAPO ON TPRIATV.IDIATV = TPRAPO.IDIATV \n" +
                        "INNER JOIN AD_TGFFINSAL ON AD_TGFFINSAL.IDIPROC = TPRIATV.IDIPROC\n" +
                        "WHERE TPRAPO.NUAPO = :NUAPO");

                if (result.next() && result.getInt("QTD") > 0) {
                    throw new Exception("Esta OP foi originada de " + result.getInt("QTD") + " item(ns) é não foi realizado apontamento manual. Tela: Apontamento Item (HTML5)");
                }
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    public void atualizarStatusOrdemProducao(BigDecimal idiproc, String statusProc) {

        String setorOP;

        switch (statusProc){
            case "F":
                setorOP = "OP FINALIZADA";
                break;
            case "C":
                setorOP = "OP CANCELADA";
                break;
            default:
                setorOP = "";
        }

        if ("".equals(statusProc)) {
            return;
        }

        NativeSql sql = new NativeSql(this.jdbc);
        try {
            sql.setNamedParameter("IDIPROC", idiproc);
            ResultSet result  = sql.executeQuery("SELECT SALDO.ITEM FROM AD_TGFFINSAL SALDO WHERE SALDO.IDIPROC = :IDIPROC");

            while (result.next()) {
                this.atualizarClienteSaldo(result.getBigDecimal("ITEM"), null, setorOP);
            }

            if (statusProc.equals("C")) {
                JapeFactory.dao("AD_GRADEOP").deleteByCriteria("IDIPROC = ? ", idiproc);
            }

            result.close();
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
                sql.setNamedParameter("IDEFX", instanciaAtividadeVO.asInt("IDEFX"));

                ResultSet result;
                boolean verificarOpIntermediaria = true;

                if (this.existeApontamentoOP(instanciaAtividadeVO.asBigDecimal("IDIPROC"))) {
                    result = sql.executeQuery("SELECT DISTINCT SALDO.ITEM, SALDO.IDIPROC FROM AD_TGFFINSAL SALDO\n" +
                            "INNER JOIN TZAAPONTAMENTO ON SALDO.ITEM = TZAAPONTAMENTO.TZANUITEM AND SALDO.IDIPROC = TZAAPONTAMENTO.IDIPROC\n" +
                            "INNER JOIN TPRIATV ATIVIDADE ON ATIVIDADE.IDIATV = TZAAPONTAMENTO.IDIATV\n" +
                            "WHERE SALDO.IDIPROC = :IDIPROC\n" +
                            "AND ATIVIDADE.IDEFX IN (\n" +
                            "SELECT IDEFXORIG FROM TPRTFX \n" +
                            "INNER JOIN TPREFX ON TPRTFX.IDEFXORIG = TPREFX.IDEFX \n" +
                            "WHERE TPRTFX.IDEFXDEST = :IDEFX\n" +
                            "AND TPREFX.TIPO = 1101\n" +
                            ")");
                    verificarOpIntermediaria = false;
                } else {
                    result = sql.executeQuery("SELECT ITEM, IDIPROC FROM AD_TGFFINSAL at2 \n" +
                            "WHERE IDIPROC = :IDIPROC");

                }

                while (result.next()) {
                    if (verificarOpIntermediaria) {
                        verificarOpIntermediaria = false;
                    }

                    SetorAtividade setorItem = this.buscarProximoSetorAtividadeItem(result.getBigDecimal("ITEM"));

                    if (setorItem != null) {
                        this.atualizarClienteSaldo(setorItem.getCodigoItem(), setorItem.getIdiatv(), setorItem.getDescricaoAtividade());
                    }
                }

                if (verificarOpIntermediaria) {
                    result = sql.executeQuery("SELECT SALDO.ITEM FROM TPRIPROC OP\n" +
                            "INNER JOIN AD_TGFFINSAL SALDO ON OP.AD_NULOP = SALDO.NULOP \n" +
                            "WHERE OP.IDIPROC = :IDIPROC");

                    while (result.next()) {
                        this.atualizarClienteSaldo(result.getBigDecimal("ITEM"), null, setorAtividade);
                    }
                }

                result.close();
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

    private SetorAtividade buscarProximoSetorAtividadeItem(BigDecimal codigoItem) {

        SetorAtividade setorAtividade = null;

        NativeSql consulta = new NativeSql(this.jdbc);
        consulta.appendSql("SELECT PROXIMA_ATIVIDADE.IDIATV, TPREFX.DESCRICAO ");
        consulta.appendSql("FROM ( ");
        consulta.appendSql("SELECT MIN(TPRIATV.IDIATV) IDIATV FROM TPRIATV ");
        consulta.appendSql("INNER JOIN AD_TGFFINSAL SALDO ON TPRIATV.IDIPROC = SALDO.IDIPROC ");
        consulta.appendSql("WHERE SALDO.ITEM = :TZANUITEM ");

        if (this.itemJaApontado(codigoItem)) {
            consulta.appendSql("AND TPRIATV.IDEFX in ( ");
            consulta.appendSql("\tSELECT TRANSICAOFLUXO.IDEFXDEST FROM ");
            consulta.appendSql("\t(SELECT APT.IDIATV, APT.IDIPROC FROM TZAAPONTAMENTO APT ");
            consulta.appendSql("\tINNER JOIN AD_TGFFINSAL ADS ON ADS.ITEM = APT.TZANUITEM AND APT.IDIPROC = ADS.IDIPROC ");
            consulta.appendSql("\tWHERE APT.TZANUITEM = :TZANUITEM ");
            consulta.appendSql("\tORDER BY TZADTGERACAO DESC fetch first 1 row ONLY) ULTIMO_APT ");
            consulta.appendSql("\tINNER JOIN TPRIATV ATIVIDADE ON ULTIMO_APT.IDIATV = ATIVIDADE.IDIATV ");
            consulta.appendSql("\tINNER JOIN TPRTFX TRANSICAOFLUXO ON ATIVIDADE.IDEFX = TRANSICAOFLUXO.IDEFXORIG ");
            consulta.appendSql(") ");
        }

        consulta.appendSql("AND TPRIATV.DHACEITE IS NOT NULL ");
        consulta.appendSql("AND TPRIATV.DHFINAL IS NULL ");
        consulta.appendSql(") PROXIMA_ATIVIDADE ");
        consulta.appendSql("INNER JOIN TPRIATV ON  PROXIMA_ATIVIDADE.IDIATV = TPRIATV.IDIATV ");
        consulta.appendSql("INNER JOIN TPREFX ON TPRIATV.IDEFX = TPREFX.IDEFX ");

        try {
            consulta.setNamedParameter("TZANUITEM", codigoItem);
            ResultSet result = consulta.executeQuery();

            if (result.next()) {
                setorAtividade = new SetorAtividade(result.getBigDecimal("IDIATV"),
                        codigoItem, result.getString("DESCRICAO"));
            }

            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return setorAtividade;
    }

    private boolean itemJaApontado(BigDecimal codigoItem) {
        boolean retorno = false;

        NativeSql consulta = new NativeSql(this.jdbc);
        consulta.appendSql("SELECT count(APT.NUAPO) QTD FROM TZAAPONTAMENTO APT\n" +
                "INNER JOIN AD_TGFFINSAL SALDO ON SALDO.ITEM = APT.TZANUITEM AND APT.IDIPROC = SALDO.IDIPROC \n" +
                "WHERE APT.TZANUITEM = :TZANUITEM");

        try {
            consulta.setNamedParameter("TZANUITEM", codigoItem);
            ResultSet result = consulta.executeQuery();

            retorno = result.next() && result.getInt("QTD") > 0;

            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retorno;
    }

    private boolean existeApontamentoOP(BigDecimal idiproc) {
        boolean retorno = false;

        if (idiproc.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        NativeSql consulta = new NativeSql(this.jdbc);
        consulta.appendSql("SELECT count(TZAAPONTAMENTO.NUAPO) QTD FROM AD_TGFFINSAL SALDO\n" +
                "INNER JOIN TZAAPONTAMENTO ON SALDO.ITEM = TZAAPONTAMENTO.TZANUITEM AND SALDO.IDIPROC = TZAAPONTAMENTO.IDIPROC\n" +
                "WHERE SALDO.IDIPROC = :IDIPROC");

        try {
            consulta.setNamedParameter("IDIPROC", idiproc);
            ResultSet result = consulta.executeQuery();

            retorno = result.next() && (result.getInt("QTD") > 0);
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retorno;
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

            if (result.next() && (result.getString("setor") != null)) {
                setorAtividade = result.getString("setor");
            }

            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return setorAtividade;
    }
}
