package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.domain.RetornoLancamentoOrdemProducao;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class OrdemProducaoGradeAcao  implements AcaoRotinaJava {

   private OrdemProducao ordemProducao = null;
   private ContextoAcao contextoAcao;

   @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
       this.contextoAcao = contextoAcao;

       Integer numeroGrade = null;
       try {
           numeroGrade = Integer.parseInt(contextoAcao.getParam("NUMEROGRADE").toString());
       } catch (Exception e) {
           contextoAcao.mostraErro("Informe o número da grade válido! Erro: " + e.getMessage());
       }

       Integer processoZap = null;
       try {
           processoZap = Integer.parseInt(contextoAcao.getParam("PROCESSOZAP").toString());
       } catch (Exception e) {
           contextoAcao.mostraErro("Informe o processo Zap válido! Erro: " + e.getMessage());
       }

       ordemProducao = new OrdemProducao(contextoAcao);

       int qtdItensGrade = ordemProducao.quantidadeItensGrade(numeroGrade);

       if (qtdItensGrade == 0) {
           contextoAcao.mostraErro("Não existe itens na grade " + numeroGrade);
       }

       contextoAcao.confirmar("Gerar Produ\u00e7\u00e3o da Grade", "Existem " + qtdItensGrade + " iten(s) na grade " + numeroGrade + " deseja gerar a ordem de produção?", 1);

        this.processarOrdemProducaoGrade(numeroGrade, processoZap);
    }

    private void processarOrdemProducaoGrade(Integer codigoGrade, Integer processoZap) throws Exception {

        RetornoLancamentoOrdemProducao retorno = ordemProducao.processoProducaoGrade(codigoGrade, processoZap);

        String mensagem = retorno.getMsg();

        if (retorno.isSucesso()) {
            mensagem = "Ordem(ns) de Produ\u00e7\u00e3o nº: " +
                    "<b>[" +
                    retorno.getNumeroOrdem() +
                    "]</b>" +
                    " gerada com sucesso!";

        }

        contextoAcao.setMensagemRetorno(mensagem);
    }
}
