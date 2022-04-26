package br.com.goup.snkcustomevents.domain.xml;

import java.math.BigDecimal;

public class ImpostosXmlCte {
    protected BigDecimal baseIcms;
    protected BigDecimal aliqIcms;
    protected BigDecimal vlrIcms;
    protected BigDecimal cfop;
    protected BigDecimal codTrib;

    public ImpostosXmlCte() {
    }

    public BigDecimal getBaseIcms() {
        return this.baseIcms;
    }

    public BigDecimal getAliqIcms() {
        return this.aliqIcms;
    }

    public BigDecimal getVlrIcms() {
        return this.vlrIcms;
    }

    public BigDecimal getCfop() {
        return this.cfop;
    }

    public BigDecimal getCodTrib() {
        return this.codTrib;
    }
}