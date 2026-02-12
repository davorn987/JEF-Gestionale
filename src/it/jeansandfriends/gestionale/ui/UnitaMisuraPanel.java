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

import it.jeansandfriends.gestionale.dao.DuplicateKeyException;
import it.jeansandfriends.gestionale.dao.UnitaMisuraDao;
import it.jeansandfriends.gestionale.model.UnitaMisura;
import it.jeansandfriends.gestionale.ui.prodotti.UnitaMisuraEditDialog;
import it.jeansandfriends.gestionale.ui.prodotti.UnitaMisuraTableModel;

public class UnitaMisuraPanel extends JPanel {

    private final UnitaMisuraDao dao = new UnitaMisuraDao();
    private final UnitaMisuraTableModel model = new UnitaMisuraTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<UnitaMisuraTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);
    private final JCheckBox chkIncludeInactive = new JCheckBox("Mostra inattivi");

    public UnitaMisuraPanel() {
        setLayout(new BorderLayout());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnNew = new JButton("Nuova");
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
        List<UnitaMisura> all = dao.findAll(chkIncludeInactive.isSelected());
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

        sorter.setRowFilter(new RowFilter<UnitaMisuraTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends UnitaMisuraTableModel, ? extends Integer> entry) {
                String codice = safe(entry.getValue(0));
                String descrizione = safe(entry.getValue(1));

                return codice.contains(needle) || descrizione.contains(needle);
            }

            private String safe(Object v) {
                return v == null ? "" : v.toString().toLowerCase();
            }
        });
    }

    private void onNew() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        UnitaMisuraEditDialog dlg = new UnitaMisuraEditDialog(owner, "Nuova Unità di Misura", null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getUnitaMisura());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        UnitaMisura selected = getSelected();
        if (selected == null) return;

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        UnitaMisuraEditDialog dlg = new UnitaMisuraEditDialog(owner, "Modifica Unità di Misura", selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getUnitaMisura());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDisable() {
        UnitaMisura selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Oscurare l'unità di misura " + selected.getCodice() + "?",
                "Oscura Unità di Misura",
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
        UnitaMisura selected = getSelected();
        if (selected == null) return;

        try {
            dao.restore(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private UnitaMisura getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un'unità di misura.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}
