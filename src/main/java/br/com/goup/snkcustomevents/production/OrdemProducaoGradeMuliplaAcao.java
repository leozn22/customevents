package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.domain.RetornoLancamentoOrdemProducao;
import br.com.goup.snkcustomevents.production.helpers.OrdemProducaoHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tabela: TPRIPROC - (Dicionário de Dados)
 * Descrição: Gerar OP (Multiplas Grades)
 * Parâmetros: (1)
 *
 * Descrição: Numero das Grades (Separar por virgula)
 * Nome: NUMEROGRADES
 * Tipo de parâmetro: Texto
 * Obrigatório: true
 */
public class OrdemProducaoGradeMuliplaAcao implements AcaoRotinaJava {
    private OrdemProducaoHelper ordemProducao = null;
    private ContextoAcao contextoAcao;
    private List<String> listaGrades;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        this.contextoAcao = contextoAcao;

        String grades = null;
        try {
            grades = contextoAcao.getParam("NUMEROGRADES").toString();
            listaGrades = Stream.of(grades.split(",")).map(String::trim).collect(Collectors.toList());
        } catch (Exception e) {
            contextoAcao.mostraErro("Informe o número da grade válido! Erro: " + e.getMessage());
        }

        ordemProducao = new OrdemProducaoHelper(contextoAcao);

        for (String numeroGrade : listaGrades) {
            int qtdItensGrade = 0;

            try {
                qtdItensGrade = ordemProducao.quantidadeItensGrade(Integer.parseInt(numeroGrade));
            } catch (Exception e) {
                contextoAcao.mostraErro("Falha ao verificar o numero da grade. Erro: " + e.getMessage());
            }

            if (qtdItensGrade == 0) {
                contextoAcao.mostraErro("Não existe itens na grade " + numeroGrade);
            }
        }

        this.processarOrdemProducaoGrade();
    }

    private void processarOrdemProducaoGrade() {
        RetornoLancamentoOrdemProducao retorno = ordemProducao.processoProducaoListaGrade(this.listaGrades);

        String mensagem = retorno.getMsg();

        if (retorno.isSucesso()) {
            mensagem = "Ordem(ns) de Produ\u00e7\u00e3o nº: " +
                       "<b>[" + String.join(",", retorno.getListaNumeroOrdem()) + "]</b>" +
                       " gerada com sucesso!";
        }

        contextoAcao.setMensagemRetorno(mensagem);
    }
}
