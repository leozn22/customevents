package br.com.goup.snkcustomevents.notas;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.nfse.NFSeHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

public class BuscaAutorizacaoNFSe implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        ServiceContext sctx = new ServiceContext(null);
        sctx.setAutentication(AuthenticationInfo.getCurrent());
        sctx.makeCurrent();

        try {
            SPBeanUtils.setupContext(sctx);
        } catch (Exception e) {
            e.printStackTrace();
            scheduledActionContext.info("Error: Não foi Possivel Executar a Chamada SPBeanUtils.setupContext \n" + e.getMessage());
        }

        try {
            buscaAutorizacaoNFSe();
        } catch (Exception e) {
            e.printStackTrace();
            scheduledActionContext.info("Error: " + e.getMessage());
        }
    }

    public void buscaAutorizacaoNFSe() {
        JdbcWrapper jdbc = null;
        try {
            try {
                EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
                jdbc = dwfEntityFacade.getJdbcWrapper();
                jdbc.openSession();
                NativeSql sqlNotas = new NativeSql(jdbc);

                sqlNotas.appendSql("SELECT CAB.NUNOTA\n" +
                        "  FROM TGFCAB CAB\n" +
                        "    JOIN (SELECT TPO.CODTIPOPER,\n" +
                        "                 MAX(DHALTER) MX\n" +
                        "        FROM TGFTOP TPO\n" +
                        "        GROUP BY TPO.CODTIPOPER) MAX_TOP\n" +
                        "      ON CAB.CODTIPOPER = MAX_TOP.CODTIPOPER\n" +
                        "    JOIN TGFTOP TOPER\n" +
                        "      ON MAX_TOP.CODTIPOPER = TOPER.CODTIPOPER\n" +
                        "      AND MAX_TOP.MX = TOPER.DHALTER\n" +
                        "  WHERE CAB.STATUSNFSE = 'E'\n" +
                        "    AND TOPER.AD_BUSCA_AUTORIZ = 'S'\n" +
                        "  ORDER BY CAB.DTNEG DESC\n" +
                        "FETCH NEXT 25 ROWS ONLY");

                ResultSet rsNotas = sqlNotas.executeQuery();

                Collection<BigDecimal> lisNotasSelecionadas = new ArrayList<BigDecimal>();
                while (rsNotas.next()) {

                    BigDecimal nunota = rsNotas.getBigDecimal("NUNOTA");
                    lisNotasSelecionadas.add(nunota);
                    System.out.println("Acao Agendada - Busca autorização Nro. Unico: " + nunota);
                }

                rsNotas.close();

                NFSeHelpper nfseHelpper = new NFSeHelpper();
                nfseHelpper.buscaAutorizacaoNotas(lisNotasSelecionadas);

            } finally {
                JdbcWrapper.closeSession(jdbc);
            }
        } catch (Exception e) {
            RuntimeException re = new RuntimeException(e);
            System.out.println("Erro Exception: " + re);
            throw re;
        }
    }
}
