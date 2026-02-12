package it.jeansandfriends.gestionale.model;

public class Attributo {
    private Long id;
    private Long categoriaId;
    private String codice;
    private String descrizione;
    private boolean attivo;

    public Attributo() {}

    public Attributo(Long id, Long categoriaId, String codice, String descrizione, boolean attivo) {
        this.id = id;
        this.categoriaId = categoriaId;
        this.codice = codice;
        this.descrizione = descrizione;
        this.attivo = attivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

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
