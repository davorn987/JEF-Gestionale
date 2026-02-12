package it.jeansandfriends.gestionale.model;

public class Pagamento {
    private Long id;
    private String codice;
    private String descrizione;

    private Long tipoPagamentoId;

    private String tipoPagamentoCodice;
    private String tipoPagamentoDescrizione;

    private Integer nrRate;
    private Integer distanzaRateGiorni;
    private Integer giorniPrimaRata;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Long getTipoPagamentoId() { return tipoPagamentoId; }
    public void setTipoPagamentoId(Long tipoPagamentoId) { this.tipoPagamentoId = tipoPagamentoId; }

    public String getTipoPagamentoCodice() { return tipoPagamentoCodice; }
    public void setTipoPagamentoCodice(String tipoPagamentoCodice) { this.tipoPagamentoCodice = tipoPagamentoCodice; }

    public String getTipoPagamentoDescrizione() { return tipoPagamentoDescrizione; }
    public void setTipoPagamentoDescrizione(String tipoPagamentoDescrizione) { this.tipoPagamentoDescrizione = tipoPagamentoDescrizione; }

    public Integer getNrRate() { return nrRate; }
    public void setNrRate(Integer nrRate) { this.nrRate = nrRate; }

    public Integer getDistanzaRateGiorni() { return distanzaRateGiorni; }
    public void setDistanzaRateGiorni(Integer distanzaRateGiorni) { this.distanzaRateGiorni = distanzaRateGiorni; }

    public Integer getGiorniPrimaRata() { return giorniPrimaRata; }
    public void setGiorniPrimaRata(Integer giorniPrimaRata) { this.giorniPrimaRata = giorniPrimaRata; }

    @Override
    public String toString() {
        String c = codice == null ? "" : codice;
        String d = descrizione == null ? "" : descrizione;
        String t = tipoPagamentoCodice == null ? "" : tipoPagamentoCodice;

        String base = c;
        if (!d.isEmpty()) base = base + " - " + d;
        if (!t.isEmpty()) base = base + " [" + t + "]";
        return base;
    }
}