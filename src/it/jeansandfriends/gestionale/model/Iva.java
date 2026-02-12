package it.jeansandfriends.gestionale.model;

public class Iva {
    private Long id;
    private String codice;
    private String descrizione;
    private double percentuale;

    public Iva() {}

    public Iva(Long id, String codice, String descrizione, double percentuale) {
        this.id = id;
        this.codice = codice;
        this.descrizione = descrizione;
        this.percentuale = percentuale;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public double getPercentuale() { return percentuale; }
    public void setPercentuale(double percentuale) { this.percentuale = percentuale; }

    @Override
    public String toString() {
        String c = codice == null ? "" : codice;
        String d = descrizione == null ? "" : descrizione;
        String pct = String.valueOf(percentuale).replace(',', '.');
        if (!pct.contains(".")) pct = pct + ".0";
        if (d.isEmpty()) return c + " (" + pct + "%)";
        return c + " - " + d + " (" + pct + "%)";
    }
}