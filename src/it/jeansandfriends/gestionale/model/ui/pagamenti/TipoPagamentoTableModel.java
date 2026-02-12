package it.jeansandfriends.gestionale.ui.pagamenti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.TipoPagamento;

public class TipoPagamentoTableModel extends AbstractTableModel {

    private final String[] columns = { "Codice", "Descrizione" };
    private List<TipoPagamento> rows = new ArrayList<>();

    public void setRows(List<TipoPagamento> rows) {
        this.rows = rows != null ? rows : new ArrayList<TipoPagamento>();
        fireTableDataChanged();
    }

    public TipoPagamento getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TipoPagamento t = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return t.getCodice();
            case 1: return t.getDescrizione();
            default: return null;
        }
    }
}