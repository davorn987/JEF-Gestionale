package it.jeansandfriends.gestionale.ui.fornitori;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Fornitore;

public class FornitoriTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Codice", "Attivo", "Ragione Sociale", "Città", "Provincia", "P.IVA", "Email", "Telefono"
    };

    private List<Fornitore> rows = new ArrayList<>();

    public void setRows(List<Fornitore> rows) {
        this.rows = rows != null ? rows : new ArrayList<Fornitore>();
        fireTableDataChanged();
    }

    public Fornitore getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 1) return Boolean.class; // Attivo
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Fornitore f = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return String.format("%06d", f.getCodiceFornitore());
            case 1: return f.isAttivo();
            case 2: return f.getRagioneSociale();
            case 3: return f.getCitta();
            case 4: return f.getProvinciaId();
            case 5: return f.getPiva();
            case 6: return f.getEmail();
            case 7: return f.getTelefono();
            default: return null;
        }
    }
}
