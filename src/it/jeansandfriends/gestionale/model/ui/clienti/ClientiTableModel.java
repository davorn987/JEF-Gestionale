package it.jeansandfriends.gestionale.ui.clienti;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.Cliente;

public class ClientiTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Codice", "Attivo", "Ragione Sociale", "Città", "Provincia", "P.IVA", "Email", "Telefono"
    };

    private List<Cliente> rows = new ArrayList<>();

    public void setRows(List<Cliente> rows) {
        this.rows = rows != null ? rows : new ArrayList<Cliente>();
        fireTableDataChanged();
    }

    public Cliente getRowAt(int modelRow) {
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
        Cliente c = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return String.format("%06d", c.getCodiceCliente());
            case 1: return c.isAttivo();
            case 2: return c.getRagioneSociale();
            case 3: return c.getCitta();
            case 4: return c.getProvinciaId();
            case 5: return c.getPiva();
            case 6: return c.getEmail();
            case 7: return c.getTelefono();
            default: return null;
        }
    }
}