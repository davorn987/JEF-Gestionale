package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.UnitaMisura;

public class UnitaMisuraDao {

    public List<UnitaMisura> findAll(boolean includeInactive) {
        String sql = "SELECT id, codice, descrizione, attivo FROM unita_misura " +
                     (includeInactive ? "" : "WHERE attivo = 1 ") +
                     "ORDER BY codice";
        List<UnitaMisura> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Unità di Misura: " + ex.getMessage(), ex);
        }
    }

    public List<UnitaMisura> findAll() {
        return findAll(false);
    }

    public UnitaMisura findById(long id) {
        String sql = "SELECT id, codice, descrizione, attivo FROM unita_misura WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Unità di Misura per id: " + ex.getMessage(), ex);
        }
    }

    public void insert(UnitaMisura u) {
        String sql = "INSERT INTO unita_misura (codice, descrizione, attivo) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getCodice());
            ps.setString(2, u.getDescrizione());
            ps.setInt(3, u.isAttivo() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, u.getCodice());
        }
    }

    public void update(UnitaMisura u) {
        if (u.getId() == null) throw new IllegalArgumentException("UnitaMisura senza id");
        String sql = "UPDATE unita_misura SET codice=?, descrizione=?, attivo=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getCodice());
            ps.setString(2, u.getDescrizione());
            ps.setInt(3, u.isAttivo() ? 1 : 0);
            ps.setLong(4, u.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, u.getCodice());
        }
    }

    public void softDelete(long id) {
        String sql = "UPDATE unita_misura SET attivo = 0 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore oscuramento Unità di Misura: " + ex.getMessage(), ex);
        }
    }

    public void restore(long id) {
        String sql = "UPDATE unita_misura SET attivo = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore ripristino Unità di Misura: " + ex.getMessage(), ex);
        }
    }

    private static UnitaMisura map(ResultSet rs) throws SQLException {
        return new UnitaMisura(
                rs.getLong("id"),
                rs.getString("codice"),
                rs.getString("descrizione"),
                rs.getInt("attivo") == 1
        );
    }

    private static void handleSqlException(SQLException ex, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && (msg.contains("unita_misura.codice") || msg.contains("ux_unita_misura_codice"))) {
            throw new DuplicateKeyException("Codice Unità di Misura già esistente: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio Unità di Misura: " + ex.getMessage(), ex);
    }
}
