package br.com.goup.snkcustomevents.utils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AcessoBanco implements AutoCloseable {
    private EntityFacade dwf;
    private JdbcWrapper jdbc;
    private NativeSql sql;
    private boolean aberto = false;


    public AcessoBanco() throws SQLException {
        this.openSession();
    }

    public void openSession() throws SQLException {
        if (!aberto) {
            dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);
            aberto = true;
        }
    }

    public void closeSession() {
        if (aberto){
            NativeSql.releaseResources(sql);
            jdbc.closeSession();
        }
        aberto = false;
    }

    @Override
    public void close() {
        this.closeSession();
    }

    public JdbcWrapper getJdbc() {
        return jdbc;
    }

    public NativeSql getNativeSql() {
        return sql;
    }

    public boolean isOpen() {
        return aberto;
    }

    public ResultSet find(String consulta, Object... params) throws Exception {
        sql.resetSqlBuf();
        sql.cleanParameters();
        sql.appendSql(consulta);
        for (Object param : params) {
            sql.addParameter(param);
        }
        return sql.executeQuery();
    }

    public ResultSet findCriteria(String consulta) throws Exception {
        sql.resetSqlBuf();
        sql.cleanParameters();
        sql.appendSql(consulta);

        return sql.executeQuery();
    }

    public ResultSet findOne(String consulta, Object... params) throws Exception {
        sql.resetSqlBuf();
        sql.cleanParameters();
        sql.appendSql(consulta);
        for (Object param : params) {
            sql.addParameter(param);
        }
        ResultSet rs = sql.executeQuery();
        if (rs.next())
            return rs;
        return null;
    }

    public void update(String contexto, Object... params) throws Exception {
        sql.resetSqlBuf();
        sql.cleanParameters();
        sql.appendSql(contexto);
        for (Object param : params) {
            sql.addParameter(param);
        }
        sql.executeUpdate();
    }

    public void insertGeneric(String tabela, HashMap<String,Object> chaveValores) throws Exception {
        StringBuilder insert = new StringBuilder();
        StringBuilder values = new StringBuilder();

        insert.append(" INSERT INTO ").append(tabela).append(" ( ");
        values.append(" ) ").append(" VALUES ").append(" ( ");

        int auxiliarVirgula = 0;
        for(Map.Entry<String, ?> map : chaveValores.entrySet()){
            insert.append(map.getKey());
            values.append("'").append(map.getValue()).append("'");
            auxiliarVirgula++;
            if(auxiliarVirgula < chaveValores.size() ){
                insert.append(", ");
                values.append(", ");
            }
        }

        values.append(" ) ");

        sql.resetSqlBuf();
        sql.cleanParameters();
        sql.appendSql(insert.append(values).toString());
        sql.executeUpdate();
    }

    public void delete(String contexto, Object... params) throws Exception {
        sql.resetSqlBuf();
        sql.cleanParameters();
        sql.appendSql(contexto);
        for (Object param : params) {
            sql.addParameter(param);
        }
        sql.executeUpdate();
    }

    public EntityFacade getDwf() {
        return dwf;
    }
}
