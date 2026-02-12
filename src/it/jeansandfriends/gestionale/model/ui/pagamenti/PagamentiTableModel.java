package it.jeansandfriends.gestionale.ui.pagamenti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Pagamento;

public class PagamentiTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Codice", "Descrizione", "Tipo", "Nr Rate", "Distanza Rate (gg)", "Giorni Prima Rata"
    };

    private List<Pagamento> rows = new ArrayList<>();

    public void setRows(List<Pagamento> rows) {
        this.rows = rows != null ? rows : new ArrayList<Pagamento>();
        fireTableDataChanged();
    }

    public Pagamento getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pagamento p = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return p.getCodice();
            case 1: return p.getDescrizione();
            case 2: return p.getTipoPagamentoCodice(); // << ora codice (BON/CON/...)
            case 3: return p.getNrRate();
            case 4: return p.getDistanzaRateGiorni();
            case 5: return p.getGiorniPrimaRata();
            default: return null;
        }
    }
}