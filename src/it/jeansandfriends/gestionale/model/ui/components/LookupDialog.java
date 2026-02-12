package it.jeansandfriends.gestionale.ui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

public class LookupDialog<T> extends JDialog {

    private final List<T> items;
    private final LookupTableModel model;
    private final JTable table;
    private final TableRowSorter<LookupTableModel> sorter;

    private final JTextField txtFilter = new JTextField(28);

    private boolean confirmed = false;
    private boolean cleared = false;
    private T selectedItem = null;

    public LookupDialog(
            JFrame owner,
            String title,
            List<T> items,
            String[] columns,
            Function<T, Object>[] getters
    ) {
        super(owner, title, true);

        this.items = (items != null) ? items : new ArrayList<T>();

        this.model = new LookupTableModel(columns, getters);
        this.table = new JTable(model);
        this.sorter = new TableRowSorter<>(model);

        setSize(900, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // top: filtro
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Filtro:"));
        top.add(txtFilter);
        add(top, BorderLayout.NORTH);

        // table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // buttons
        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> onOk());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> { confirmed = false; cleared = false; dispose(); });

        JButton btnClear = new JButton("Svuota");
        btnClear.addActionListener(e -> onClear());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnClear);
        bottom.add(btnOk);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        // filtro live
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // double click = OK
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onOk();
                }
            }
        });

        // load
        model.setRows(this.items);
        applyFilter();
    }

    private void applyFilter() {
        String q = txtFilter.getText();
        if (q == null || q.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        final String needle = q.trim().toLowerCase();

        sorter.setRowFilter(new RowFilter<LookupTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends LookupTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object v = entry.getValue(i);
                    if (v != null && v.toString().toLowerCase().contains(needle)) return true;
                }
                return false;
            }
        });
    }

    private void onOk() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un record.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        selectedItem = model.getRowAt(modelRow);

        confirmed = true;
        cleared = false;
        dispose();
    }

    private void onClear() {
        selectedItem = null;
        confirmed = true;
        cleared = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public boolean isCleared() { return cleared; }

    public T getSelectedItem() { return selectedItem; }

    private class LookupTableModel extends AbstractTableModel {
        private final String[] columns;
        private final Function<T, Object>[] getters;
        private List<T> rows = new ArrayList<>();

        LookupTableModel(String[] columns, Function<T, Object>[] getters) {
            this.columns = columns;
            this.getters = getters;
        }

        void setRows(List<T> rows) {
            this.rows = rows != null ? rows : new ArrayList<T>();
            fireTableDataChanged();
        }

        T getRowAt(int row) {
            return rows.get(row);
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            T item = rows.get(rowIndex);
            return getters[columnIndex].apply(item);
        }
    }
}