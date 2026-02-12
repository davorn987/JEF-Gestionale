package it.jeansandfriends.gestionale.ui.prodotti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.ProdottoCategoria;

public class ProdottoCategoriaTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Codice", "Descrizione", "Attivo"
    };

    private List<ProdottoCategoria> rows = new ArrayList<>();

    public void setRows(List<ProdottoCategoria> rows) {
        this.rows = rows != null ? rows : new ArrayList<ProdottoCategoria>();
        fireTableDataChanged();
    }

    public ProdottoCategoria getRowAt(int modelRow) {
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
        ProdottoCategoria pc = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return pc.getCodice();
            case 1: return pc.getDescrizione();
            case 2: return pc.isAttivo();
            default: return null;
        }
    }
}
