package it.jeansandfriends.gestionale.ui.pagamenti;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.dao.PagamentoDao;
import it.jeansandfriends.gestionale.dao.TipoPagamentoDao;
import it.jeansandfriends.gestionale.model.Pagamento;
import it.jeansandfriends.gestionale.model.TipoPagamento;
import it.jeansandfriends.gestionale.ui.components.LookupField;

public class PagamentoEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(20);
    private final JTextField txtDescrizione = new JTextField(30);

    private final LookupField<TipoPagamento> lfTipo = new LookupField<>(t ->
            safe(t.getCodice()) + " - " + safe(t.getDescrizione())
    );

    private final JTextField txtNrRate = new JTextField(10);
    private final JTextField txtDistanza = new JTextField(10);
    private final JTextField txtGiorniPrima = new JTextField(10);

    private boolean confirmed = false;
    private final Pagamento pagamento;

    public PagamentoEditDialog(JFrame owner, String title, Pagamento toEditOrNull) {
        super(owner, title, true);
        this.pagamento = (toEditOrNull != null) ? clonePagamento(toEditOrNull) : new Pagamento();

        setSize(680, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Codice: progressivo e NON modificabile
        txtCodice.setEditable(false);
        if (toEditOrNull == null) {
            int next = new PagamentoDao().getNextCodice();
            pagamento.setCodice(String.valueOf(next)); // progressivo semplice
        }

        // Bind lookup Tipo Pagamento
        lfTipo.bindSelector(owner,
                "Seleziona Tipo di Pagamento",
                v -> new TipoPagamentoDao().findAll(),
                new String[] { "Codice", "Descrizione" },
                new Function[] {
                        (Function<TipoPagamento, Object>) t -> t.getCodice(),
                        (Function<TipoPagamento, Object>) t -> t.getDescrizione()
                });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int r = 0;
        addRow(form, gbc, r++, "Codice*", txtCodice);
        addRow(form, gbc, r++, "Descrizione", txtDescrizione);
        addRow(form, gbc, r++, "Tipo di Pagamento*", lfTipo);
        addRow(form, gbc, r++, "Nr Rate (default 1)", txtNrRate);
        addRow(form, gbc, r++, "Distanza tra rate (gg)", txtDistanza);
        addRow(form, gbc, r++, "Giorni prima rata", txtGiorniPrima);

        add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("Salva");
        btnOk.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> { confirmed = false; dispose(); });

        JPanel buttons = new JPanel();
        buttons.add(btnOk);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // Prefill
        txtCodice.setText(nvl(pagamento.getCodice()));
        txtDescrizione.setText(nvl(pagamento.getDescrizione()));

        txtNrRate.setText(pagamento.getNrRate() == null ? "" : String.valueOf(pagamento.getNrRate()));
        txtDistanza.setText(pagamento.getDistanzaRateGiorni() == null ? "" : String.valueOf(pagamento.getDistanzaRateGiorni()));
        txtGiorniPrima.setText(pagamento.getGiorniPrimaRata() == null ? "" : String.valueOf(pagamento.getGiorniPrimaRata()));

        // Prefill tipo: se possibile usa findById (più pulito), altrimenti fallback su findAll
        if (pagamento.getTipoPagamentoId() != null) {
            TipoPagamento selected = findTipoById(pagamento.getTipoPagamentoId());
            lfTipo.setValue(selected);
        } else {
            lfTipo.setValue(null);
        }
    }

    private TipoPagamento findTipoById(Long id) {
        if (id == null) return null;

        // Se hai aggiunto findById nel dao, usa quello:
        try {
            return new TipoPagamentoDao().findById(id);
        } catch (Throwable ignore) {
            // fallback compatibilità: cerca in findAll
        }

        List<TipoPagamento> tipi = new TipoPagamentoDao().findAll();
        for (TipoPagamento t : tipi) {
            if (t != null && t.getId() != null && t.getId().equals(id)) return t;
        }
        return null;
    }

    private void onSave() {
        String codice = txtCodice.getText().trim();
        if (codice.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il campo Codice è obbligatorio.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TipoPagamento tipo = lfTipo.getValue();
        if (tipo == null || tipo.getId() == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un Tipo di Pagamento.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // codice readonly
        pagamento.setCodice(codice);
        pagamento.setDescrizione(emptyToNull(txtDescrizione.getText()));
        pagamento.setTipoPagamentoId(tipo.getId());

        pagamento.setNrRate(parseIntOrNull(txtNrRate.getText()));
        pagamento.setDistanzaRateGiorni(parseIntOrNull(txtDistanza.getText()));
        pagamento.setGiorniPrimaRata(parseIntOrNull(txtGiorniPrima.getText()));

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Pagamento getPagamento() { return pagamento; }

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

    private static Pagamento clonePagamento(Pagamento src) {
        Pagamento p = new Pagamento();
        p.setId(src.getId());
        p.setCodice(src.getCodice());
        p.setDescrizione(src.getDescrizione());
        p.setTipoPagamentoId(src.getTipoPagamentoId());
        p.setNrRate(src.getNrRate());
        p.setDistanzaRateGiorni(src.getDistanzaRateGiorni());
        p.setGiorniPrimaRata(src.getGiorniPrimaRata());

        p.setTipoPagamentoCodice(src.getTipoPagamentoCodice());
        p.setTipoPagamentoDescrizione(src.getTipoPagamentoDescrizione());
        return p;
    }

    private static String nvl(String s) { return s == null ? "" : s; }
    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try { return Integer.parseInt(t); } catch (NumberFormatException ex) { return null; }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}