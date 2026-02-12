package it.jeansandfriends.gestionale.model;

public class Fornitore {
    private Long id;

    private int codiceFornitore;
    private boolean attivo;

    private String ragioneSociale;
    private String indirizzo;
    private String cap;
    private String citta;

    private Long provinciaId;
    private Long nazioneId;

    private String telefono;
    private String cellulare;
    private String piva;
    private String cf;

    private Long ivaId;
    private Long pagamentoId;

    private String email;
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getCodiceFornitore() { return codiceFornitore; }
    public void setCodiceFornitore(int codiceFornitore) { this.codiceFornitore = codiceFornitore; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    public String getRagioneSociale() { return ragioneSociale; }
    public void setRagioneSociale(String ragioneSociale) { this.ragioneSociale = ragioneSociale; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }

    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }

    public Long getProvinciaId() { return provinciaId; }
    public void setProvinciaId(Long provinciaId) { this.provinciaId = provinciaId; }

    public Long getNazioneId() { return nazioneId; }
    public void setNazioneId(Long nazioneId) { this.nazioneId = nazioneId; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCellulare() { return cellulare; }
    public void setCellulare(String cellulare) { this.cellulare = cellulare; }

    public String getPiva() { return piva; }
    public void setPiva(String piva) { this.piva = piva; }

    public String getCf() { return cf; }
    public void setCf(String cf) { this.cf = cf; }

    public Long getIvaId() { return ivaId; }
    public void setIvaId(Long ivaId) { this.ivaId = ivaId; }

    public Long getPagamentoId() { return pagamentoId; }
    public void setPagamentoId(Long pagamentoId) { this.pagamentoId = pagamentoId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        String cod = String.format("%06d", codiceFornitore);
        String rag = ragioneSociale == null ? "" : ragioneSociale;
        if (rag.trim().isEmpty()) return cod;
        return cod + " - " + rag;
    }
}
