package it.jeansandfriends.gestionale.ui.pagamenti;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.dao.TipoPagamentoDao;
import it.jeansandfriends.gestionale.model.TipoPagamento;

public class TipoPagamentoEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(20);
    private final JTextField txtDescrizione = new JTextField(30);

    private boolean confirmed = false;
    private final TipoPagamento tipo;

    public TipoPagamentoEditDialog(JFrame owner, String title, TipoPagamento toEditOrNull) {
        super(owner, title, true);
        this.tipo = (toEditOrNull != null) ? cloneTipo(toEditOrNull) : new TipoPagamento();

        setSize(520, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Codice: non editabile
        txtCodice.setEditable(false);

        // se è nuovo, assegna progressivo
        if (toEditOrNull == null) {
            int next = new TipoPagamentoDao().getNextCodice();
            // puoi formattare con zeri: String.format("%03d", next)
            tipo.setCodice(String.valueOf(next));
        }

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
        form.add(new JLabel("Descrizione*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtDescrizione, gbc);
        r++;

        add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("Salva");
        btnOk.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> { confirmed = false; dispose(); });

        JPanel buttons = new JPanel();
        buttons.add(btnOk);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        txtCodice.setText(nvl(tipo.getCodice()));
        txtDescrizione.setText(nvl(tipo.getDescrizione()));
    }

    private void onSave() {
        String descr = txtDescrizione.getText().trim();

        if (nvl(tipo.getCodice()).trim().isEmpty() || descr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Codice e Descrizione sono obbligatori.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // codice già impostato (readonly)
        tipo.setDescrizione(descr);

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public TipoPagamento getTipo() { return tipo; }

    private static TipoPagamento cloneTipo(TipoPagamento src) {
        return new TipoPagamento(src.getId(), src.getCodice(), src.getDescrizione());
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}