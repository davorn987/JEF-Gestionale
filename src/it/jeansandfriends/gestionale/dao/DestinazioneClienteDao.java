package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.DestinazioneCliente;

public class DestinazioneClienteDao {

    public List<DestinazioneCliente> findByClienteId(long clienteId) {
        String sql = "SELECT id, cliente_id, progressivo, ragione_sociale, indirizzo, cap, citta, provincia, nazione, telefono, cellulare, email " +
                     "FROM clienti_destinazioni WHERE cliente_id = ? ORDER BY progressivo";

        List<DestinazioneCliente> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DestinazioneCliente d = new DestinazioneCliente();
                    d.setId(rs.getLong("id"));
                    d.setClienteId(rs.getLong("cliente_id"));
                    d.setProgressivo(rs.getInt("progressivo"));
                    d.setRagioneSociale(rs.getString("ragione_sociale"));
                    d.setIndirizzo(rs.getString("indirizzo"));
                    d.setCap(rs.getString("cap"));
                    d.setCitta(rs.getString("citta"));
                    d.setProvincia(rs.getString("provincia"));
                    d.setNazione(rs.getString("nazione"));
                    d.setTelefono(rs.getString("telefono"));
                    d.setCellulare(rs.getString("cellulare"));
                    d.setEmail(rs.getString("email"));
                    list.add(d);
                }
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Destinazioni: " + ex.getMessage(), ex);
        }
    }

    public int getNextProgressivo(long clienteId) {
        String sql = "SELECT COALESCE(MAX(progressivo), 0) + 1 AS next_prog FROM clienti_destinazioni WHERE cliente_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("next_prog") : 1;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore calcolo progressivo destinazione: " + ex.getMessage(), ex);
        }
    }

    public void insert(DestinazioneCliente d) {
        String sql = "INSERT INTO clienti_destinazioni " +
                     "(cliente_id, progressivo, ragione_sociale, indirizzo, cap, citta, provincia, nazione, telefono, cellulare, email) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, d.getClienteId());
            ps.setInt(2, d.getProgressivo());
            ps.setString(3, d.getRagioneSociale());
            ps.setString(4, d.getIndirizzo());
            ps.setString(5, d.getCap());
            ps.setString(6, d.getCitta());

            // provincia = sigla (MI, BG...) oppure null
            ps.setString(7, d.getProvincia());

            // nazione = ISO2 (IT, FR...) oppure null
            ps.setString(8, d.getNazione());

            ps.setString(9, d.getTelefono());
            ps.setString(10, d.getCellulare());
            ps.setString(11, d.getEmail());

            ps.executeUpdate();

        } catch (SQLException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("unique") && msg.contains("clienti_destinazioni.cliente_id")
                    && msg.contains("clienti_destinazioni.progressivo")) {
                throw new DuplicateKeyException("Progressivo destinazione già esistente per questo cliente.", ex);
            }
            throw new RuntimeException("Errore inserimento Destinazione: " + ex.getMessage(), ex);
        }
    }

    public void update(DestinazioneCliente d) {
        if (d.getId() == null) throw new IllegalArgumentException("Destinazione senza id");

        String sql = "UPDATE clienti_destinazioni SET " +
                     "ragione_sociale=?, indirizzo=?, cap=?, citta=?, provincia=?, nazione=?, telefono=?, cellulare=?, email=? " +
                     "WHERE id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, d.getRagioneSociale());
            ps.setString(2, d.getIndirizzo());
            ps.setString(3, d.getCap());
            ps.setString(4, d.getCitta());
            ps.setString(5, d.getProvincia()); // sigla
            ps.setString(6, d.getNazione());   // ISO2
            ps.setString(7, d.getTelefono());
            ps.setString(8, d.getCellulare());
            ps.setString(9, d.getEmail());
            ps.setLong(10, d.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore aggiornamento Destinazione: " + ex.getMessage(), ex);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM clienti_destinazioni WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore eliminazione Destinazione: " + ex.getMessage(), ex);
        }
    }
}