package it.jeansandfriends.gestionale.ui.iva;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Iva;

public class IvaTableModel extends AbstractTableModel {

    private final String[] columns = { "Codice", "Descrizione", "Percentuale" };
    private List<Iva> rows = new ArrayList<>();

    public void setRows(List<Iva> rows) {
        this.rows = rows != null ? rows : new ArrayList<Iva>();
        fireTableDataChanged();
    }

    public Iva getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Iva i = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return i.getCodice();
            case 1: return i.getDescrizione();
            case 2: return i.getPercentuale();
            default: return null;
        }
    }
}