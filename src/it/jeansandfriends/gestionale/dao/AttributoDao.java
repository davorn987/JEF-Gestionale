package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Attributo;

public class AttributoDao {

    public List<Attributo> findAll(boolean includeInactive) {
        String sql = "SELECT id, categoria_id, codice, descrizione, attivo FROM attributi " +
                     (includeInactive ? "" : "WHERE attivo = 1 ") +
                     "ORDER BY categoria_id, codice";
        List<Attributo> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Attributi: " + ex.getMessage(), ex);
        }
    }

    public List<Attributo> findAll() {
        return findAll(false);
    }

    public List<Attributo> findByCategoria(long categoriaId, boolean includeInactive) {
        String sql = "SELECT id, categoria_id, codice, descrizione, attivo FROM attributi " +
                     "WHERE categoria_id = ? " +
                     (includeInactive ? "" : "AND attivo = 1 ") +
                     "ORDER BY codice";
        List<Attributo> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Attributi per categoria: " + ex.getMessage(), ex);
        }
    }

    public Attributo findById(long id) {
        String sql = "SELECT id, categoria_id, codice, descrizione, attivo FROM attributi WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Attributo per id: " + ex.getMessage(), ex);
        }
    }

    public void insert(Attributo a) {
        String sql = "INSERT INTO attributi (categoria_id, codice, descrizione, attivo) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, a.getCategoriaId());
            ps.setString(2, a.getCodice());
            ps.setString(3, a.getDescrizione());
            ps.setInt(4, a.isAttivo() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, a.getCategoriaId(), a.getCodice());
        }
    }

    public void update(Attributo a) {
        if (a.getId() == null) throw new IllegalArgumentException("Attributo senza id");
        String sql = "UPDATE attributi SET categoria_id=?, codice=?, descrizione=?, attivo=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, a.getCategoriaId());
            ps.setString(2, a.getCodice());
            ps.setString(3, a.getDescrizione());
            ps.setInt(4, a.isAttivo() ? 1 : 0);
            ps.setLong(5, a.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, a.getCategoriaId(), a.getCodice());
        }
    }

    public void softDelete(long id) {
        String sql = "UPDATE attributi SET attivo = 0 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore oscuramento Attributo: " + ex.getMessage(), ex);
        }
    }

    public void restore(long id) {
        String sql = "UPDATE attributi SET attivo = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore ripristino Attributo: " + ex.getMessage(), ex);
        }
    }

    private static Attributo map(ResultSet rs) throws SQLException {
        return new Attributo(
                rs.getLong("id"),
                rs.getLong("categoria_id"),
                rs.getString("codice"),
                rs.getString("descrizione"),
                rs.getInt("attivo") == 1
        );
    }

    private static void handleSqlException(SQLException ex, Long categoriaId, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && (msg.contains("attributi.categoria_id") || msg.contains("ux_attributi_cat_codice"))) {
            throw new DuplicateKeyException("Codice Attributo già esistente per questa categoria: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio Attributo: " + ex.getMessage(), ex);
    }
}
