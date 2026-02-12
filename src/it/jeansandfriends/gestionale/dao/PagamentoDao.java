package it.jeansandfriends.gestionale.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.model.Pagamento;

public class PagamentoDao {

    public List<Pagamento> findAll() {
        String sql =
            "SELECT p.id, p.codice, p.descrizione, p.tipo_pagamento_id, " +
            "       t.codice AS tipo_codice, t.descrizione AS tipo_descrizione, " +
            "       p.nr_rate, p.distanza_rate_giorni, p.giorni_prima_rata " +
            "FROM pagamenti p " +
            "JOIN pagamenti_tipi t ON t.id = p.tipo_pagamento_id " +
            "ORDER BY CAST(p.codice AS INTEGER), p.codice";

        List<Pagamento> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Pagamenti: " + ex.getMessage(), ex);
        }
    }

    public Pagamento findById(long id) {
        String sql =
            "SELECT p.id, p.codice, p.descrizione, p.tipo_pagamento_id, " +
            "       t.codice AS tipo_codice, t.descrizione AS tipo_descrizione, " +
            "       p.nr_rate, p.distanza_rate_giorni, p.giorni_prima_rata " +
            "FROM pagamenti p " +
            "JOIN pagamenti_tipi t ON t.id = p.tipo_pagamento_id " +
            "WHERE p.id = ?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Errore lettura Pagamento byId: " + ex.getMessage(), ex);
        }
    }

    /**
     * Progressivo "semplice": 1,2,3...
     * Nota: funziona se i codici già presenti sono numerici.
     */
    public int getNextCodice() {
        String sql = "SELECT COALESCE(MAX(CAST(codice AS INTEGER)), 0) + 1 AS next_code FROM pagamenti";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt("next_code") : 1;

        } catch (SQLException ex) {
            throw new RuntimeException("Errore calcolo prossimo codice Pagamento: " + ex.getMessage(), ex);
        }
    }

    public void insert(Pagamento p) {
        String sql =
            "INSERT INTO pagamenti (" +
            "codice, descrizione, tipo_pagamento_id, nr_rate, distanza_rate_giorni, giorni_prima_rata" +
            ") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, requiredTrim(p.getCodice(), "codice"));
            ps.setString(2, emptyToNull(p.getDescrizione()));

            if (p.getTipoPagamentoId() != null) ps.setLong(3, p.getTipoPagamentoId());
            else ps.setNull(3, java.sql.Types.INTEGER);

            ps.setInt(4, normalizeNrRate(p.getNrRate()));
            ps.setInt(5, normalizeNonNegative(p.getDistanzaRateGiorni()));
            ps.setInt(6, normalizeNonNegative(p.getGiorniPrimaRata()));

            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, p.getCodice());
        }
    }

    public void update(Pagamento p) {
        if (p.getId() == null) throw new IllegalArgumentException("Pagamento senza id");

        String sql =
            "UPDATE pagamenti SET " +
            "codice=?, descrizione=?, tipo_pagamento_id=?, nr_rate=?, distanza_rate_giorni=?, giorni_prima_rata=? " +
            "WHERE id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, requiredTrim(p.getCodice(), "codice"));
            ps.setString(2, emptyToNull(p.getDescrizione()));

            if (p.getTipoPagamentoId() != null) ps.setLong(3, p.getTipoPagamentoId());
            else ps.setNull(3, java.sql.Types.INTEGER);

            ps.setInt(4, normalizeNrRate(p.getNrRate()));
            ps.setInt(5, normalizeNonNegative(p.getDistanzaRateGiorni()));
            ps.setInt(6, normalizeNonNegative(p.getGiorniPrimaRata()));

            ps.setLong(7, p.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            handleSqlException(ex, p.getCodice());
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM pagamenti WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Errore eliminazione Pagamento: " + ex.getMessage(), ex);
        }
    }

    private static Pagamento map(ResultSet rs) throws SQLException {
        Pagamento p = new Pagamento();
        p.setId(rs.getLong("id"));
        p.setCodice(rs.getString("codice"));
        p.setDescrizione(rs.getString("descrizione"));

        long tipoId = rs.getLong("tipo_pagamento_id");
        p.setTipoPagamentoId(rs.wasNull() ? null : tipoId);

        p.setTipoPagamentoCodice(rs.getString("tipo_codice"));
        p.setTipoPagamentoDescrizione(rs.getString("tipo_descrizione"));

        int nrRate = rs.getInt("nr_rate");
        p.setNrRate(rs.wasNull() ? null : nrRate);

        int dist = rs.getInt("distanza_rate_giorni");
        p.setDistanzaRateGiorni(rs.wasNull() ? null : dist);

        int gg = rs.getInt("giorni_prima_rata");
        p.setGiorniPrimaRata(rs.wasNull() ? null : gg);

        return p;
    }

    private static void handleSqlException(SQLException ex, String codice) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("unique") && msg.contains("pagamenti.codice")) {
            throw new DuplicateKeyException("Codice Pagamento già esistente: " + codice, ex);
        }
        throw new RuntimeException("Errore salvataggio Pagamento: " + ex.getMessage(), ex);
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String requiredTrim(String s, String fieldName) {
        if (s == null) throw new IllegalArgumentException("Campo " + fieldName + " obbligatorio");
        String t = s.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Campo " + fieldName + " obbligatorio");
        return t;
    }

    private static int normalizeNrRate(Integer v) {
        if (v == null) return 1;
        if (v < 1) return 1;
        return v;
    }

    private static int normalizeNonNegative(Integer v) {
        if (v == null) return 0;
        if (v < 0) return 0;
        return v;
    }
}