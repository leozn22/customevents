package br.com.goup.snkcustomevents.production;

import br.com.goup.snkcustomevents.domain.ItemProducao;
import br.com.goup.snkcustomevents.domain.RetornoLancamentoOrdemProducao;
import br.com.goup.snkcustomevents.domain.ViewProducaoSaldoItem;
import br.com.goup.snkcustomevents.production.helpers.OrdemProducaoHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Tabela: AD_TXPROD  (Construtor de tela)
 * Descrição: Gerar Produção
 *
 * Parâmetros: (0)
 */
public class OrdemProducaoAcao implements AcaoRotinaJava {

	private ContextoAcao contextoAcao;

	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
		this.contextoAcao = ctx;

		if (ctx.getLinhas() == null || ctx.getLinhas().length == 0) {
			ctx.mostraErro("Registro n\u00e3o selecionado!");	
		}

		List<ViewProducaoSaldoItem> listaItens = new ArrayList<>();

		for (Registro registro: ctx.getLinhas()) {
			listaItens.add(new ViewProducaoSaldoItem(registro));
		}

		StringJoiner joiner = new StringJoiner(",");
		for (ViewProducaoSaldoItem item : listaItens) {
			String tzaNuItem = item.getTzaNuItem();
			joiner.add(tzaNuItem);
		}
		ctx.confirmar("Gerar Produ\u00E7\u00E3o", "Deseja gerar a produ\u00E7\u00E3o dos iten(s) [" + joiner.toString() + "] ?", 1);
		this.processarProducao(listaItens);
	}

	private void processarProducao(List<ViewProducaoSaldoItem> listaItens) throws Exception {
		List<ItemProducao> listaItemProducao = new ArrayList<>();

		for(ViewProducaoSaldoItem saldoItem: listaItens) {
			Optional<ItemProducao> item = listaItemProducao.stream().filter(i -> i.getCodigoProduto().equals(saldoItem.getTzaCodProd())).findAny();

			if (item.isPresent()) {
				item.get().setTamanhoLote(item.get().getTamanhoLote().add(saldoItem.getQuantidade()));
				item.get().getListaSaldoItem().add(saldoItem);
			} else {
				ItemProducao itemProducao = new ItemProducao();
				itemProducao.setCodigoPlanta(saldoItem.getCodigoPlanta().intValue());
				itemProducao.setCodigoProduto(saldoItem.getTzaCodProd());
				itemProducao.setTamanhoLote(itemProducao.getTamanhoLote().add(saldoItem.getQuantidade()));

				itemProducao.getListaSaldoItem().add(saldoItem);
				listaItemProducao.add(itemProducao);
			}
		}

		OrdemProducaoHelper ordemProducao = new OrdemProducaoHelper(contextoAcao);
		List<RetornoLancamentoOrdemProducao> listaRetorno = new ArrayList<>();

		for (ItemProducao itemProducao: listaItemProducao) {
			RetornoLancamentoOrdemProducao retorno = ordemProducao.processoProducao(itemProducao);
			listaRetorno.add(retorno);
		}

		this.exibirMsg(listaRetorno);
	}

	public void exibirMsg(List<RetornoLancamentoOrdemProducao> listaRetorno) {
		StringJoiner listaOpSucesso = new StringJoiner(",");
		StringJoiner listaFalhaNulop = new StringJoiner(",");
		for (RetornoLancamentoOrdemProducao ret : listaRetorno) {
			if (ret.isSucesso()) {
				listaOpSucesso.add(String.join(",", ret.getListaNumeroOrdem()));
			} else {
				listaFalhaNulop.add(ret.getMsg());
			}
		}

		String msg = "";

		if (listaOpSucesso.length() > 0) {
			msg = "Ordem(ns) de Produ\u00e7\u00e3o nº: " +
					"<b>[" +
					listaOpSucesso.toString() +
					"]</b>" +
					" gerada com sucesso!";
		}

		if (listaFalhaNulop.length() > 0) {
			msg += "<br>Falha ao gerar OP: " + listaFalhaNulop.toString();
		}

		if (msg.equals("")) {
			msg = "Falha ao gerar OP erro n\u00E3o catalogado";
		}

		contextoAcao.setMensagemRetorno(msg);
	}
}
