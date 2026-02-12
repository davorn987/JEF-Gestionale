package it.jeansandfriends.gestionale.model;

public class TipoPagamento {
    private Long id;
    private String codice;
    private String descrizione;

    public TipoPagamento() {}

    public TipoPagamento(Long id, String codice, String descrizione) {
        this.id = id;
        this.codice = codice;
        this.descrizione = descrizione;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
}