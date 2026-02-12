package it.jeansandfriends.gestionale.ui.pagamenti;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import it.jeansandfriends.gestionale.model.TipoPagamento;

public class TipoPagamentoListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof TipoPagamento) {
            TipoPagamento t = (TipoPagamento) value;
            String cod = t.getCodice() == null ? "" : t.getCodice();
            String desc = t.getDescrizione() == null ? "" : t.getDescrizione();
            setText(cod + " - " + desc);
        }
        return this;
    }
}