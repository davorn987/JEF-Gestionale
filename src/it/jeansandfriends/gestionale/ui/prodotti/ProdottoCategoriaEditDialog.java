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

import it.jeansandfriends.gestionale.model.ProdottoCategoria;

public class ProdottoCategoriaEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(10);
    private final JTextField txtDescrizione = new JTextField(30);
    private final JCheckBox chkAttivo = new JCheckBox("Attivo");

    private boolean confirmed = false;
    private final ProdottoCategoria categoria;

    public ProdottoCategoriaEditDialog(JFrame owner, String title, ProdottoCategoria categoriaToEditOrNull) {
        super(owner, title, true);

        if (categoriaToEditOrNull != null) {
            this.categoria = cloneProdottoCategoria(categoriaToEditOrNull);
        } else {
            this.categoria = new ProdottoCategoria();
            this.categoria.setAttivo(true);
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
        txtCodice.setText(categoria.getCodice());
        txtDescrizione.setText(categoria.getDescrizione());
        chkAttivo.setSelected(categoria.isAttivo());
    }

    private void onOk() {
        String codice = txtCodice.getText();
        if (codice == null || codice.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il codice è obbligatorio.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        categoria.setCodice(codice.trim());
        categoria.setDescrizione(txtDescrizione.getText());
        categoria.setAttivo(chkAttivo.isSelected());

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public ProdottoCategoria getProdottoCategoria() { return categoria; }

    private static ProdottoCategoria cloneProdottoCategoria(ProdottoCategoria pc) {
        ProdottoCategoria clone = new ProdottoCategoria();
        clone.setId(pc.getId());
        clone.setCodice(pc.getCodice());
        clone.setDescrizione(pc.getDescrizione());
        clone.setAttivo(pc.isAttivo());
        return clone;
    }
}
