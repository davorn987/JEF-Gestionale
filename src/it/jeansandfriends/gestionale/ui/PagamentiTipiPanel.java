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
import it.jeansandfriends.gestionale.dao.TipoPagamentoDao;
import it.jeansandfriends.gestionale.model.TipoPagamento;
import it.jeansandfriends.gestionale.ui.pagamenti.TipoPagamentoEditDialog;
import it.jeansandfriends.gestionale.ui.pagamenti.TipoPagamentoTableModel;

public class PagamentiTipiPanel extends JPanel {

    private final TipoPagamentoDao dao = new TipoPagamentoDao();
    private final TipoPagamentoTableModel model = new TipoPagamentoTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<TipoPagamentoTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);

    public PagamentiTipiPanel() {
        setLayout(new BorderLayout());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnNew = new JButton("Nuovo");
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

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        refresh();
    }

    private void refresh() {
        List<TipoPagamento> all = dao.findAll();
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

        sorter.setRowFilter(new RowFilter<TipoPagamentoTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TipoPagamentoTableModel, ? extends Integer> entry) {
                String codice = safe(entry.getValue(0));
                String descr = safe(entry.getValue(1));
                return codice.contains(needle) || descr.contains(needle);
            }
            private String safe(Object v) { return v == null ? "" : v.toString().toLowerCase(); }
        });
    }

    private void onNew() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        TipoPagamentoEditDialog dlg = new TipoPagamentoEditDialog(owner, "Nuovo Tipo Pagamento", null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getTipo());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        TipoPagamento selected = getSelected();
        if (selected == null) return;

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        TipoPagamentoEditDialog dlg = new TipoPagamentoEditDialog(owner, "Modifica Tipo Pagamento", selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getTipo());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Codice duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDelete() {
        TipoPagamento selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare il tipo pagamento selezionato?",
                "Elimina",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            dao.deleteById(selected.getId());
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private TipoPagamento getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un record.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}