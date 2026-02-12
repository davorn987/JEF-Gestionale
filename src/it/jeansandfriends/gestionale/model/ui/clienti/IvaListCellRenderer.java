package it.jeansandfriends.gestionale.ui.clienti;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import it.jeansandfriends.gestionale.model.Iva;

public class IvaListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            setText("");
        } else if (value instanceof Iva) {
            Iva i = (Iva) value;
            String cod = i.getCodice() == null ? "" : i.getCodice();
            String desc = i.getDescrizione() == null ? "" : i.getDescrizione();
            setText(cod + " - " + desc + " (" + i.getPercentuale() + "%)");
        }
        return this;
    }
}