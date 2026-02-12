package it.jeansandfriends.gestionale.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public final class Database {

    private static final String JDBC_PREFIX = "jdbc:sqlite:";

    private Database() {}

    public static void init() {
        loadSqliteDriver();

        try (Connection c = getConnection()) {
            c.setAutoCommit(true);
            createSchema(c);
        } catch (SQLException ex) {
            throw new RuntimeException("Errore inizializzazione DB: " + ex.getMessage(), ex);
        }
    }

    private static void loadSqliteDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Driver SQLite JDBC non trovato nel classpath. " +
                "Aggiungi sqlite-jdbc (xerial) al Build Path di Eclipse.", e
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = JDBC_PREFIX + AppPaths.getDbFile().getAbsolutePath();
        Connection c = DriverManager.getConnection(url);

        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return c;
    }

    private static void createSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {

            // --------------------
            // BASE
            // --------------------
            st.execute("CREATE TABLE IF NOT EXISTS azienda (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1)," +
                    "ragione_sociale TEXT NOT NULL," +
                    "piva TEXT," +
                    "cf TEXT," +
                    "indirizzo TEXT," +
                    "cap TEXT," +
                    "citta TEXT," +
                    "provincia TEXT," +
                    "nazione TEXT," +
                    "pec TEXT," +
                    "codice_sdi TEXT," +
                    "telefono TEXT," +
                    "email TEXT," +
                    "iban TEXT" +
                    ");");

            st.execute("CREATE TABLE IF NOT EXISTS iva (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT," +
                    "percentuale REAL NOT NULL" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_iva_codice ON iva(codice);");

            // --------------------
            // PAGAMENTI
            // --------------------
            st.execute("CREATE TABLE IF NOT EXISTS pagamenti_tipi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT NOT NULL" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_pagamenti_tipi_codice ON pagamenti_tipi(codice);");

            st.execute("CREATE TABLE IF NOT EXISTS pagamenti (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT," +
                    "tipo_pagamento_id INTEGER NOT NULL," +
                    "nr_rate INTEGER NOT NULL DEFAULT 1," +
                    "distanza_rate_giorni INTEGER NOT NULL DEFAULT 0," +
                    "giorni_prima_rata INTEGER NOT NULL DEFAULT 0," +
                    "FOREIGN KEY (tipo_pagamento_id) REFERENCES pagamenti_tipi(id)" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_pagamenti_codice ON pagamenti(codice);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_pagamenti_tipo ON pagamenti(tipo_pagamento_id);");

            // --------------------
            // NAZIONI + PROVINCE
            // --------------------
            // 1) create base table (anche vecchia)
            st.execute("CREATE TABLE IF NOT EXISTS nazioni (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice_iso2 TEXT NOT NULL," +
                    "descrizione_it TEXT NOT NULL" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_nazioni_codice ON nazioni(codice_iso2);");

            st.execute("CREATE TABLE IF NOT EXISTS province (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sigla TEXT NOT NULL," +
                    "nome TEXT NOT NULL" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_province_sigla ON province(sigla);");

            // 2) assicurati che esista colonna attivo PRIMA di creare indici su attivo
            ensureGeoColumns(st);

            // 3) indici su attivo (ora sicuri)
            st.execute("CREATE INDEX IF NOT EXISTS idx_nazioni_attivo ON nazioni(attivo);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_province_attivo ON province(attivo);");

            // --------------------
            // CLIENTI (migrazione se vecchia tabella)
            // --------------------
            migrateClientiIfNeeded(c);

            st.execute("CREATE TABLE IF NOT EXISTS clienti (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice_cliente INTEGER NOT NULL," +
                    "attivo INTEGER NOT NULL DEFAULT 1," +
                    "ragione_sociale TEXT NOT NULL," +
                    "indirizzo TEXT," +
                    "cap TEXT," +
                    "citta TEXT," +
                    "provincia_id INTEGER," +
                    "nazione_id INTEGER," +
                    "telefono TEXT," +
                    "cellulare TEXT," +
                    "piva TEXT," +
                    "cf TEXT," +
                    "iva_id INTEGER," +
                    "pagamento_id INTEGER," +
                    "fatturazione_cliente_id INTEGER," +
                    "email TEXT," +
                    "note TEXT," +
                    "FOREIGN KEY (iva_id) REFERENCES iva(id)," +
                    "FOREIGN KEY (pagamento_id) REFERENCES pagamenti(id)," +
                    "FOREIGN KEY (fatturazione_cliente_id) REFERENCES clienti(id)," +
                    "FOREIGN KEY (nazione_id) REFERENCES nazioni(id)," +
                    "FOREIGN KEY (provincia_id) REFERENCES province(id)" +
                    ");");

            tryAddColumn(st, "clienti", "provincia_id", "INTEGER");
            tryAddColumn(st, "clienti", "nazione_id", "INTEGER");

            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_clienti_codice_cliente ON clienti(codice_cliente);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_clienti_ragione_sociale ON clienti(ragione_sociale);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_clienti_attivo ON clienti(attivo);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_clienti_nazione ON clienti(nazione_id);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_clienti_provincia ON clienti(provincia_id);");

            // --------------------
            // FORNITORI
            // --------------------
            st.execute("CREATE TABLE IF NOT EXISTS fornitori (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice_fornitore INTEGER NOT NULL," +
                    "attivo INTEGER NOT NULL DEFAULT 1," +
                    "ragione_sociale TEXT NOT NULL," +
                    "indirizzo TEXT," +
                    "cap TEXT," +
                    "citta TEXT," +
                    "provincia_id INTEGER," +
                    "nazione_id INTEGER," +
                    "telefono TEXT," +
                    "cellulare TEXT," +
                    "piva TEXT," +
                    "cf TEXT," +
                    "iva_id INTEGER," +
                    "pagamento_id INTEGER," +
                    "email TEXT," +
                    "note TEXT," +
                    "FOREIGN KEY (iva_id) REFERENCES iva(id)," +
                    "FOREIGN KEY (pagamento_id) REFERENCES pagamenti(id)," +
                    "FOREIGN KEY (nazione_id) REFERENCES nazioni(id)," +
                    "FOREIGN KEY (provincia_id) REFERENCES province(id)" +
                    ");");

            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_fornitori_codice_fornitore ON fornitori(codice_fornitore);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_fornitori_ragione_sociale ON fornitori(ragione_sociale);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_fornitori_attivo ON fornitori(attivo);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_fornitori_nazione ON fornitori(nazione_id);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_fornitori_provincia ON fornitori(provincia_id);");

            // --------------------
            // DESTINAZIONI CLIENTE
            // --------------------
            st.execute("CREATE TABLE IF NOT EXISTS clienti_destinazioni (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cliente_id INTEGER NOT NULL," +
                    "progressivo INTEGER NOT NULL," +
                    "ragione_sociale TEXT," +
                    "indirizzo TEXT," +
                    "cap TEXT," +
                    "citta TEXT," +
                    "provincia TEXT," +
                    "nazione TEXT," +
                    "telefono TEXT," +
                    "cellulare TEXT," +
                    "email TEXT," +
                    "FOREIGN KEY (cliente_id) REFERENCES clienti(id)" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_dest_cliente_prog ON clienti_destinazioni(cliente_id, progressivo);");

            // --------------------
            // PRODOTTI - ANAGRAFICHE BASE
            // --------------------
            st.execute("CREATE TABLE IF NOT EXISTS prodotti_categorie (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT," +
                    "attivo INTEGER NOT NULL DEFAULT 1" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_prodotti_categorie_codice ON prodotti_categorie(codice);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_prodotti_categorie_attivo ON prodotti_categorie(attivo);");

            st.execute("CREATE TABLE IF NOT EXISTS unita_misura (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT," +
                    "attivo INTEGER NOT NULL DEFAULT 1" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_unita_misura_codice ON unita_misura(codice);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_unita_misura_attivo ON unita_misura(attivo);");

            st.execute("CREATE TABLE IF NOT EXISTS attributi_categorie (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT," +
                    "attivo INTEGER NOT NULL DEFAULT 1" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_attributi_categorie_codice ON attributi_categorie(codice);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_attributi_categorie_attivo ON attributi_categorie(attivo);");

            st.execute("CREATE TABLE IF NOT EXISTS attributi (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "categoria_id INTEGER NOT NULL," +
                    "codice TEXT NOT NULL," +
                    "descrizione TEXT," +
                    "attivo INTEGER NOT NULL DEFAULT 1," +
                    "FOREIGN KEY (categoria_id) REFERENCES attributi_categorie(id)" +
                    ");");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_attributi_cat_codice ON attributi(categoria_id, codice);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_attributi_categoria ON attributi(categoria_id);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_attributi_attivo ON attributi(attivo);");

            // --------------------
            // DEFAULTS + SEED
            // --------------------
            st.execute("INSERT OR IGNORE INTO azienda (id, ragione_sociale) VALUES (1, '');");

            seedNazioniFromLocaleIfEmpty(st);
            seedProvinceItaliaIfEmpty(st);
            seedUnitaMisuraIfEmpty(st);
        }
    }

    private static void ensureGeoColumns(Statement st) throws SQLException {
        // Se DB vecchio: aggiungi attivo con default 1
        tryAddColumn(st, "nazioni", "attivo", "INTEGER NOT NULL DEFAULT 1");
        tryAddColumn(st, "province", "attivo", "INTEGER NOT NULL DEFAULT 1");

        // Se ci sono record già presenti, assicuriamoci che abbiano attivo=1 (in caso di null da vecchie versioni)
        // Nota: con NOT NULL DEFAULT 1 normalmente non serve, ma su alcuni DB già popolati può capitare.
        try {
            st.execute("UPDATE nazioni SET attivo = 1 WHERE attivo IS NULL;");
        } catch (SQLException ignore) {}
        try {
            st.execute("UPDATE province SET attivo = 1 WHERE attivo IS NULL;");
        } catch (SQLException ignore) {}
    }

    private static void tryAddColumn(Statement st, String table, String column, String type) {
        try {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + ";");
        } catch (SQLException ignore) {
            // colonna già esistente -> ok
        }
    }

    private static void seedNazioniFromLocaleIfEmpty(Statement st) throws SQLException {
        int cnt = 0;
        try (ResultSet rs = st.executeQuery("SELECT COUNT(1) AS cnt FROM nazioni;")) {
            if (rs.next()) cnt = rs.getInt("cnt");
        }
        if (cnt > 0) return;

        Locale it = Locale.ITALIAN;
        for (String code : Locale.getISOCountries()) {
            Locale loc = new Locale("", code);
            String nameIt = loc.getDisplayCountry(it);
            if (nameIt == null || nameIt.trim().isEmpty()) continue;

            String safeCode = code.replace("'", "''");
            String safeName = nameIt.replace("'", "''");

            st.execute("INSERT OR IGNORE INTO nazioni (codice_iso2, descrizione_it, attivo) " +
                       "VALUES ('" + safeCode + "', '" + safeName + "', 1);");
        }
    }

    private static void seedProvinceItaliaIfEmpty(Statement st) throws SQLException {
        int cnt = 0;
        try (ResultSet rs = st.executeQuery("SELECT COUNT(1) AS cnt FROM province;")) {
            if (rs.next()) cnt = rs.getInt("cnt");
        }
        if (cnt > 0) return;

        // Lombardia
        insProv(st, "BG", "Bergamo");
        insProv(st, "BS", "Brescia");
        insProv(st, "CO", "Como");
        insProv(st, "CR", "Cremona");
        insProv(st, "LC", "Lecco");
        insProv(st, "LO", "Lodi");
        insProv(st, "MN", "Mantova");
        insProv(st, "MI", "Milano");
        insProv(st, "MB", "Monza e della Brianza");
        insProv(st, "PV", "Pavia");
        insProv(st, "SO", "Sondrio");
        insProv(st, "VA", "Varese");

        // Piemonte
        insProv(st, "AL", "Alessandria");
        insProv(st, "AT", "Asti");
        insProv(st, "BI", "Biella");
        insProv(st, "CN", "Cuneo");
        insProv(st, "NO", "Novara");
        insProv(st, "TO", "Torino");
        insProv(st, "VB", "Verbano-Cusio-Ossola");
        insProv(st, "VC", "Vercelli");

        // Valle d'Aosta
        insProv(st, "AO", "Aosta");

        // Liguria
        insProv(st, "GE", "Genova");
        insProv(st, "IM", "Imperia");
        insProv(st, "SP", "La Spezia");
        insProv(st, "SV", "Savona");

        // Veneto
        insProv(st, "BL", "Belluno");
        insProv(st, "PD", "Padova");
        insProv(st, "RO", "Rovigo");
        insProv(st, "TV", "Treviso");
        insProv(st, "VE", "Venezia");
        insProv(st, "VR", "Verona");
        insProv(st, "VI", "Vicenza");

        // Trentino-Alto Adige
        insProv(st, "BZ", "Bolzano");
        insProv(st, "TN", "Trento");

        // Friuli-Venezia Giulia
        insProv(st, "GO", "Gorizia");
        insProv(st, "PN", "Pordenone");
        insProv(st, "TS", "Trieste");
        insProv(st, "UD", "Udine");

        // Emilia-Romagna
        insProv(st, "BO", "Bologna");
        insProv(st, "FE", "Ferrara");
        insProv(st, "FC", "Forlì-Cesena");
        insProv(st, "MO", "Modena");
        insProv(st, "PR", "Parma");
        insProv(st, "PC", "Piacenza");
        insProv(st, "RA", "Ravenna");
        insProv(st, "RE", "Reggio Emilia");
        insProv(st, "RN", "Rimini");

        // Toscana
        insProv(st, "AR", "Arezzo");
        insProv(st, "FI", "Firenze");
        insProv(st, "GR", "Grosseto");
        insProv(st, "LI", "Livorno");
        insProv(st, "LU", "Lucca");
        insProv(st, "MS", "Massa-Carrara");
        insProv(st, "PI", "Pisa");
        insProv(st, "PT", "Pistoia");
        insProv(st, "PO", "Prato");
        insProv(st, "SI", "Siena");

        // Umbria
        insProv(st, "PG", "Perugia");
        insProv(st, "TR", "Terni");

        // Marche
        insProv(st, "AN", "Ancona");
        insProv(st, "AP", "Ascoli Piceno");
        insProv(st, "FM", "Fermo");
        insProv(st, "MC", "Macerata");
        insProv(st, "PU", "Pesaro e Urbino");

        // Lazio
        insProv(st, "FR", "Frosinone");
        insProv(st, "LT", "Latina");
        insProv(st, "RI", "Rieti");
        insProv(st, "RM", "Roma");
        insProv(st, "VT", "Viterbo");

        // Abruzzo
        insProv(st, "AQ", "L'Aquila");
        insProv(st, "CH", "Chieti");
        insProv(st, "PE", "Pescara");
        insProv(st, "TE", "Teramo");

        // Molise
        insProv(st, "CB", "Campobasso");
        insProv(st, "IS", "Isernia");

        // Campania
        insProv(st, "AV", "Avellino");
        insProv(st, "BN", "Benevento");
        insProv(st, "CE", "Caserta");
        insProv(st, "NA", "Napoli");
        insProv(st, "SA", "Salerno");

        // Puglia
        insProv(st, "BA", "Bari");
        insProv(st, "BT", "Barletta-Andria-Trani");
        insProv(st, "BR", "Brindisi");
        insProv(st, "FG", "Foggia");
        insProv(st, "LE", "Lecce");
        insProv(st, "TA", "Taranto");

        // Basilicata
        insProv(st, "MT", "Matera");
        insProv(st, "PZ", "Potenza");

        // Calabria
        insProv(st, "CZ", "Catanzaro");
        insProv(st, "CS", "Cosenza");
        insProv(st, "KR", "Crotone");
        insProv(st, "RC", "Reggio Calabria");
        insProv(st, "VV", "Vibo Valentia");

        // Sicilia
        insProv(st, "AG", "Agrigento");
        insProv(st, "CL", "Caltanissetta");
        insProv(st, "CT", "Catania");
        insProv(st, "EN", "Enna");
        insProv(st, "ME", "Messina");
        insProv(st, "PA", "Palermo");
        insProv(st, "RG", "Ragusa");
        insProv(st, "SR", "Siracusa");
        insProv(st, "TP", "Trapani");

        // Sardegna
        insProv(st, "CA", "Cagliari");
        insProv(st, "NU", "Nuoro");
        insProv(st, "OR", "Oristano");
        insProv(st, "SS", "Sassari");
        insProv(st, "SU", "Sud Sardegna");
    }

    private static void insProv(Statement st, String sigla, String nome) throws SQLException {
        String s = sigla.replace("'", "''");
        String n = nome.replace("'", "''");
        st.execute("INSERT OR IGNORE INTO province (sigla, nome, attivo) VALUES ('" + s + "', '" + n + "', 1);");
    }

    private static void seedUnitaMisuraIfEmpty(Statement st) throws SQLException {
        int cnt = 0;
        try (ResultSet rs = st.executeQuery("SELECT COUNT(1) AS cnt FROM unita_misura;")) {
            if (rs.next()) cnt = rs.getInt("cnt");
        }
        if (cnt > 0) return;

        st.execute("INSERT OR IGNORE INTO unita_misura (codice, descrizione, attivo) VALUES ('PZ', 'Pezzi', 1);");
        st.execute("INSERT OR IGNORE INTO unita_misura (codice, descrizione, attivo) VALUES ('KG', 'Chilogrammi', 1);");
        st.execute("INSERT OR IGNORE INTO unita_misura (codice, descrizione, attivo) VALUES ('MT', 'Metri', 1);");
        st.execute("INSERT OR IGNORE INTO unita_misura (codice, descrizione, attivo) VALUES ('LT', 'Litri', 1);");
    }

    private static void migrateClientiIfNeeded(Connection c) throws SQLException {
        if (!tableExists(c, "clienti")) return;
        if (columnExists(c, "clienti", "codice_cliente")) return;

        String backupName = "clienti_old_" + System.currentTimeMillis();

        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE clienti RENAME TO " + backupName + ";");

            st.execute("CREATE TABLE clienti (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codice_cliente INTEGER NOT NULL," +
                    "attivo INTEGER NOT NULL DEFAULT 1," +
                    "ragione_sociale TEXT NOT NULL," +
                    "indirizzo TEXT," +
                    "cap TEXT," +
                    "citta TEXT," +
                    "provincia_id INTEGER," +
                    "nazione_id INTEGER," +
                    "telefono TEXT," +
                    "cellulare TEXT," +
                    "piva TEXT," +
                    "cf TEXT," +
                    "iva_id INTEGER," +
                    "pagamento_id INTEGER," +
                    "fatturazione_cliente_id INTEGER," +
                    "email TEXT," +
                    "note TEXT" +
                    ");");

            try {
                st.execute(
                    "INSERT INTO clienti (codice_cliente, attivo, ragione_sociale, indirizzo, cap, citta, telefono, piva, cf, email, note) " +
                    "SELECT " +
                    "ROW_NUMBER() OVER (ORDER BY id) AS codice_cliente, " +
                    "1 AS attivo, " +
                    "ragione_sociale, indirizzo, cap, citta, telefono, piva, cf, email, note " +
                    "FROM " + backupName + " ORDER BY id;"
                );
            } catch (SQLException ex) {
                st.execute(
                    "INSERT INTO clienti (codice_cliente, attivo, ragione_sociale, indirizzo, cap, citta, telefono, piva, cf, email, note) " +
                    "SELECT " +
                    "id AS codice_cliente, " +
                    "1 AS attivo, " +
                    "ragione_sociale, indirizzo, cap, citta, telefono, piva, cf, email, note " +
                    "FROM " + backupName + " ORDER BY id;"
                );
            }
        }
    }

    private static boolean tableExists(Connection c, String tableName) throws SQLException {
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet rs = md.getTables(null, null, tableName, new String[] { "TABLE" })) {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection c, String tableName, String columnName) throws SQLException {
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet rs = md.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }
}