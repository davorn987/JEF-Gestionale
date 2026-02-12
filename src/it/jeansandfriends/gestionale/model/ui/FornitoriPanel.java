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

import it.jeansandfriends.gestionale.dao.FornitoreDao;
import it.jeansandfriends.gestionale.model.Fornitore;
import it.jeansandfriends.gestionale.ui.fornitori.FornitoreEditDialog;
import it.jeansandfriends.gestionale.ui.fornitori.FornitoriTableModel;

public class FornitoriPanel extends JPanel {

    private final FornitoreDao dao = new FornitoreDao();
    private final FornitoriTableModel model = new FornitoriTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<FornitoriTableModel> sorter = new TableRowSorter<>(model);

    private final JTextField txtSearch = new JTextField(24);
    private final JCheckBox chkIncludeInactive = new JCheckBox("Mostra inattivi");

    public FornitoriPanel() {
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

        // filtro live
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        refresh();
    }

    private void refresh() {
        List<Fornitore> all = dao.findAll(chkIncludeInactive.isSelected());
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

        sorter.setRowFilter(new RowFilter<FornitoriTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends FornitoriTableModel, ? extends Integer> entry) {
                // filtriamo su: codice, ragione sociale, città, piva, email
                String codice = safe(entry.getValue(0));
                String ragSoc = safe(entry.getValue(2));
                String citta = safe(entry.getValue(3));
                String piva = safe(entry.getValue(5));
                String email = safe(entry.getValue(6));

                return codice.contains(needle) ||
                       ragSoc.contains(needle) ||
                       citta.contains(needle) ||
                       piva.contains(needle) ||
                       email.contains(needle);
            }

            private String safe(Object v) {
                return v == null ? "" : v.toString().toLowerCase();
            }
        });
    }

    private void onNew() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        int nextCode = dao.getNextCodiceFornitore();

        FornitoreEditDialog dlg = new FornitoreEditDialog(owner, "Nuovo Fornitore", null, nextCode);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getFornitore());
                refresh();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        Fornitore selected = getSelected();
        if (selected == null) return;

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        FornitoreEditDialog dlg = new FornitoreEditDialog(owner, "Modifica Fornitore", selected, 0);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getFornitore());
                refresh();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDisable() {
        Fornitore selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Oscurare il fornitore " + String.format("%06d", selected.getCodiceFornitore()) + " ?",
                "Oscura Fornitore",
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
        Fornitore selected = getSelected();
        if (selected == null) return;

        try {
            dao.restore(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Fornitore getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un fornitore.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}
