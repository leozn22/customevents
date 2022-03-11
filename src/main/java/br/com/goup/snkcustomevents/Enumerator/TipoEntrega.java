package br.com.goup.snkcustomevents.Enumerator;

public enum TipoEntrega {

    PORTA_A_PORTA("PP"),
    BASE("BS"), BALCAO_MG("BM");

    private String value;

    TipoEntrega(String value) {
        this.value =  value;
    }

    public String getValue() {
        return value;
    }
}
