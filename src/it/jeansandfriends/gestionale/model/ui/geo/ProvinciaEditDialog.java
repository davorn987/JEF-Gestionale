package it.jeansandfriends.gestionale.ui.geo;

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

import it.jeansandfriends.gestionale.model.Provincia;

public class ProvinciaEditDialog extends JDialog {

    private final JTextField txtSigla = new JTextField(10);
    private final JTextField txtNome = new JTextField(30);

    private boolean confirmed = false;
    private final Provincia prov;

    public ProvinciaEditDialog(JFrame owner, String title, Provincia toEditOrNull) {
        super(owner, title, true);
        this.prov = (toEditOrNull != null) ? cloneProvincia(toEditOrNull) : new Provincia();

        setSize(520, 230);
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
        form.add(new JLabel("Sigla* (es: MI)"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtSigla, gbc);
        r++;

        gbc.gridx = 0; gbc.gridy = r; gbc.weightx = 0;
        form.add(new JLabel("Nome*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(txtNome, gbc);
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

        txtSigla.setText(nvl(prov.getSigla()));
        txtNome.setText(nvl(prov.getNome()));
    }

    private void onSave() {
        String sigla = txtSigla.getText().trim().toUpperCase();
        String nome = txtNome.getText().trim();

        if (sigla.isEmpty() || nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sigla e Nome sono obbligatori.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sigla.length() != 2) {
            JOptionPane.showMessageDialog(this, "La sigla provincia deve essere di 2 caratteri (es: MI).", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        prov.setSigla(sigla);
        prov.setNome(nome);

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Provincia getProvincia() { return prov; }

    private static Provincia cloneProvincia(Provincia src) {
        return new Provincia(src.getId(), src.getSigla(), src.getNome());
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}