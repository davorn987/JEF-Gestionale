package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Fornitore;

public class FornitoreDao {

    public List<Fornitore> findAll(boolean includeInactive) {
        String sql =
            "SELECT id, codice_fornitore, attivo, ragione_sociale, indirizzo, cap, citta, " +
            "       provincia_id, nazione_id, " +
            "       telefono, cellulare, piva, cf, iva_id, pagamento_id, email, note " +
            "FROM fornitori " +
            (includeInactive ? "" : "WHERE attivo = 1 ") +
            "ORDER BY codice_fornitore";

        List<Fornitore> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Fornitori: " + ex.getMessage(), ex);
        }
    }

    public int getNextCodiceFornitore() {
        String sql = "SELECT COALESCE(MAX(codice_fornitore), 0) + 1 AS next_code FROM fornitori";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_code") : 1;
        } catch (SQLException ex) {
            throw new RuntimeException("Errore calcolo prossimo codice fornitore: " + ex.getMessage(), ex);
        }
    }

    public void insert(Fornitore f) {
        String sql =
            "INSERT INTO fornitori (" +
            "codice_fornitore, attivo, ragione_sociale, indirizzo, cap, citta, provincia_id, nazione_id, " +
            "telefono, cellulare, piva, cf, iva_id, pagamento_id, email, note" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, f.getCodiceFornitore());
            ps.setInt(2, f.isAttivo() ? 1 : 0);

            ps.setString(3, f.getRagioneSociale());
            ps.setString(4, f.getIndirizzo());
            ps.setString(5, f.getCap());
            ps.setString(6, f.getCitta());

            if (f.getProvinciaId() != null) ps.setLong(7, f.getProvinciaId()); else ps.setNull(7, java.sql.Types.INTEGER);
            if (f.getNazioneId() != null) ps.setLong(8, f.getNazioneId()); else ps.setNull(8, java.sql.Types.INTEGER);

            ps.setString(9, f.getTelefono());
            ps.setString(10, f.getCellulare());
            ps.setString(11, f.getPiva());
            ps.setString(12, f.getCf());

            if (f.getIvaId() != null) ps.setLong(13, f.getIvaId()); else ps.setNull(13, java.sql.Types.INTEGER);
            if (f.getPagamentoId() != null) ps.setLong(14, f.getPagamentoId()); else ps.setNull(14, java.sql.Types.INTEGER);

            ps.setString(15, f.getEmail());
            ps.setString(16, f.getNote());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore inserimento Fornitore: " + ex.getMessage(), ex);
        }
    }

    public void update(Fornitore f) {
        if (f.getId() == null) throw new IllegalArgumentException("Impossibile aggiornare fornitore senza id");

        String sql =
            "UPDATE fornitori SET " +
            "ragione_sociale=?, indirizzo=?, cap=?, citta=?, provincia_id=?, nazione_id=?, " +
            "telefono=?, cellulare=?, piva=?, cf=?, iva_id=?, pagamento_id=?, email=?, note=? " +
            "WHERE id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, f.getRagioneSociale());
            ps.setString(2, f.getIndirizzo());
            ps.setString(3, f.getCap());
            ps.setString(4, f.getCitta());

            if (f.getProvinciaId() != null) ps.setLong(5, f.getProvinciaId()); else ps.setNull(5, java.sql.Types.INTEGER);
            if (f.getNazioneId() != null) ps.setLong(6, f.getNazioneId()); else ps.setNull(6, java.sql.Types.INTEGER);

            ps.setString(7, f.getTelefono());
            ps.setString(8, f.getCellulare());
            ps.setString(9, f.getPiva());
            ps.setString(10, f.getCf());

            if (f.getIvaId() != null) ps.setLong(11, f.getIvaId()); else ps.setNull(11, java.sql.Types.INTEGER);
            if (f.getPagamentoId() != null) ps.setLong(12, f.getPagamentoId()); else ps.setNull(12, java.sql.Types.INTEGER);

            ps.setString(13, f.getEmail());
            ps.setString(14, f.getNote());

            ps.setLong(15, f.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore aggiornamento Fornitore: " + ex.getMessage(), ex);
        }
    }

    public void softDelete(long id) {
        String sql = "UPDATE fornitori SET attivo = 0 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore oscuramento Fornitore: " + ex.getMessage(), ex);
        }
    }

    public void restore(long id) {
        String sql = "UPDATE fornitori SET attivo = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore ripristino Fornitore: " + ex.getMessage(), ex);
        }
    }

    public Fornitore findById(long fornitoreId) {
        String sql =
            "SELECT id, codice_fornitore, attivo, ragione_sociale, indirizzo, cap, citta, " +
            "       provincia_id, nazione_id, " +
            "       telefono, cellulare, piva, cf, iva_id, pagamento_id, email, note " +
            "FROM fornitori WHERE id = ?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, fornitoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura fornitore per id: " + ex.getMessage(), ex);
        }
    }

    private static Fornitore map(ResultSet rs) throws SQLException {
        Fornitore f = new Fornitore();
        f.setId(rs.getLong("id"));
        f.setCodiceFornitore(rs.getInt("codice_fornitore"));
        f.setAttivo(rs.getInt("attivo") == 1);

        f.setRagioneSociale(rs.getString("ragione_sociale"));
        f.setIndirizzo(rs.getString("indirizzo"));
        f.setCap(rs.getString("cap"));
        f.setCitta(rs.getString("citta"));

        long provId = rs.getLong("provincia_id");
        f.setProvinciaId(rs.wasNull() ? null : provId);

        long nazId = rs.getLong("nazione_id");
        f.setNazioneId(rs.wasNull() ? null : nazId);

        f.setTelefono(rs.getString("telefono"));
        f.setCellulare(rs.getString("cellulare"));
        f.setPiva(rs.getString("piva"));
        f.setCf(rs.getString("cf"));

        long ivaId = rs.getLong("iva_id");
        f.setIvaId(rs.wasNull() ? null : ivaId);

        long pagId = rs.getLong("pagamento_id");
        f.setPagamentoId(rs.wasNull() ? null : pagId);

        f.setEmail(rs.getString("email"));
        f.setNote(rs.getString("note"));

        return f;
    }
}
