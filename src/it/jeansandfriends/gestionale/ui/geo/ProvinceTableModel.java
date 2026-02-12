package it.jeansandfriends.gestionale.ui.geo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Provincia;

public class ProvinceTableModel extends AbstractTableModel {

    private final String[] columns = { "Attivo", "Sigla", "Nome" };
    private List<Provincia> rows = new ArrayList<>();

    public void setRows(List<Provincia> rows) {
        this.rows = rows != null ? rows : new ArrayList<Provincia>();
        fireTableDataChanged();
    }

    public Provincia getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Provincia p = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return p.isAttivo();
            case 1: return p.getSigla();
            case 2: return p.getNome();
            default: return null;
        }
    }
}