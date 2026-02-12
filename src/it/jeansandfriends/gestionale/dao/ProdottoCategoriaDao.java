package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.ProdottoCategoria;

public class ProdottoCategoriaDao {

    public List<ProdottoCategoria> findAll(boolean includeInactive) {
        String sql = "SELECT id, codice, descrizione, attivo FROM prodotti_categorie " +
                     (includeInactive ? "" : "WHERE attivo = 1 ") +
                     "ORDER BY codice";
        List<ProdottoCategoria> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Categorie Prodotto: " + ex.getMessage(), ex);
        }
    }

    public List<ProdottoCategoria> findAll() {
        return findAll(false);
    }

    public ProdottoCategoria findById(long id) {
        String sql = "SELECT id, codice, descrizione, attivo FROM prodotti_categorie WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Categoria Prodotto per id: " + ex.getMessage(), ex);
        }
    }

    public void insert(ProdottoCategoria pc) {
        String sql = "INSERT INTO prodotti_categorie (codice, descrizione, attivo) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, pc.getCodice());
            ps.setString(2, pc.getDescrizione());
            ps.setInt(3, pc.isAttivo() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, pc.getCodice());
        }
    }

    public void update(ProdottoCategoria pc) {
        if (pc.getId() == null) throw new IllegalArgumentException("ProdottoCategoria senza id");
        String sql = "UPDATE prodotti_categorie SET codice=?, descrizione=?, attivo=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, pc.getCodice());
            ps.setString(2, pc.getDescrizione());
            ps.setInt(3, pc.isAttivo() ? 1 : 0);
            ps.setLong(4, pc.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, pc.getCodice());
        }
    }

    public void softDelete(long id) {
        String sql = "UPDATE prodotti_categorie SET attivo = 0 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore oscuramento Categoria Prodotto: " + ex.getMessage(), ex);
        }
    }

    public void restore(long id) {
        String sql = "UPDATE prodotti_categorie SET attivo = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore ripristino Categoria Prodotto: " + ex.getMessage(), ex);
        }
    }

    private static ProdottoCategoria map(ResultSet rs) throws SQLException {
        return new ProdottoCategoria(
                rs.getLong("id"),
                rs.getString("codice"),
                rs.getString("descrizione"),
                rs.getInt("attivo") == 1
        );
    }

    private static void handleSqlException(SQLException ex, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && (msg.contains("prodotti_categorie.codice") || msg.contains("ux_prodotti_categorie_codice"))) {
            throw new DuplicateKeyException("Codice Categoria Prodotto già esistente: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio Categoria Prodotto: " + ex.getMessage(), ex);
    }
}
