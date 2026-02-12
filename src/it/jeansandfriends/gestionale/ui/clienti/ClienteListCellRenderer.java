package it.jeansandfriends.gestionale.ui.clienti;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import it.jeansandfriends.gestionale.model.Cliente;

public class ClienteListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value == null) {
            setText("");
        } else if (value instanceof Cliente) {
            Cliente c = (Cliente) value;
            String cod = String.format("%06d", c.getCodiceCliente());
            String rag = c.getRagioneSociale() == null ? "" : c.getRagioneSociale();
            setText(cod + " - " + rag);
        }
        return this;
    }
}