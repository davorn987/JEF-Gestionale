package it.jeansandfriends.gestionale.ui.destinazioni;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.dao.NazioneDao;
import it.jeansandfriends.gestionale.dao.ProvinciaDao;
import it.jeansandfriends.gestionale.model.DestinazioneCliente;
import it.jeansandfriends.gestionale.model.Nazione;
import it.jeansandfriends.gestionale.model.Provincia;
import it.jeansandfriends.gestionale.ui.components.LookupField;

public class DestinazioneEditDialog extends JDialog {

    private final JTextField txtProg = new JTextField(10); // read-only
    private final JTextField txtRagSoc = new JTextField(30);
    private final JTextField txtIndirizzo = new JTextField(30);
    private final JTextField txtCap = new JTextField(10);
    private final JTextField txtCitta = new JTextField(20);

    // Prima erano JTextField liberi
    private final LookupField<Provincia> lfProvincia = new LookupField<>(p ->
            safe(p.getSigla()) + " - " + safe(p.getNome())
    );
    private final LookupField<Nazione> lfNazione = new LookupField<>(n ->
            safe(n.getCodiceIso2()) + " - " + safe(n.getDescrizioneIt())
    );

    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtCellulare = new JTextField(20);
    private final JTextField txtEmail = new JTextField(25);

    private boolean confirmed = false;
    private final DestinazioneCliente dest;

    public DestinazioneEditDialog(JFrame owner, String title, DestinazioneCliente toEditOrNull, long clienteId, int progressivoForNew) {
        super(owner, title, true);

        if (toEditOrNull != null) {
            this.dest = cloneDest(toEditOrNull);
        } else {
            this.dest = new DestinazioneCliente();
            this.dest.setClienteId(clienteId);
            this.dest.setProgressivo(progressivoForNew);
        }

        setSize(860, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        txtProg.setEditable(false);

        // Lookup: SOLO ATTIVI
        lfProvincia.bindSelector(owner,
                "Seleziona Provincia",
                v -> new ProvinciaDao().findAll(false),
                new String[] { "Sigla", "Nome" },
                new Function[] {
                        (Function<Provincia, Object>) p -> p.getSigla(),
                        (Function<Provincia, Object>) p -> p.getNome()
                });

        lfNazione.bindSelector(owner,
                "Seleziona Nazione",
                v -> new NazioneDao().findAll(false),
                new String[] { "ISO2", "Descrizione" },
                new Function[] {
                        (Function<Nazione, Object>) n -> n.getCodiceIso2(),
                        (Function<Nazione, Object>) n -> n.getDescrizioneIt()
                });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int r = 0;
        addRow(form, gbc, r++, "Progressivo", txtProg);
        addRow(form, gbc, r++, "Ragione Sociale Dest.", txtRagSoc);
        addRow(form, gbc, r++, "Indirizzo", txtIndirizzo);
        addRow(form, gbc, r++, "CAP", txtCap);
        addRow(form, gbc, r++, "Città", txtCitta);
        addRow(form, gbc, r++, "Provincia", lfProvincia);
        addRow(form, gbc, r++, "Nazione", lfNazione);
        addRow(form, gbc, r++, "Telefono", txtTelefono);
        addRow(form, gbc, r++, "Cellulare", txtCellulare);
        addRow(form, gbc, r++, "Email", txtEmail);

        add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("Salva");
        btnOk.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> { confirmed = false; dispose(); });

        JPanel buttons = new JPanel();
        buttons.add(btnOk);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // prefill
        txtProg.setText(String.format("%04d", dest.getProgressivo()));
        txtRagSoc.setText(nvl(dest.getRagioneSociale()));
        txtIndirizzo.setText(nvl(dest.getIndirizzo()));
        txtCap.setText(nvl(dest.getCap()));
        txtCitta.setText(nvl(dest.getCitta()));

        // Destinazioni oggi salvano provincia/nazione come TEXT.
        // Noi le impostiamo come SIGLA e ISO2 (scelti da lookup), così restano consistenti.
        lfProvincia.setValue(findProvinciaBySigla(dest.getProvincia()));
        lfNazione.setValue(findNazioneByIso2(dest.getNazione()));

        txtTelefono.setText(nvl(dest.getTelefono()));
        txtCellulare.setText(nvl(dest.getCellulare()));
        txtEmail.setText(nvl(dest.getEmail()));
    }

    private Provincia findProvinciaBySigla(String sigla) {
        String s = sigla == null ? "" : sigla.trim();
        if (s.isEmpty()) return null;
        for (Provincia p : new ProvinciaDao().findAll(false)) {
            if (p != null && p.getSigla() != null && p.getSigla().equalsIgnoreCase(s)) return p;
        }
        return null;
    }

    private Nazione findNazioneByIso2(String iso2) {
        String s = iso2 == null ? "" : iso2.trim();
        if (s.isEmpty()) return null;
        for (Nazione n : new NazioneDao().findAll(false)) {
            if (n != null && n.getCodiceIso2() != null && n.getCodiceIso2().equalsIgnoreCase(s)) return n;
        }
        return null;
    }

    private void onSave() {
        dest.setRagioneSociale(emptyToNull(txtRagSoc.getText()));
        dest.setIndirizzo(emptyToNull(txtIndirizzo.getText()));
        dest.setCap(emptyToNull(txtCap.getText()));
        dest.setCitta(emptyToNull(txtCitta.getText()));

        // Persistiamo come testo standardizzato:
        // - provincia = SIGLA (es "MI")
        // - nazione = ISO2 (es "IT")
        Provincia prov = lfProvincia.getValue();
        dest.setProvincia(prov == null ? null : emptyToNull(prov.getSigla()));

        Nazione naz = lfNazione.getValue();
        dest.setNazione(naz == null ? null : emptyToNull(naz.getCodiceIso2()));

        dest.setTelefono(emptyToNull(txtTelefono.getText()));
        dest.setCellulare(emptyToNull(txtCellulare.getText()));
        dest.setEmail(emptyToNull(txtEmail.getText()));

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public DestinazioneCliente getDestinazione() { return dest; }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JPanel customField) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(customField, gbc);
    }

    private static DestinazioneCliente cloneDest(DestinazioneCliente s) {
        DestinazioneCliente d = new DestinazioneCliente();
        d.setId(s.getId());
        d.setClienteId(s.getClienteId());
        d.setProgressivo(s.getProgressivo());
        d.setRagioneSociale(s.getRagioneSociale());
        d.setIndirizzo(s.getIndirizzo());
        d.setCap(s.getCap());
        d.setCitta(s.getCitta());
        d.setProvincia(s.getProvincia());
        d.setNazione(s.getNazione());
        d.setTelefono(s.getTelefono());
        d.setCellulare(s.getCellulare());
        d.setEmail(s.getEmail());
        return d;
    }

    private static String nvl(String s) { return s == null ? "" : s; }
    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static String safe(String s) { return s == null ? "" : s; }
}