package it.jeansandfriends.gestionale.ui.prodotti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.model.UnitaMisura;

public class UnitaMisuraEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(10);
    private final JTextField txtDescrizione = new JTextField(30);
    private final JCheckBox chkAttivo = new JCheckBox("Attivo");

    private boolean confirmed = false;
    private final UnitaMisura unitaMisura;

    public UnitaMisuraEditDialog(JFrame owner, String title, UnitaMisura unitaMisuraToEditOrNull) {
        super(owner, title, true);

        if (unitaMisuraToEditOrNull != null) {
            this.unitaMisura = cloneUnitaMisura(unitaMisuraToEditOrNull);
        } else {
            this.unitaMisura = new UnitaMisura();
            this.unitaMisura.setAttivo(true);
        }

        setSize(500, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Codice:"), gbc);
        gbc.gridx = 1;
        form.add(txtCodice, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Descrizione:"), gbc);
        gbc.gridx = 1;
        form.add(txtDescrizione, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(chkAttivo, gbc);

        add(form, BorderLayout.CENTER);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> onOk());
        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnOk);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        txtCodice.setText(unitaMisura.getCodice());
        txtDescrizione.setText(unitaMisura.getDescrizione());
        chkAttivo.setSelected(unitaMisura.isAttivo());
    }

    private void onOk() {
        String codice = txtCodice.getText();
        if (codice == null || codice.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il codice è obbligatorio.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        unitaMisura.setCodice(codice.trim());
        unitaMisura.setDescrizione(txtDescrizione.getText());
        unitaMisura.setAttivo(chkAttivo.isSelected());

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public UnitaMisura getUnitaMisura() { return unitaMisura; }

    private static UnitaMisura cloneUnitaMisura(UnitaMisura u) {
        UnitaMisura clone = new UnitaMisura();
        clone.setId(u.getId());
        clone.setCodice(u.getCodice());
        clone.setDescrizione(u.getDescrizione());
        clone.setAttivo(u.isAttivo());
        return clone;
    }
}
