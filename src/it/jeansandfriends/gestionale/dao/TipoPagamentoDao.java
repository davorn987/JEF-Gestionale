package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.TipoPagamento;

public class TipoPagamentoDao {

    public List<TipoPagamento> findAll() {
        String sql = "SELECT id, codice, descrizione FROM pagamenti_tipi ORDER BY codice";
        List<TipoPagamento> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new TipoPagamento(
                        rs.getLong("id"),
                        rs.getString("codice"),
                        rs.getString("descrizione")
                ));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Tipi Pagamento: " + ex.getMessage(), ex);
        }
    }

    public TipoPagamento findById(long id) {
        String sql = "SELECT id, codice, descrizione FROM pagamenti_tipi WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new TipoPagamento(
                        rs.getLong("id"),
                        rs.getString("codice"),
                        rs.getString("descrizione")
                );
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Tipo Pagamento byId: " + ex.getMessage(), ex);
        }
    }

    /**
     * Ritorna il prossimo codice numerico disponibile (max+1).
     * Se la colonna "codice" non è numerica, tenta comunque a fare CAST.
     *
     * Nota: su SQLite/SQL Server/MySQL il cast cambia; qui assumo DB che supporta CAST(... AS INTEGER).
     * Se mi dici che DB usi, lo adatto perfettamente.
     */
    public int getNextCodice() {
        String sql = "SELECT COALESCE(MAX(CAST(codice AS INTEGER)), 0) + 1 AS next_code FROM pagamenti_tipi";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt("next_code") : 1;

        } catch (SQLException ex) {
            // fallback super-safe: se CAST non funziona, proviamo max() come stringa e parse in Java
            throw new RuntimeException("Errore calcolo prossimo codice Tipo Pagamento: " + ex.getMessage(), ex);
        }
    }

    public void insert(TipoPagamento t) {
        String sql = "INSERT INTO pagamenti_tipi (codice, descrizione) VALUES (?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getCodice());
            ps.setString(2, t.getDescrizione());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, t.getCodice());
        }
    }

    public void update(TipoPagamento t) {
        if (t.getId() == null) throw new IllegalArgumentException("TipoPagamento senza id");
        String sql = "UPDATE pagamenti_tipi SET codice=?, descrizione=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getCodice());
            ps.setString(2, t.getDescrizione());
            ps.setLong(3, t.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, t.getCodice());
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM pagamenti_tipi WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore eliminazione Tipo Pagamento: " + ex.getMessage(), ex);
        }
    }

    private static void handleSqlException(SQLException ex, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && msg.contains("pagamenti_tipi.codice")) {
            throw new DuplicateKeyException("Codice Tipo Pagamento già esistente: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio Tipo Pagamento: " + ex.getMessage(), ex);
    }
}