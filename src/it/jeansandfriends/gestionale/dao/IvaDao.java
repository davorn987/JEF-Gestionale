package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Iva;

public class IvaDao {

    public List<Iva> findAll() {
        String sql = "SELECT id, codice, descrizione, percentuale FROM iva ORDER BY codice";
        List<Iva> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Iva i = new Iva();
                i.setId(rs.getLong("id"));
                i.setCodice(rs.getString("codice"));
                i.setDescrizione(rs.getString("descrizione"));
                i.setPercentuale(rs.getDouble("percentuale"));
                list.add(i);
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura IVA: " + ex.getMessage(), ex);
        }
    }

    public Iva findById(long id) {
        String sql = "SELECT id, codice, descrizione, percentuale FROM iva WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Iva i = new Iva();
                i.setId(rs.getLong("id"));
                i.setCodice(rs.getString("codice"));
                i.setDescrizione(rs.getString("descrizione"));
                i.setPercentuale(rs.getDouble("percentuale"));
                return i;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura IVA byId: " + ex.getMessage(), ex);
        }
    }

    public void insert(Iva i) {
        String sql = "INSERT INTO iva (codice, descrizione, percentuale) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, i.getCodice());
            ps.setString(2, i.getDescrizione());
            ps.setDouble(3, i.getPercentuale());

            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, i.getCodice());
        }
    }

    public void update(Iva i) {
        if (i.getId() == null) throw new IllegalArgumentException("IVA senza id");

        String sql = "UPDATE iva SET codice=?, descrizione=?, percentuale=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, i.getCodice());
            ps.setString(2, i.getDescrizione());
            ps.setDouble(3, i.getPercentuale());
            ps.setLong(4, i.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, i.getCodice());
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM iva WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore eliminazione IVA: " + ex.getMessage(), ex);
        }
    }

    private static void handleSqlException(SQLException ex, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && msg.contains("iva.codice")) {
            throw new DuplicateKeyException("Codice IVA già esistente: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio IVA: " + ex.getMessage(), ex);
    }
}