package it.jeansandfriends.gestionale.ui.prodotti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.AttributoCategoria;

public class AttributoCategoriaTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Codice", "Descrizione", "Attivo"
    };

    private List<AttributoCategoria> rows = new ArrayList<>();

    public void setRows(List<AttributoCategoria> rows) {
        this.rows = rows != null ? rows : new ArrayList<AttributoCategoria>();
        fireTableDataChanged();
    }

    public AttributoCategoria getRowAt(int modelRow) {
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
        AttributoCategoria ac = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return ac.getCodice();
            case 1: return ac.getDescrizione();
            case 2: return ac.isAttivo();
            default: return null;
        }
    }
}
