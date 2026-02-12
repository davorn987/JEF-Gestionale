package it.jeansandfriends.gestionale.ui.destinazioni;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import it.jeansandfriends.gestionale.model.DestinazioneCliente;

public class DestinazioniTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Prog.", "Ragione Sociale", "Città", "Provincia", "Nazione", "Email", "Telefono"
    };

    private List<DestinazioneCliente> rows = new ArrayList<>();

    public void setRows(List<DestinazioneCliente> rows) {
        this.rows = rows != null ? rows : new ArrayList<DestinazioneCliente>();
        fireTableDataChanged();
    }

    public DestinazioneCliente getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DestinazioneCliente d = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return String.format("%04d", d.getProgressivo());
            case 1: return d.getRagioneSociale();
            case 2: return d.getCitta();
            case 3: return d.getProvincia();
            case 4: return d.getNazione();
            case 5: return d.getEmail();
            case 6: return d.getTelefono();
            default: return null;
        }
    }
}