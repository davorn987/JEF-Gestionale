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
import it.jeansandfriends.gestionale.dao.ProvinciaDao;
import it.jeansandfriends.gestionale.model.Provincia;
import it.jeansandfriends.gestionale.ui.geo.ProvinciaEditDialog;
import it.jeansandfriends.gestionale.ui.geo.ProvinceTableModel;

public class ProvincePanel extends JPanel {

    private final ProvinciaDao dao = new ProvinciaDao();
    private final ProvinceTableModel model = new ProvinceTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<ProvinceTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);
    private final JCheckBox chkIncludeInactive = new JCheckBox("Mostra inattivi");

    public ProvincePanel() {
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
        List<Provincia> all = dao.findAll(chkIncludeInactive.isSelected());
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
        sorter.setRowFilter(new RowFilter<ProvinceTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends ProvinceTableModel, ? extends Integer> entry) {
                String sigla = safe(entry.getValue(1));
                String nome = safe(entry.getValue(2));
                return sigla.contains(needle) || nome.contains(needle);
            }
            private String safe(Object v) { return v == null ? "" : v.toString().toLowerCase(); }
        });
    }

    private void onNew() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ProvinciaEditDialog dlg = new ProvinciaEditDialog(owner, "Nuova Provincia", null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                Provincia p = dlg.getProvincia();
                p.setAttivo(true);
                dao.insert(p);
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Sigla duplicata", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        Provincia selected = getSelected();
        if (selected == null) return;

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ProvinciaEditDialog dlg = new ProvinciaEditDialog(owner, "Modifica Provincia", selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                Provincia edited = dlg.getProvincia();
                edited.setAttivo(selected.isAttivo());
                dao.update(edited);
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Sigla duplicata", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDisable() {
        Provincia selected = getSelected();
        if (selected == null) return;

        try {
            dao.softDelete(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRestore() {
        Provincia selected = getSelected();
        if (selected == null) return;

        try {
            dao.restore(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Provincia getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un record.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}