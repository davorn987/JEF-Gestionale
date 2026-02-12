package it.jeansandfriends.gestionale.model;

public class Nazione {
    private Long id;
    private String codiceIso2;
    private String descrizioneIt;
    private boolean attivo = true;

    public Nazione() {}

    public Nazione(Long id, String codiceIso2, String descrizioneIt) {
        this.id = id;
        this.codiceIso2 = codiceIso2;
        this.descrizioneIt = descrizioneIt;
        this.attivo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodiceIso2() { return codiceIso2; }
    public void setCodiceIso2(String codiceIso2) { this.codiceIso2 = codiceIso2; }

    public String getDescrizioneIt() { return descrizioneIt; }
    public void setDescrizioneIt(String descrizioneIt) { this.descrizioneIt = descrizioneIt; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    @Override
    public String toString() {
        String c = codiceIso2 == null ? "" : codiceIso2;
        String d = descrizioneIt == null ? "" : descrizioneIt;
        if (c.isEmpty() && d.isEmpty()) return "";
        if (c.isEmpty()) return d;
        if (d.isEmpty()) return c;
        return c + " - " + d;
    }
}