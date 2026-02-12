package it.jeansandfriends.gestionale.ui.destinazioni;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
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

import it.jeansandfriends.gestionale.dao.DestinazioneClienteDao;
import it.jeansandfriends.gestionale.dao.DuplicateKeyException;
import it.jeansandfriends.gestionale.model.DestinazioneCliente;

public class DestinazioniDialog extends JDialog {

    private final long clienteId;

    private final DestinazioneClienteDao dao = new DestinazioneClienteDao();
    private final DestinazioniTableModel model = new DestinazioniTableModel();
    private final JTable table = new JTable(model);

    private final TableRowSorter<DestinazioniTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField(24);

    public DestinazioniDialog(JFrame owner, long clienteId, String clienteLabel) {
        super(owner, "Destinazioni - " + clienteLabel, true);
        this.clienteId = clienteId;

        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnNew = new JButton("Nuova");
        btnNew.addActionListener(e -> onNew(owner));

        JButton btnEdit = new JButton("Modifica");
        btnEdit.addActionListener(e -> onEdit(owner));

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
        List<DestinazioneCliente> all = dao.findByClienteId(clienteId);
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

        sorter.setRowFilter(new RowFilter<DestinazioniTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DestinazioniTableModel, ? extends Integer> entry) {
                // prog, rag soc, città, email, telefono
                String prog = safe(entry.getValue(0));
                String rag = safe(entry.getValue(1));
                String cit = safe(entry.getValue(2));
                String email = safe(entry.getValue(5));
                String tel = safe(entry.getValue(6));
                return prog.contains(needle) || rag.contains(needle) || cit.contains(needle) || email.contains(needle) || tel.contains(needle);
            }
            private String safe(Object v) { return v == null ? "" : v.toString().toLowerCase(); }
        });
    }

    private void onNew(JFrame owner) {
        int nextProg = dao.getNextProgressivo(clienteId);

        DestinazioneEditDialog dlg = new DestinazioneEditDialog(owner, "Nuova Destinazione", null, clienteId, nextProg);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.insert(dlg.getDestinazione());
                refresh();
            } catch (DuplicateKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Progressivo duplicato", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit(JFrame owner) {
        DestinazioneCliente selected = getSelected();
        if (selected == null) return;

        DestinazioneEditDialog dlg = new DestinazioneEditDialog(owner, "Modifica Destinazione", selected, clienteId, 0);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            try {
                dao.update(dlg.getDestinazione());
                refresh();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDelete() {
        DestinazioneCliente selected = getSelected();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare la destinazione " + String.format("%04d", selected.getProgressivo()) + "?",
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

    private DestinazioneCliente getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una destinazione.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getRowAt(modelRow);
    }
}