package it.jeansandfriends.gestionale.ui.clienti;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import it.jeansandfriends.gestionale.model.Nazione;

public class NazioneListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            setText("");
        } else if (value instanceof Nazione) {
            Nazione n = (Nazione) value;
            setText((n.getCodiceIso2() == null ? "" : n.getCodiceIso2()) + " - " +
                    (n.getDescrizioneIt() == null ? "" : n.getDescrizioneIt()));
        }
        return this;
    }
}