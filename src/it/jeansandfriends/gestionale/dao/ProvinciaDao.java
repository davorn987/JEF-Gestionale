package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Provincia;

public class ProvinciaDao {

    public List<Provincia> findAll(boolean includeInactive) {
        String sql =
            "SELECT id, sigla, nome, attivo " +
            "FROM province " +
            (includeInactive ? "" : "WHERE attivo = 1 ") +
            "ORDER BY sigla";

        List<Provincia> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Provincia p = new Provincia();
                p.setId(rs.getLong("id"));
                p.setSigla(rs.getString("sigla"));
                p.setNome(rs.getString("nome"));
                p.setAttivo(rs.getInt("attivo") == 1);
                list.add(p);
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Province: " + ex.getMessage(), ex);
        }
    }

    public List<Provincia> findAll() {
        return findAll(false);
    }

    public Provincia findById(long id, boolean includeInactive) {
        String sql =
            "SELECT id, sigla, nome, attivo " +
            "FROM province " +
            "WHERE id = ? " +
            (includeInactive ? "" : "AND attivo = 1 ");

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Provincia p = new Provincia();
                p.setId(rs.getLong("id"));
                p.setSigla(rs.getString("sigla"));
                p.setNome(rs.getString("nome"));
                p.setAttivo(rs.getInt("attivo") == 1);
                return p;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Provincia byId: " + ex.getMessage(), ex);
        }
    }

    public Provincia findById(long id) {
        return findById(id, false);
    }

    public void insert(Provincia p) {
        String sql = "INSERT INTO province (sigla, nome, attivo) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getSigla());
            ps.setString(2, p.getNome());
            ps.setInt(3, p.isAttivo() ? 1 : 0);
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, p.getSigla());
        }
    }

    public void update(Provincia p) {
        if (p.getId() == null) throw new IllegalArgumentException("Provincia senza id");

        String sql = "UPDATE province SET sigla=?, nome=?, attivo=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getSigla());
            ps.setString(2, p.getNome());
            ps.setInt(3, p.isAttivo() ? 1 : 0);
            ps.setLong(4, p.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, p.getSigla());
        }
    }

    public void softDelete(long id) { setAttivo(id, false); }
    public void restore(long id) { setAttivo(id, true); }

    private void setAttivo(long id, boolean attivo) {
        String sql = "UPDATE province SET attivo = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, attivo ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore aggiornamento stato Provincia: " + ex.getMessage(), ex);
        }
    }

    private static void handleSqlException(SQLException ex, String sigla) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && msg.contains("province.sigla")) {
            throw new DuplicateKeyException("Sigla provincia già esistente: " + sigla, ex);
        }
        throw new RuntimeException("Errore salvataggio Provincia: " + ex.getMessage(), ex);
    }
}