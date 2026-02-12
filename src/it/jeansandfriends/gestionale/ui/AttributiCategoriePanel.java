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

import it.jeansandfriends.gestionale.dao.AttributoCategoriaDao;
import it.jeansandfriends.gestionale.dao.DuplicateKeyException;
import it.jeansandfriends.gestionale.model.AttributoCategoria;
import it.jeansandfriends.gestionale.ui.prodotti.AttributoCategoriaEditDialog;
import it.jeansandfriends.gestionale.ui.prodotti.AttributoCategoriaTableModel;

public class AttributiCategoriePanel extends JPanel {

    private final AttributoCategoriaDao dao = new AttributoCategoriaDao();
    private final AttributoCategoriaTableModel model = new AttributoCategoriaTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<AttributoCategoriaTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);
    private final JCheckBox chkIncludeInactive = new JCheckBox("Mostra inattivi");

    public AttributiCategoriePanel() {
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
        List<AttributoCategoria> all = dao.findAll(chkIncludeInactive.isSelected());
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

        sorter.setRowFilter(new RowFilter<AttributoCategoriaTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends AttributoCategoriaTableModel, ? extends Integer> entry) {
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
        AttributoCategoriaEditDialog dlg = new AttributoCategoriaEditDialog(owner, "Nuova Categoria Attributo", null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getAttributoCategoria());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        AttributoCategoria selected = getSelected();
        if (selected == null) return;

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AttributoCategoriaEditDialog dlg = new AttributoCategoriaEditDialog(owner, "Modifica Categoria Attributo", selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getAttributoCategoria());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDisable() {
        AttributoCategoria selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Oscurare la categoria attributo " + selected.getCodice() + "?",
                "Oscura Categoria Attributo",
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
        AttributoCategoria selected = getSelected();
        if (selected == null) return;

        try {
            dao.restore(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private AttributoCategoria getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una categoria attributo.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}
