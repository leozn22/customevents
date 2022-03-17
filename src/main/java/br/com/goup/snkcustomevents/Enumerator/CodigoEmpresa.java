package br.com.goup.snkcustomevents.Enumerator;

public enum CodigoEmpresa {
    ZAP_GRAFICA(1), GRAFICA_SANTA_CECILIA(21);

    private int value;

    CodigoEmpresa(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
