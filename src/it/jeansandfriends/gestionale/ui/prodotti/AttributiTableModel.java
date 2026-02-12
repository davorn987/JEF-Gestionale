package it.jeansandfriends.gestionale.ui.prodotti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Attributo;

public class AttributiTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Categoria ID", "Codice", "Descrizione", "Attivo"
    };

    private List<Attributo> rows = new ArrayList<>();

    public void setRows(List<Attributo> rows) {
        this.rows = rows != null ? rows : new ArrayList<Attributo>();
        fireTableDataChanged();
    }

    public Attributo getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) return Boolean.class; // Attivo
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Attributo a = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return a.getCategoriaId();
            case 1: return a.getCodice();
            case 2: return a.getDescrizione();
            case 3: return a.isAttivo();
            default: return null;
        }
    }
}
