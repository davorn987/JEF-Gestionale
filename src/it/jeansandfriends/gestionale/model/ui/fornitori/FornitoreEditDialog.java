package it.jeansandfriends.gestionale.ui.fornitori;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.dao.IvaDao;
import it.jeansandfriends.gestionale.dao.NazioneDao;
import it.jeansandfriends.gestionale.dao.PagamentoDao;
import it.jeansandfriends.gestionale.dao.ProvinciaDao;
import it.jeansandfriends.gestionale.model.Fornitore;
import it.jeansandfriends.gestionale.model.Iva;
import it.jeansandfriends.gestionale.model.Nazione;
import it.jeansandfriends.gestionale.model.Pagamento;
import it.jeansandfriends.gestionale.model.Provincia;
import it.jeansandfriends.gestionale.ui.components.LookupField;

public class FornitoreEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(10); // read-only
    private final JCheckBox chkAttivo = new JCheckBox("Attivo");

    private final JTextField txtRagioneSociale = new JTextField(30);
    private final JTextField txtIndirizzo = new JTextField(30);
    private final JTextField txtCap = new JTextField(10);
    private final JTextField txtCitta = new JTextField(20);

    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtCellulare = new JTextField(20);
    private final JTextField txtPiva = new JTextField(20);
    private final JTextField txtCf = new JTextField(20);

    private final JTextField txtEmail = new JTextField(25);

    // LookupFields
    private final LookupField<Provincia> lfProvincia = new LookupField<>(p ->
            safe(p.getSigla()) + " - " + safe(p.getNome())
    );

    private final LookupField<Nazione> lfNazione = new LookupField<>(n ->
            safe(n.getCodiceIso2()) + " - " + safe(n.getDescrizioneIt())
    );

    private final LookupField<Iva> lfIva = new LookupField<>(i -> {
        String cod = safe(i.getCodice());
        String desc = safe(i.getDescrizione());
        String pct = String.valueOf(i.getPercentuale());
        if (desc.isEmpty()) return cod + " (" + pct + "%)";
        return cod + " - " + desc + " (" + pct + "%)";
    });

    private final LookupField<Pagamento> lfPagamento = new LookupField<>(p -> {
        String cod = safe(p.getCodice());
        String desc = safe(p.getDescrizione());
        String tipo = safe(p.getTipoPagamentoCodice());
        String base = cod + (desc.isEmpty() ? "" : (" - " + desc));
        if (!tipo.isEmpty()) base = base + " [" + tipo + "]";
        return base;
    });

    private boolean confirmed = false;
    private final Fornitore fornitore;

    // ID selezionati
    private Long provinciaId = null;
    private Long nazioneId = null;
    private Long ivaId = null;
    private Long pagamentoId = null;

    public FornitoreEditDialog(JFrame owner, String title, Fornitore fornitoreToEditOrNull, int codiceFornitoreForNew) {
        super(owner, title, true);

        if (fornitoreToEditOrNull != null) {
            this.fornitore = cloneFornitore(fornitoreToEditOrNull);
        } else {
            this.fornitore = new Fornitore();
            this.fornitore.setCodiceFornitore(codiceFornitoreForNew);
            this.fornitore.setAttivo(true);
        }

        setSize(980, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        txtCodice.setEditable(false);

        bindLookups(owner);

        // UI
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int r = 0;
        addRow(form, gbc, r++, "Codice Fornitore", txtCodice, chkAttivo);

        addRow(form, gbc, r++, "Ragione Sociale*", txtRagioneSociale);
        addRow(form, gbc, r++, "Indirizzo", txtIndirizzo);
        addRow(form, gbc, r++, "CAP", txtCap);
        addRow(form, gbc, r++, "Città", txtCitta);

        addRow(form, gbc, r++, "Provincia (opz.)", lfProvincia);
        addRow(form, gbc, r++, "Nazione (opz.)", lfNazione);

        addRow(form, gbc, r++, "Telefono", txtTelefono);
        addRow(form, gbc, r++, "Cellulare", txtCellulare);
        addRow(form, gbc, r++, "P. IVA", txtPiva);
        addRow(form, gbc, r++, "Codice Fiscale", txtCf);

        addRow(form, gbc, r++, "IVA predefinita", lfIva);
        addRow(form, gbc, r++, "Pagamento predefinito", lfPagamento);

        addRow(form, gbc, r++, "Email", txtEmail);

        add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("Salva");
        btnOk.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> { confirmed = false; dispose(); });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnOk);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // Prefill campi testuali
        txtCodice.setText(String.format("%06d", fornitore.getCodiceFornitore()));
        chkAttivo.setSelected(fornitore.isAttivo());

        txtRagioneSociale.setText(nvl(fornitore.getRagioneSociale()));
        txtIndirizzo.setText(nvl(fornitore.getIndirizzo()));
        txtCap.setText(nvl(fornitore.getCap()));
        txtCitta.setText(nvl(fornitore.getCitta()));

        txtTelefono.setText(nvl(fornitore.getTelefono()));
        txtCellulare.setText(nvl(fornitore.getCellulare()));
        txtPiva.setText(nvl(fornitore.getPiva()));
        txtCf.setText(nvl(fornitore.getCf()));
        txtEmail.setText(nvl(fornitore.getEmail()));

        // Prefill lookup da id (pulendo se inattivo/non trovato)
        provinciaId = fornitore.getProvinciaId();
        nazioneId = fornitore.getNazioneId();
        ivaId = fornitore.getIvaId();
        pagamentoId = fornitore.getPagamentoId();

        Provincia prov = (provinciaId == null) ? null : new ProvinciaDao().findById(provinciaId);
        lfProvincia.setValue(prov);
        if (prov == null) provinciaId = null;

        Nazione naz = (nazioneId == null) ? null : new NazioneDao().findById(nazioneId);
        lfNazione.setValue(naz);
        if (naz == null) nazioneId = null;

        Iva iva = (ivaId == null) ? null : new IvaDao().findById(ivaId);
        lfIva.setValue(iva);
        if (iva == null) ivaId = null;

        Pagamento pag = (pagamentoId == null) ? null : new PagamentoDao().findById(pagamentoId);
        lfPagamento.setValue(pag);
        if (pag == null) pagamentoId = null;
    }

    private void bindLookups(JFrame owner) {
        // Provincia: SOLO ATTIVE
        lfProvincia.bindSelector(owner,
                "Seleziona Provincia",
                v -> new ProvinciaDao().findAll(false),
                new String[] { "Sigla", "Nome" },
                new Function[] {
                        (Function<Provincia, Object>) p -> p.getSigla(),
                        (Function<Provincia, Object>) p -> p.getNome()
                });

        // Nazione: SOLO ATTIVE
        lfNazione.bindSelector(owner,
                "Seleziona Nazione",
                v -> new NazioneDao().findAll(false),
                new String[] { "ISO2", "Descrizione" },
                new Function[] {
                        (Function<Nazione, Object>) n -> n.getCodiceIso2(),
                        (Function<Nazione, Object>) n -> n.getDescrizioneIt()
                });

        // IVA (tutte)
        lfIva.bindSelector(owner,
                "Seleziona IVA",
                v -> new IvaDao().findAll(),
                new String[] { "Codice", "Descrizione", "%" },
                new Function[] {
                        (Function<Iva, Object>) i -> i.getCodice(),
                        (Function<Iva, Object>) i -> i.getDescrizione(),
                        (Function<Iva, Object>) i -> i.getPercentuale()
                });

        // Pagamento (tutti)
        lfPagamento.bindSelector(owner,
                "Seleziona Pagamento",
                v -> new PagamentoDao().findAll(),
                new String[] { "Codice", "Descrizione", "Tipo" },
                new Function[] {
                        (Function<Pagamento, Object>) p -> p.getCodice(),
                        (Function<Pagamento, Object>) p -> p.getDescrizione(),
                        (Function<Pagamento, Object>) p -> p.getTipoPagamentoCodice()
                });
    }

    private void onSave() {
        String ragSoc = txtRagioneSociale.getText().trim();
        if (ragSoc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ragione Sociale è obbligatoria.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Provincia prov = lfProvincia.getValue();
        provinciaId = (prov == null ? null : prov.getId());

        Nazione naz = lfNazione.getValue();
        nazioneId = (naz == null ? null : naz.getId());

        Iva iva = lfIva.getValue();
        ivaId = (iva == null ? null : iva.getId());

        Pagamento pag = lfPagamento.getValue();
        pagamentoId = (pag == null ? null : pag.getId());

        fornitore.setAttivo(chkAttivo.isSelected());
        fornitore.setRagioneSociale(ragSoc);

        fornitore.setIndirizzo(emptyToNull(txtIndirizzo.getText()));
        fornitore.setCap(emptyToNull(txtCap.getText()));
        fornitore.setCitta(emptyToNull(txtCitta.getText()));

        fornitore.setProvinciaId(provinciaId);
        fornitore.setNazioneId(nazioneId);

        fornitore.setTelefono(emptyToNull(txtTelefono.getText()));
        fornitore.setCellulare(emptyToNull(txtCellulare.getText()));
        fornitore.setPiva(emptyToNull(txtPiva.getText()));
        fornitore.setCf(emptyToNull(txtCf.getText()));

        fornitore.setIvaId(ivaId);
        fornitore.setPagamentoId(pagamentoId);

        fornitore.setEmail(emptyToNull(txtEmail.getText()));

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Fornitore getFornitore() { return fornitore; }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field, JCheckBox optionalCheck) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);

        if (optionalCheck != null) {
            gbc.gridx = 2; gbc.weightx = 0;
            panel.add(optionalCheck, gbc);
        }
    }

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

    private static String safe(String s) { return s == null ? "" : s; }
    private static String nvl(String s) { return s == null ? "" : s; }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Fornitore cloneFornitore(Fornitore src) {
        Fornitore f = new Fornitore();
        f.setId(src.getId());
        f.setCodiceFornitore(src.getCodiceFornitore());
        f.setAttivo(src.isAttivo());

        f.setRagioneSociale(src.getRagioneSociale());
        f.setIndirizzo(src.getIndirizzo());
        f.setCap(src.getCap());
        f.setCitta(src.getCitta());

        f.setProvinciaId(src.getProvinciaId());
        f.setNazioneId(src.getNazioneId());

        f.setTelefono(src.getTelefono());
        f.setCellulare(src.getCellulare());
        f.setPiva(src.getPiva());
        f.setCf(src.getCf());

        f.setIvaId(src.getIvaId());
        f.setPagamentoId(src.getPagamentoId());

        f.setEmail(src.getEmail());
        f.setNote(src.getNote());
        return f;
    }
}
