package it.jeansandfriends.gestionale.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
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
import it.jeansandfriends.gestionale.dao.IvaDao;
import it.jeansandfriends.gestionale.model.Iva;
import it.jeansandfriends.gestionale.ui.iva.IvaEditDialog;
import it.jeansandfriends.gestionale.ui.iva.IvaTableModel;

public class IvaPanel extends JPanel {

    private final IvaDao dao = new IvaDao();
    private final IvaTableModel model = new IvaTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<IvaTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);

    public IvaPanel() {
        setLayout(new BorderLayout());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Top bar: bottoni + ricerca
        JButton btnNew = new JButton("Nuova");
        btnNew.addActionListener(e -> onNew());

        JButton btnEdit = new JButton("Modifica");
        btnEdit.addActionListener(e -> onEdit());

        JButton btnDelete = new JButton("Elimina");
        btnDelete.addActionListener(e -> onDelete());

        JButton btnRefresh = new JButton("Aggiorna");
        btnRefresh.addActionListener(e -> refresh());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnNew);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        top.add(new JLabel("Cerca:"));
        top.add(txtSearch);

        add(top, BorderLayout.NORTH);

        // Filtro live su codice/descrizione
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        refresh(); // carica dati (all'inizio vuoto)
    }

    private void refresh() {
        List<Iva> all = dao.findAll();
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

        sorter.setRowFilter(new RowFilter<IvaTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends IvaTableModel, ? extends Integer> entry) {
                Object codice = entry.getValue(0);      // colonna Codice
                Object descr = entry.getValue(1);       // colonna Descrizione

                String c = codice != null ? codice.toString().toLowerCase() : "";
                String d = descr != null ? descr.toString().toLowerCase() : "";

                return c.contains(needle) || d.contains(needle);
            }
        });
    }

    private void onNew() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        IvaEditDialog dlg = new IvaEditDialog(owner, "Nuova IVA", null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getIva());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una riga da modificare.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Iva selected = model.getRowAt(modelRow);

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        IvaEditDialog dlg = new IvaEditDialog(owner, "Modifica IVA", selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getIva());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una riga da eliminare.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confermi eliminazione?",
                "Elimina IVA",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        int modelRow = table.convertRowIndexToModel(viewRow);
        Iva selected = model.getRowAt(modelRow);

        try {
            dao.deleteById(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}