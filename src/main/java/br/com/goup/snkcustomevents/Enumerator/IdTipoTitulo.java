package br.com.goup.snkcustomevents.Enumerator;

public enum IdTipoTitulo {

    DINHEIRO(2), CHEQUE(3), BOLETO(4), CARTAO_DEBITO(12),
    CARTAO_CREDITO(7), DEPOSITO(15), CREDITO(26), SEM_TITULO(0);

    private int value;

    IdTipoTitulo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
