package br.com.goup.snkcustomevents.ofx;

public class Constantes {
    public static final int CODIGO_DECISAO_BAIXA_PARCIAL = 4;

    public static final String SQL_GET_PROMESSAS = "" +
            "SELECT\n" +
            "    PRM.NUNOTA,\n" +
            "    CAB.NUMNOTA,\n" +
            "    PRM.VALORDEPOSITO,\n" +
            "    PRM.DATADEPOSITO,\n" +
            "    PRM.NRODESPOSITO,\n" +
            "    PRM.CODCTABCOINT,\n" +
            "    EXB.NUIMPORT,\n" +
            "    EXB.NUEXB\n" +
            "FROM TGFIEB IEB\n" +
            "    INNER JOIN TGFEXB EXB ON EXB.NUIMPORT = IEB.NUIMPORT\n" +
            "    INNER JOIN TZAPRM PRM ON TRUNC(PRM.DATADEPOSITO) = EXB.DTLANC AND \n" +
            "       CASE WHEN EXB.CODBCO = 341 THEN TO_CHAR(BH_GET_SOMENTE_NUMEROS(SUBSTR(nvl(EXB.AD_HIST2, EXB.HIST),10,5) || '.')) ELSE TO_CHAR(EXB.NRODOC) END = TO_CHAR(PRM.NRODESPOSITO) AND \n" +
            "       PRM.VALORDEPOSITO = EXB.VALOR \n" +
            "    INNER JOIN TGFCAB CAB ON CAB.NUNOTA = PRM.NUNOTA \n" +
            "WHERE\n" +
            "    EXB.RECDESP = 1\n" +
            "AND IEB.NUIMPORT = ?\n" +
            // não existe mais de uma correspondencia para o deposito
            "AND NOT EXISTS(\n" +
            "    SELECT\n" +
            "        1\n" +
            "    FROM TZAPRM\n" +
            "    WHERE \n" +
            "        TRUNC(DATADEPOSITO) = EXB.DTLANC\n" +
            "    AND CASE\n" +
            "            WHEN EXB.CODBCO = 341 THEN TO_CHAR(\n" +
            "                BH_GET_SOMENTE_NUMEROS(SUBSTR(nvl(EXB.AD_HIST2, EXB.HIST), 10, 5) || '.')\n" +
            "            )\n" +
            "            ELSE TO_CHAR(EXB.NRODOC)\n" +
            "            END = TO_CHAR(NRODESPOSITO)\n" +
            "            AND VALORDEPOSITO = EXB.VALOR \n" +
            "    HAVING\n" +
            "        count(DISTINCT NUMNOTA) > 1\n" +
            ")";

    public static final String SQL_GET_PROMESSAS_PIX = "" +
            "SELECT * FROM (SELECT\n" +
            "    PRM.NUNOTA,\n" +
            "    CAB.NUMNOTA,\n" +
            "    PRM.VALORDEPOSITO,\n" +
            "    PRM.DATADEPOSITO,\n" +
            "    PRM.NRODESPOSITO,\n" +
            "    PRM.CODCTABCOINT,\n" +
            "    EXB.NUIMPORT,\n" +
            "    EXB.NUEXB\n" +
            "FROM TGFIEB IEB\n" +
            "    INNER JOIN TGFEXB EXB ON EXB.NUIMPORT = IEB.NUIMPORT\n" +
            "    INNER JOIN TZAPRM PRM ON TRUNC(PRM.DATADEPOSITO) = EXB.DTLANC AND \n" +
            "       CASE WHEN EXB.CODBCO = 341 THEN TO_CHAR(BH_GET_SOMENTE_NUMEROS(SUBSTR(nvl(EXB.AD_HIST2, EXB.HIST),10,5) || '.')) ELSE TO_CHAR(EXB.NRODOC) END = TO_CHAR(PRM.NRODESPOSITO) AND \n" +
            "       PRM.VALORDEPOSITO = EXB.VALOR \n" +
            "    INNER JOIN TGFCAB CAB ON CAB.NUNOTA = PRM.NUNOTA \n" +
            "WHERE\n" +
            "    EXB.RECDESP = 1\n" +
            "AND IEB.NUIMPORT = %s\n" +
            // não existe mais de uma correspondencia para o deposito
            "AND NOT EXISTS(\n" +
            "    SELECT\n" +
            "        1\n" +
            "    FROM TZAPRM\n" +
            "    WHERE \n" +
            "        TRUNC(DATADEPOSITO) = EXB.DTLANC\n" +
            "    AND CASE\n" +
            "            WHEN EXB.CODBCO = 341 THEN TO_CHAR(\n" +
            "                BH_GET_SOMENTE_NUMEROS(SUBSTR(nvl(EXB.AD_HIST2, EXB.HIST), 10, 5) || '.')\n" +
            "            )\n" +
            "            ELSE TO_CHAR(EXB.NRODOC)\n" +
            "            END = TO_CHAR(NRODESPOSITO)\n" +
            "            AND VALORDEPOSITO = EXB.VALOR \n" +
            "    HAVING\n" +
            "        count(DISTINCT NUMNOTA) > 1\n" +
            ") " +
            "UNION             \n" +
            "            \n" +
            "            SELECT\n" +
            "                PRM.NUNOTA,\n" +
            "                CAB.NUMNOTA,\n" +
            "                PRM.BH_VLRDEPOSITO,\n" +
            "                PRM.BH_DATADEPOSITO,\n" +
            "                PRM.BH_NRODEPOSITO,\n" +
            "                PRM.CODCTABCOINT,\n" +
            "                EXB.NUIMPORT,\n" +
            "                EXB.NUEXB\n" +
            "            FROM TGFIEB IEB\n" +
            "                INNER JOIN TGFEXB EXB ON EXB.NUIMPORT = IEB.NUIMPORT\n" +
            "                INNER JOIN TGFFIN PRM ON TO_CHAR( EXB.FITID ) = TO_CHAR( PRM.BH_NRODEPOSITO )\n" +
            "                INNER JOIN TGFCAB CAB ON CAB.NUNOTA = PRM.NUNOTA \n" +
            "            WHERE\n" +
            "                EXB.RECDESP = 1\n" +
            "            AND IEB.NUIMPORT = %s) ITS";

    public static final String SQL_GET_FINANCEIROS_BAIXA = "" +
            "SELECT\n" +
            "    CASE WHEN CRE.NUNOTA IS NULL THEN 'Normal' ELSE 'Crédito' END as TIPO,\n" +
            "    FIN.NUFIN,\n" +
            "    FIN.VLRDESDOB\n" +
            "FROM TGFFIN FIN\n" +
            "     INNER JOIN TGFPAR PAR ON PAR.CODPARC = FIN.CODPARC\n" +
            "     LEFT  JOIN TGFFIN CRE ON CRE.RECDESP = 1 AND CRE.CODTIPTIT = 26 AND CRE.NUMNOTA =  FIN.NUMNOTA\n" +
            "WHERE\n" +
            "    FIN.RECDESP = 1\n" +
            "AND FIN.DHBAIXA IS NULL\n" +
            "AND FIN.NUMNOTA = ?";

    public static final String SQL_GET_FINANCEIROS_CONCILIAR = "" +
            "SELECT\n" +
            "    FIN.NUFIN,\n" +
            "    FIN.NUBCO,\n" +
            "    FIN.DHBAIXA,\n" +
            "    FIN.VLRDESDOB\n" +
            "FROM TGFFIN FIN\n" +
            "     LEFT  JOIN TGFFIN CRE ON CRE.RECDESP = 1 AND CRE.CODTIPTIT = 26 AND CRE.NUMNOTA =  FIN.NUMNOTA\n" +
            "WHERE\n" +
            "    TRUNC(FIN.BH_DATADEPOSITO) = TRUNC(?) \n" +
            "AND FIN.BH_NRODEPOSITO = ? \n" +
            "AND FIN.RECDESP = 1\n" +
            "AND FIN.NUBCO IS NOT NULL\n" +
            "AND FIN.DHBAIXA IS NOT NULL\n" +
            "AND FIN.NUMNOTA = ?";

//    public static final String SQL_GET_FINANCEIROS_CONCILIAR = "" +
//            "SELECT\n" +
//            "    FIN.NUFIN,\n" +
//            "    FIN.NUBCO,\n" +
//            "    FIN.DHBAIXA,\n" +
//            "    FIN.VLRDESDOB\n" +
//            "FROM TGFFIN FIN\n" +
//            "     LEFT  JOIN TGFFIN CRE ON CRE.RECDESP = 1 AND CRE.CODTIPTIT = 26 AND CRE.NUMNOTA =  FIN.NUMNOTA\n" +
//            "WHERE\n" +
//            "    TRUNC(FIN.BH_DATADEPOSITO) = TRUNC(TO_DATE(%s,'yyyy-mm-dd')) \n" +
//            "AND FIN.BH_NRODEPOSITO = '%s' \n" +
//            "AND FIN.RECDESP = 1\n" +
//            "AND FIN.NUBCO IS NOT NULL\n" +
//            "AND FIN.DHBAIXA IS NOT NULL\n" +
//            "AND FIN.NUMNOTA = %s";

    public static final String GET_VALOR_JA_BAIXADO_DA_PROMESSA = "" +
            "SELECT\n" +
            "  nullValue(sum(FIN.VLRDESDOB),0) as TOTALBAIXADO\n" +
            "FROM TGFFIN FIN\n" +
            "INNER JOIN TGFPAR PAR ON PAR.CODPARC = FIN.CODPARC\n" +
            "LEFT JOIN TGFFIN CRE ON CRE.RECDESP = 1 AND CRE.CODTIPTIT = 26 AND CRE.NUMNOTA = FIN.NUMNOTA\n" +
            "WHERE\n" +
            "    FIN.RECDESP = 1\n" +
            "AND FIN.DHBAIXA IS NOT NULL\n" +
            "AND FIN.NUMNOTA = ?";

    public static final String XML_SALVAR_ADIANTAMENTO = "" +
            "<parcelas impressao=\"ADIANTEMP\">\n" +
            "    <parcela>\n" +
            "        <CODEMP>:EMPRESA_DESP</CODEMP>\n" +
            "        <CODPARC>:PARCEIRO_DESP</CODPARC>\n" +
            "        <CODTIPOPER>4104</CODTIPOPER>\n" +
            "        <CODTIPTIT>:TITULO_DESP</CODTIPTIT>\n" +
            "        <CODNAT>:NATUREZA_DESP</CODNAT>\n" +
            "        <VLRDESDOB>:VALOR_DESP</VLRDESDOB>\n" +
            "        <CODCENCUS>:CENTRORESULTADO_DESP</CODCENCUS>\n" +
            "        <CODCTABCOINT>:CONTA_DESP</CODCTABCOINT>\n" +
            "        <CODVEICULO>0</CODVEICULO>\n" +
            "        <CODPROJ>0</CODPROJ>\n" +
            "        <RECDESP>-1</RECDESP>\n" +
            "        <VLRMULTA>0</VLRMULTA>\n" +
            "        <ORDEMCARGA>0</ORDEMCARGA>\n" +
            "        <CODBCO>1</CODBCO>\n" +
            "        <DESDOBRAMENTO>0</DESDOBRAMENTO>\n" +
            "        <HISTORICO>:HISTORICO_DESP</HISTORICO>\n" +
            "        <PROVISAO>N</PROVISAO>\n" +
            "        <ORIGEM>F</ORIGEM>\n" +
            "        <DESDOBDUPL>ZZ</DESDOBDUPL>\n" +
            "        <TIPMARCCHEQ>I</TIPMARCCHEQ>\n" +
            "        <TIPMULTA>1</TIPMULTA>\n" +
            "        <TIPJURO>1</TIPJURO>\n" +
            "        <DTNEG>:DTNEG_DESP</DTNEG>\n" +
            "        <DTVENC>:DTVENC_DESP</DTVENC>\n" +
            "        <DTVENCINIC>:DTVENC_DESP</DTVENCINIC>\n" +
            "    </parcela>\n" +
            "    <parcela>\n" +
            "        <CODEMP>1</CODEMP>\n" +
            "        <CODPARC>638</CODPARC>\n" +
            "        <CODTIPOPER>4104</CODTIPOPER>\n" +
            "        <CODTIPTIT>15</CODTIPTIT>\n" +
            "        <CODNAT>1010200</CODNAT>\n" +
            "        <VLRDESDOB>100</VLRDESDOB>\n" +
            "        <CODCENCUS>2020301</CODCENCUS>\n" +
            "        <CODCTABCOINT>9</CODCTABCOINT>\n" +
            "        <CODVEICULO>0</CODVEICULO>\n" +
            "        <CODPROJ>0</CODPROJ>\n" +
            "        <RECDESP>1</RECDESP>\n" +
            "        <VLRJUROEMBUT>0</VLRJUROEMBUT>\n" +
            "        <VLRJURONEGOC>0</VLRJURONEGOC>\n" +
            "        <VLRMULTA>0</VLRMULTA>\n" +
            "        <ORDEMCARGA>0</ORDEMCARGA>\n" +
            "        <CODBCO>1</CODBCO>\n" +
            "        <DESDOBRAMENTO>1</DESDOBRAMENTO>\n" +
            "        <HISTORICO>DEPÓSITO NÃO IDENTIFICADO</HISTORICO>\n" +
            "        <PROVISAO>N</PROVISAO>\n" +
            "        <ORIGEM>F</ORIGEM>\n" +
            "        <DESDOBDUPL>ZZ</DESDOBDUPL>\n" +
            "        <TIPMARCCHEQ>I</TIPMARCCHEQ>\n" +
            "        <TIPMULTA>1</TIPMULTA>\n" +
            "        <TIPJURO>1</TIPJURO>\n" +
            "        <DTNEG>26/07/2019</DTNEG>\n" +
            "        <DTVENC>26/07/2019</DTVENC>\n" +
            "        <DTVENCINIC>26/07/2019</DTVENCINIC>\n" +
            "    </parcela>\n" +
            "</parcelas>";
}
