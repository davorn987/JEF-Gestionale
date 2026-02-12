package it.jeansandfriends.gestionale.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import it.jeansandfriends.gestionale.dao.AttributoDao;
import it.jeansandfriends.gestionale.dao.DuplicateKeyException;
import it.jeansandfriends.gestionale.model.Attributo;
import it.jeansandfriends.gestionale.ui.prodotti.AttributiTableModel;
import it.jeansandfriends.gestionale.ui.prodotti.AttributoEditDialog;

public class AttributiPanel extends JPanel {

    private final AttributoDao dao = new AttributoDao();
    private final AttributiTableModel model = new AttributiTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<AttributiTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);
    private final JCheckBox chkIncludeInactive = new JCheckBox("Mostra inattivi");

    public AttributiPanel() {
        setLayout(new BorderLayout());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnNew = new JButton("Nuovo");
        btnNew.addActionListener(e -> onNew());

        JButton btnEdit = new JButton("Modifica");
        btnEdit.addActionListener(e -> onEdit());

        JButton btnDisable = new JButton("Oscura");
        btnDisable.addActionListener(e -> onDisable());

        JButton btnRestore = new JButton("Ripristina");
        btnRestore.addActionListener(e -> onRestore());

        JButton btnRefresh = new JButton("Aggiorna");
        btnRefresh.addActionListener(e -> refresh());

        chkIncludeInactive.addActionListener(e -> refresh());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnNew);
        top.add(btnEdit);
        top.add(btnDisable);
        top.add(btnRestore);
        top.add(btnRefresh);

        top.add(new JLabel("Cerca:"));
        top.add(txtSearch);
        top.add(chkIncludeInactive);

        add(top, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        refresh();
    }

    private void refresh() {
        List<Attributo> all = dao.findAll(chkIncludeInactive.isSelected());
        model.setRows(all);
        applyFilter();
    }

    private void applyFilter() {
        String q = txtSearch.getText();
        if (q == null || q.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        final String needle = q.trim().toLowerCase();

        sorter.setRowFilter(new RowFilter<AttributiTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends AttributiTableModel, ? extends Integer> entry) {
                String codice = safe(entry.getValue(1));
                String descrizione = safe(entry.getValue(2));

                return codice.contains(needle) || descrizione.contains(needle);
            }

            private String safe(Object v) {
                return v == null ? "" : v.toString().toLowerCase();
            }
        });
    }

    private void onNew() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AttributoEditDialog dlg = new AttributoEditDialog(owner, "Nuovo Attributo", null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getAttributo());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        Attributo selected = getSelected();
        if (selected == null) return;

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AttributoEditDialog dlg = new AttributoEditDialog(owner, "Modifica Attributo", selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getAttributo());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDisable() {
        Attributo selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Oscurare l'attributo " + selected.getCodice() + "?",
                "Oscura Attributo",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            dao.softDelete(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRestore() {
        Attributo selected = getSelected();
        if (selected == null) return;

        try {
            dao.restore(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Attributo getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un attributo.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}
