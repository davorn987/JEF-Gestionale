package it.jeansandfriends.gestionale.ui.geo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Nazione;

public class NazioniTableModel extends AbstractTableModel {

    private final String[] columns = { "Attivo", "Codice ISO2", "Descrizione (IT)" };
    private List<Nazione> rows = new ArrayList<>();

    public void setRows(List<Nazione> rows) {
        this.rows = rows != null ? rows : new ArrayList<Nazione>();
        fireTableDataChanged();
    }

    public Nazione getRowAt(int modelRow) {
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
        Nazione n = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return n.isAttivo();
            case 1: return n.getCodiceIso2();
            case 2: return n.getDescrizioneIt();
            default: return null;
        }
    }
}