package it.jeansandfriends.gestionale.ui.prodotti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.dao.AttributoCategoriaDao;
import it.jeansandfriends.gestionale.model.Attributo;
import it.jeansandfriends.gestionale.model.AttributoCategoria;

public class AttributoEditDialog extends JDialog {

    private final JComboBox<AttributoCategoria> cmbCategoria = new JComboBox<>();
    private final JTextField txtCodice = new JTextField(10);
    private final JTextField txtDescrizione = new JTextField(30);
    private final JCheckBox chkAttivo = new JCheckBox("Attivo");

    private boolean confirmed = false;
    private final Attributo attributo;

    public AttributoEditDialog(JFrame owner, String title, Attributo attributoToEditOrNull) {
        super(owner, title, true);

        if (attributoToEditOrNull != null) {
            this.attributo = cloneAttributo(attributoToEditOrNull);
        } else {
            this.attributo = new Attributo();
            this.attributo.setAttivo(true);
        }

        setSize(550, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Carica categorie
        loadCategorie();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1;
        form.add(cmbCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Codice:"), gbc);
        gbc.gridx = 1;
        form.add(txtCodice, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Descrizione:"), gbc);
        gbc.gridx = 1;
        form.add(txtDescrizione, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
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

    private void loadCategorie() {
        AttributoCategoriaDao dao = new AttributoCategoriaDao();
        List<AttributoCategoria> categorie = dao.findAll(true);
        for (AttributoCategoria cat : categorie) {
            cmbCategoria.addItem(cat);
        }
    }

    private void loadData() {
        txtCodice.setText(attributo.getCodice());
        txtDescrizione.setText(attributo.getDescrizione());
        chkAttivo.setSelected(attributo.isAttivo());

        if (attributo.getCategoriaId() != null) {
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                AttributoCategoria cat = cmbCategoria.getItemAt(i);
                if (cat.getId().equals(attributo.getCategoriaId())) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void onOk() {
        AttributoCategoria selectedCategoria = (AttributoCategoria) cmbCategoria.getSelectedItem();
        if (selectedCategoria == null) {
            JOptionPane.showMessageDialog(this, "Selezionare una categoria.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String codice = txtCodice.getText();
        if (codice == null || codice.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il codice è obbligatorio.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        attributo.setCategoriaId(selectedCategoria.getId());
        attributo.setCodice(codice.trim());
        attributo.setDescrizione(txtDescrizione.getText());
        attributo.setAttivo(chkAttivo.isSelected());

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Attributo getAttributo() { return attributo; }

    private static Attributo cloneAttributo(Attributo a) {
        Attributo clone = new Attributo();
        clone.setId(a.getId());
        clone.setCategoriaId(a.getCategoriaId());
        clone.setCodice(a.getCodice());
        clone.setDescrizione(a.getDescrizione());
        clone.setAttivo(a.isAttivo());
        return clone;
    }
}
