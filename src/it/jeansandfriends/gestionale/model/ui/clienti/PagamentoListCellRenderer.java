package it.jeansandfriends.gestionale.ui.clienti;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import it.jeansandfriends.gestionale.model.Pagamento;

public class PagamentoListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            setText("");
        } else if (value instanceof Pagamento) {
            Pagamento p = (Pagamento) value;
            String cod = p.getCodice() == null ? "" : p.getCodice();
            String desc = p.getDescrizione() == null ? "" : p.getDescrizione();
            String tipo = p.getTipoPagamentoCodice() == null ? "" : p.getTipoPagamentoCodice();
            setText(cod + " - " + desc + (tipo.isEmpty() ? "" : " [" + tipo + "]"));
        }
        return this;
    }
}