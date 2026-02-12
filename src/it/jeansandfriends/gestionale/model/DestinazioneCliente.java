package it.jeansandfriends.gestionale.model;

public class DestinazioneCliente {
    private Long id;
    private Long clienteId;
    private int progressivo; // 1.. (UI: 0001)

    private String ragioneSociale;
    private String indirizzo;
    private String cap;
    private String citta;

    /**
     * Persistenza attuale su DB: TEXT.
     * Con la nuova UI questi campi vengono impostati come:
     * - provincia: sigla provincia (es "MI")
     * - nazione: ISO2 nazione (es "IT")
     */
    private String provincia;
    private String nazione;

    private String telefono;
    private String cellulare;
    private String email;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public int getProgressivo() { return progressivo; }
    public void setProgressivo(int progressivo) { this.progressivo = progressivo; }

    public String getRagioneSociale() { return ragioneSociale; }
    public void setRagioneSociale(String ragioneSociale) { this.ragioneSociale = ragioneSociale; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }

    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getNazione() { return nazione; }
    public void setNazione(String nazione) { this.nazione = nazione; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCellulare() { return cellulare; }
    public void setCellulare(String cellulare) { this.cellulare = cellulare; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}