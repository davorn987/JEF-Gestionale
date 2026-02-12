package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Cliente;

public class ClienteDao {

    public List<Cliente> findAll(boolean includeInactive) {
        String sql =
            "SELECT id, codice_cliente, attivo, ragione_sociale, indirizzo, cap, citta, " +
            "       provincia_id, nazione_id, " +
            "       telefono, cellulare, piva, cf, iva_id, pagamento_id, fatturazione_cliente_id, email, note " +
            "FROM clienti " +
            (includeInactive ? "" : "WHERE attivo = 1 ") +
            "ORDER BY codice_cliente";

        List<Cliente> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Clienti: " + ex.getMessage(), ex);
        }
    }

    public int getNextCodiceCliente() {
        String sql = "SELECT COALESCE(MAX(codice_cliente), 0) + 1 AS next_code FROM clienti";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_code") : 1;
        } catch (SQLException ex) {
            throw new RuntimeException("Errore calcolo prossimo codice cliente: " + ex.getMessage(), ex);
        }
    }

    public void insert(Cliente cl) {
        String sql =
            "INSERT INTO clienti (" +
            "codice_cliente, attivo, ragione_sociale, indirizzo, cap, citta, provincia_id, nazione_id, " +
            "telefono, cellulare, piva, cf, iva_id, pagamento_id, fatturazione_cliente_id, email, note" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, cl.getCodiceCliente());
            ps.setInt(2, cl.isAttivo() ? 1 : 0);

            ps.setString(3, cl.getRagioneSociale());
            ps.setString(4, cl.getIndirizzo());
            ps.setString(5, cl.getCap());
            ps.setString(6, cl.getCitta());

            if (cl.getProvinciaId() != null) ps.setLong(7, cl.getProvinciaId()); else ps.setNull(7, java.sql.Types.INTEGER);
            if (cl.getNazioneId() != null) ps.setLong(8, cl.getNazioneId()); else ps.setNull(8, java.sql.Types.INTEGER);

            ps.setString(9, cl.getTelefono());
            ps.setString(10, cl.getCellulare());
            ps.setString(11, cl.getPiva());
            ps.setString(12, cl.getCf());

            if (cl.getIvaId() != null) ps.setLong(13, cl.getIvaId()); else ps.setNull(13, java.sql.Types.INTEGER);
            if (cl.getPagamentoId() != null) ps.setLong(14, cl.getPagamentoId()); else ps.setNull(14, java.sql.Types.INTEGER);
            if (cl.getFatturazioneClienteId() != null) ps.setLong(15, cl.getFatturazioneClienteId()); else ps.setNull(15, java.sql.Types.INTEGER);

            ps.setString(16, cl.getEmail());
            ps.setString(17, cl.getNote());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore inserimento Cliente: " + ex.getMessage(), ex);
        }
    }

    public void update(Cliente cl) {
        if (cl.getId() == null) throw new IllegalArgumentException("Impossibile aggiornare cliente senza id");

        String sql =
            "UPDATE clienti SET " +
            "ragione_sociale=?, indirizzo=?, cap=?, citta=?, provincia_id=?, nazione_id=?, " +
            "telefono=?, cellulare=?, piva=?, cf=?, iva_id=?, pagamento_id=?, fatturazione_cliente_id=?, email=?, note=? " +
            "WHERE id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cl.getRagioneSociale());
            ps.setString(2, cl.getIndirizzo());
            ps.setString(3, cl.getCap());
            ps.setString(4, cl.getCitta());

            if (cl.getProvinciaId() != null) ps.setLong(5, cl.getProvinciaId()); else ps.setNull(5, java.sql.Types.INTEGER);
            if (cl.getNazioneId() != null) ps.setLong(6, cl.getNazioneId()); else ps.setNull(6, java.sql.Types.INTEGER);

            ps.setString(7, cl.getTelefono());
            ps.setString(8, cl.getCellulare());
            ps.setString(9, cl.getPiva());
            ps.setString(10, cl.getCf());

            if (cl.getIvaId() != null) ps.setLong(11, cl.getIvaId()); else ps.setNull(11, java.sql.Types.INTEGER);
            if (cl.getPagamentoId() != null) ps.setLong(12, cl.getPagamentoId()); else ps.setNull(12, java.sql.Types.INTEGER);
            if (cl.getFatturazioneClienteId() != null) ps.setLong(13, cl.getFatturazioneClienteId()); else ps.setNull(13, java.sql.Types.INTEGER);

            ps.setString(14, cl.getEmail());
            ps.setString(15, cl.getNote());

            ps.setLong(16, cl.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore aggiornamento Cliente: " + ex.getMessage(), ex);
        }
    }

    public void softDelete(long id) {
        String sql = "UPDATE clienti SET attivo = 0 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore oscuramento Cliente: " + ex.getMessage(), ex);
        }
    }

    public void restore(long id) {
        String sql = "UPDATE clienti SET attivo = 1 WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Errore ripristino Cliente: " + ex.getMessage(), ex);
        }
    }

    // -----------------------------
    // SUPPORTO ANTI-LOOP
    // -----------------------------

    public Long findFatturazioneClienteId(long clienteId) {
        String sql = "SELECT fatturazione_cliente_id FROM clienti WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                long v = rs.getLong(1);
                return rs.wasNull() ? null : v;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura cliente fatturazione: " + ex.getMessage(), ex);
        }
    }

    public Cliente findById(long clienteId) {
        String sql =
            "SELECT id, codice_cliente, attivo, ragione_sociale, indirizzo, cap, citta, " +
            "       provincia_id, nazione_id, " +
            "       telefono, cellulare, piva, cf, iva_id, pagamento_id, fatturazione_cliente_id, email, note " +
            "FROM clienti WHERE id = ?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura cliente per id: " + ex.getMessage(), ex);
        }
    }

    private static Cliente map(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getLong("id"));
        c.setCodiceCliente(rs.getInt("codice_cliente"));
        c.setAttivo(rs.getInt("attivo") == 1);

        c.setRagioneSociale(rs.getString("ragione_sociale"));
        c.setIndirizzo(rs.getString("indirizzo"));
        c.setCap(rs.getString("cap"));
        c.setCitta(rs.getString("citta"));

        long provId = rs.getLong("provincia_id");
        c.setProvinciaId(rs.wasNull() ? null : provId);

        long nazId = rs.getLong("nazione_id");
        c.setNazioneId(rs.wasNull() ? null : nazId);

        c.setTelefono(rs.getString("telefono"));
        c.setCellulare(rs.getString("cellulare"));
        c.setPiva(rs.getString("piva"));
        c.setCf(rs.getString("cf"));

        long ivaId = rs.getLong("iva_id");
        c.setIvaId(rs.wasNull() ? null : ivaId);

        long pagId = rs.getLong("pagamento_id");
        c.setPagamentoId(rs.wasNull() ? null : pagId);

        long fattId = rs.getLong("fatturazione_cliente_id");
        c.setFatturazioneClienteId(rs.wasNull() ? null : fattId);

        c.setEmail(rs.getString("email"));
        c.setNote(rs.getString("note"));

        return c;
    }
}
