package br.com.goup.snkcustomevents.orders;

import br.com.goup.snkcustomevents.domain.ItemProducao;
import br.com.goup.snkcustomevents.domain.RetornoLancamentoOrdemProducao;
import br.com.goup.snkcustomevents.domain.ViewProducaoSaldoItem;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class OrdemProducaoAcao implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
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
		ctx.confirmar("Gerar Produ\u00e7\u00e3o", "Deseja gerar a produção dos iten(s) [" + joiner.toString() + "] ?", 1);
		this.processarProducao(ctx, listaItens);
	}

	private void processarProducao(ContextoAcao ctx, List<ViewProducaoSaldoItem> listaItens) throws Exception {
		List<ItemProducao> listaItemProducao = new ArrayList<>();

		for(ViewProducaoSaldoItem saldoItem: listaItens) {
			Optional<ItemProducao> item = listaItemProducao.stream().filter(i -> i.getCodigoProduto().equals(saldoItem.getTzaCodProd())).findAny();

			if (item.isPresent()) {
				item.get().setTamanhoLote(item.get().getTamanhoLote() + saldoItem.getQuantidade().intValue());
				item.get().getListaSaldoItem().add(saldoItem);
			} else {
				ItemProducao itemProducao = new ItemProducao();
				itemProducao.setCodigoPlanta(saldoItem.getCodigoPlanta().intValue());
				itemProducao.setCodigoProduto(saldoItem.getTzaCodProd());
				itemProducao.setTamanhoLote(itemProducao.getTamanhoLote() + saldoItem.getQuantidade().intValue());

				itemProducao.getListaSaldoItem().add(saldoItem);
				listaItemProducao.add(itemProducao);
			}
		}

		OrdemProducao ordemProducao = new OrdemProducao(ctx);
		List<RetornoLancamentoOrdemProducao> listaRetorno = new ArrayList<>();

		for (ItemProducao itemProducao: listaItemProducao) {
			RetornoLancamentoOrdemProducao retorno = ordemProducao.processoProducao(itemProducao);
			listaRetorno.add(retorno);
		}

		this.exibirMsgOp(ctx, listaRetorno);
	}

	private void exibirMsgOp(ContextoAcao ctx, List<RetornoLancamentoOrdemProducao> listaRetorno) {

		StringJoiner joiner = new StringJoiner(",");
		for (RetornoLancamentoOrdemProducao ret : listaRetorno) {
			String numeroOrdem = ret.getNumeroOrdem();
			joiner.add(numeroOrdem);
		}
		String mensagem = "Ordem(ns) de Produ\u00e7\u00e3o nº: " +
				"<b>[" +
				joiner.toString() +
				"]</b>" +
				" gerada com sucesso!";
		ctx.setMensagemRetorno(mensagem);
	}
}