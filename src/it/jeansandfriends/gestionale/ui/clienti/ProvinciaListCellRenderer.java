package it.jeansandfriends.gestionale.ui.clienti;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import it.jeansandfriends.gestionale.model.Provincia;

public class ProvinciaListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            setText("");
        } else if (value instanceof Provincia) {
            Provincia p = (Provincia) value;
            setText((p.getSigla() == null ? "" : p.getSigla()) + " - " +
                    (p.getNome() == null ? "" : p.getNome()));
        }
        return this;
    }
}