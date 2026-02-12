package it.jeansandfriends.gestionale.ui.prodotti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.UnitaMisura;

public class UnitaMisuraTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Codice", "Descrizione", "Attivo"
    };

    private List<UnitaMisura> rows = new ArrayList<>();

    public void setRows(List<UnitaMisura> rows) {
        this.rows = rows != null ? rows : new ArrayList<UnitaMisura>();
        fireTableDataChanged();
    }

    public UnitaMisura getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Boolean.class; // Attivo
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        UnitaMisura u = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return u.getCodice();
            case 1: return u.getDescrizione();
            case 2: return u.isAttivo();
            default: return null;
        }
    }
}
