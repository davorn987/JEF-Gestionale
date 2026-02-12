package it.jeansandfriends.gestionale.model;

public class Provincia {
    private Long id;
    private String sigla;
    private String nome;
    private boolean attivo = true;

    public Provincia() {}

    public Provincia(Long id, String sigla, String nome) {
        this.id = id;
        this.sigla = sigla;
        this.nome = nome;
        this.attivo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    @Override
    public String toString() {
        String s = sigla == null ? "" : sigla;
        String n = nome == null ? "" : nome;
        if (s.isEmpty() && n.isEmpty()) return "";
        if (s.isEmpty()) return n;
        if (n.isEmpty()) return s;
        return s + " - " + n;
    }
}