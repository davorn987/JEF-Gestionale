package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.AttributoCategoria;

public class AttributoCategoriaDao {

    public List<AttributoCategoria> findAll(boolean includeInactive) {
        String sql = "SELECT id, codice, descrizione, attivo FROM attributi_categorie " +
                     (includeInactive ? "" : "WHERE attivo = 1 ") +
                     "ORDER BY codice";
        List<AttributoCategoria> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Categorie Attributi: " + ex.getMessage(), ex);
        }
    }

    public List<AttributoCategoria> findAll() {
        return findAll(false);
    }

    public AttributoCategoria findById(long id) {
        String sql = "SELECT id, codice, descrizione, attivo FROM attributi_categorie WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Categoria Attributo per id: " + ex.getMessage(), ex);
        }
    }

    public void insert(AttributoCategoria ac) {
        String sql = "INSERT INTO attributi_categorie (codice, descrizione, attivo) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, ac.getCodice());
            ps.setString(2, ac.getDescrizione());
            ps.setInt(3, ac.isAttivo() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, ac.getCodice());
        }
    }

    public void update(AttributoCategoria ac) {
        if (ac.getId() == null) throw new IllegalArgumentException("AttributoCategoria senza id");
        String sql = "UPDATE attributi_categorie SET codice=?, descrizione=?, attivo=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, ac.getCodice());
            ps.setString(2, ac.getDescrizione());
            ps.setInt(3, ac.isAttivo() ? 1 : 0);
            ps.setLong(4, ac.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, ac.getCodice());
        }
    }

    public void softDelete(long id) {
        String sql = "UPDATE attributi_categorie SET attivo = 0 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore oscuramento Categoria Attributo: " + ex.getMessage(), ex);
        }
    }

    public void restore(long id) {
        String sql = "UPDATE attributi_categorie SET attivo = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore ripristino Categoria Attributo: " + ex.getMessage(), ex);
        }
    }

    private static AttributoCategoria map(ResultSet rs) throws SQLException {
        return new AttributoCategoria(
                rs.getLong("id"),
                rs.getString("codice"),
                rs.getString("descrizione"),
                rs.getInt("attivo") == 1
        );
    }

    private static void handleSqlException(SQLException ex, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && (msg.contains("attributi_categorie.codice") || msg.contains("ux_attributi_categorie_codice"))) {
            throw new DuplicateKeyException("Codice Categoria Attributo già esistente: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio Categoria Attributo: " + ex.getMessage(), ex);
    }
}
