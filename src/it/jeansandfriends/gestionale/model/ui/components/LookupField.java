package it.jeansandfriends.gestionale.ui.components;

import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LookupField<T> extends JPanel {

    private final JTextField txt = new JTextField(30);
    private final JButton btnSelect = new JButton("...");
    private final JButton btnClear = new JButton("X");

    private T value = null;
    private final Function<T, String> toText;

    public LookupField(Function<T, String> toText) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.toText = toText;

        txt.setEditable(false);

        add(txt);
        add(btnSelect);
        add(btnClear);

        btnClear.addActionListener(e -> setValue(null));
    }

    public void bindSelector(JFrame owner, String title,
            Function<Void, List<T>> loadItems,
            String[] columns,
            Function<T, Object>[] getters) {

        btnSelect.addActionListener(e -> {
            List<T> items = loadItems.apply(null);

            LookupDialog<T> dlg = new LookupDialog<>(owner, title, items, columns, getters);
            dlg.setVisible(true);

            if (dlg.isConfirmed()) {
                setValue(dlg.getSelectedItem()); // può essere null se "Svuota"
            }
        });
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        txt.setText(value == null ? "" : safe(toText.apply(value)));
    }

    public JTextField getTextField() { return txt; }

    private static String safe(String s) { return s == null ? "" : s; }
}