package it.jeansandfriends.gestionale.model;

public class AttributoCategoria {
    private Long id;
    private String codice;
    private String descrizione;
    private boolean attivo;

    public AttributoCategoria() {}

    public AttributoCategoria(Long id, String codice, String descrizione, boolean attivo) {
        this.id = id;
        this.codice = codice;
        this.descrizione = descrizione;
        this.attivo = attivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    @Override
    public String toString() {
        String desc = descrizione == null || descrizione.trim().isEmpty() ? "" : descrizione;
        if (desc.isEmpty()) return codice != null ? codice : "";
        return codice + " - " + desc;
    }
}
