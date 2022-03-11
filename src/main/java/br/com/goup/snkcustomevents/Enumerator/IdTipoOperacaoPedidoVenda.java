package br.com.goup.snkcustomevents.Enumerator;

public enum IdTipoOperacaoPedidoVenda implements IdTipoOperacao {

    TOP_PEDIDO_VENDA(3106), TOP_PEDIDO_REPETICAO(3107), TOP_PEDIDO_ERRO_EMPRESA(3108),
    TOP_PEDIDO_CREDITO(3110), TOP_PEDIDO_VENDA_INTERNA(3111), TOP_NF_SIMPLES_REMESSA(3204);

    private int value;

    IdTipoOperacaoPedidoVenda(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
