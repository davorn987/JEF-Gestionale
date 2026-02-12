package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Nazione;

public class NazioneDao {

    public List<Nazione> findAll(boolean includeInactive) {
        String sql =
            "SELECT id, codice_iso2, descrizione_it, attivo " +
            "FROM nazioni " +
            (includeInactive ? "" : "WHERE attivo = 1 ") +
            "ORDER BY CASE WHEN codice_iso2 = 'IT' THEN 0 ELSE 1 END, descrizione_it";

        List<Nazione> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Nazione n = new Nazione();
                n.setId(rs.getLong("id"));
                n.setCodiceIso2(rs.getString("codice_iso2"));
                n.setDescrizioneIt(rs.getString("descrizione_it"));
                n.setAttivo(rs.getInt("attivo") == 1);
                list.add(n);
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Nazioni: " + ex.getMessage(), ex);
        }
    }

    public List<Nazione> findAll() { return findAll(false); }

    public Nazione findById(long id, boolean includeInactive) {
        String sql =
            "SELECT id, codice_iso2, descrizione_it, attivo " +
            "FROM nazioni " +
            "WHERE id = ? " +
            (includeInactive ? "" : "AND attivo = 1 ");

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Nazione n = new Nazione();
                n.setId(rs.getLong("id"));
                n.setCodiceIso2(rs.getString("codice_iso2"));
                n.setDescrizioneIt(rs.getString("descrizione_it"));
                n.setAttivo(rs.getInt("attivo") == 1);
                return n;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Nazione byId: " + ex.getMessage(), ex);
        }
    }

    public Nazione findById(long id) { return findById(id, false); }

    public void insert(Nazione n) {
        String sql = "INSERT INTO nazioni (codice_iso2, descrizione_it, attivo) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, n.getCodiceIso2());
            ps.setString(2, n.getDescrizioneIt());
            ps.setInt(3, n.isAttivo() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, n.getCodiceIso2());
        }
    }

    public void update(Nazione n) {
        if (n.getId() == null) throw new IllegalArgumentException("Nazione senza id");
        String sql = "UPDATE nazioni SET codice_iso2=?, descrizione_it=?, attivo=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, n.getCodiceIso2());
            ps.setString(2, n.getDescrizioneIt());
            ps.setInt(3, n.isAttivo() ? 1 : 0);
            ps.setLong(4, n.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, n.getCodiceIso2());
        }
    }

    public void softDelete(long id) { setAttivo(id, false); }
    public void restore(long id) { setAttivo(id, true); }

    private void setAttivo(long id, boolean attivo) {
        String sql = "UPDATE nazioni SET attivo = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, attivo ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore aggiornamento stato Nazione: " + ex.getMessage(), ex);
        }
    }

    private static void handleSqlException(SQLException ex, String codiceIso2) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && msg.contains("nazioni.codice_iso2")) {
            throw new DuplicateKeyException("Codice nazione già esistente: " + codiceIso2, ex);
        }
        throw new RuntimeException("Errore salvataggio Nazione: " + ex.getMessage(), ex);
    }
}