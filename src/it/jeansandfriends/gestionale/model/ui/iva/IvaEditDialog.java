package it.jeansandfriends.gestionale.ui.iva;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.model.Iva;

public class IvaEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(20);
    private final JTextField txtDescrizione = new JTextField(30);
    private final JTextField txtPercentuale = new JTextField(10);

    private boolean confirmed = false;
    private final Iva iva;

    public IvaEditDialog(JFrame owner, String title, Iva ivaToEditOrNull) {
        super(owner, title, true);
        this.iva = (ivaToEditOrNull != null) ? cloneIva(ivaToEditOrNull) : new Iva();

        setSize(520, 240);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int r = 0;

        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0;
        form.add(new JLabel("Codice*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtCodice, gbc);
        r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0;
        form.add(new JLabel("Descrizione"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtDescrizione, gbc);
        r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0;
        form.add(new JLabel("Percentuale IVA*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtPercentuale, gbc);
        r++;

        add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("Salva");
        btnOk.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JPanel buttons = new JPanel();
        buttons.add(btnOk);
        buttons.add(btnCancel);

        add(buttons, BorderLayout.SOUTH);

        // prefill
        txtCodice.setText(nvl(iva.getCodice()));
        txtDescrizione.setText(nvl(iva.getDescrizione()));
        txtPercentuale.setText(iva.getPercentuale() == 0 ? "" : formatPercent(iva.getPercentuale()));
    }

    private void onSave() {
        String codice = txtCodice.getText().trim();
        String descr = txtDescrizione.getText().trim();
        String percRaw = txtPercentuale.getText().trim();

        if (codice.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il campo Codice è obbligatorio.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double perc = parsePercent(percRaw);
        if (perc == null) {
            JOptionPane.showMessageDialog(this, "Percentuale non valida. Esempi: 22 oppure 22,0", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        iva.setCodice(codice);
        iva.setDescrizione(descr.isEmpty() ? null : descr);
        iva.setPercentuale(perc);

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Iva getIva() {
        return iva;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static Iva cloneIva(Iva src) {
        return new Iva(src.getId(), src.getCodice(), src.getDescrizione(), src.getPercentuale());
    }

    private static Double parsePercent(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String normalized = s.trim().replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String formatPercent(double d) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.ITALY);
        DecimalFormat df = new DecimalFormat("0.##", sym);
        return df.format(d);
    }
}