package it.jeansandfriends.gestionale.ui.components;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FilterableComboBox<T> {

    private final JComboBox<T> combo;
    private final Function<T, String> toText;

    private List<T> master = new ArrayList<>();

    private boolean isUpdatingModel = false;
    private boolean pendingUpdate = false;

    public FilterableComboBox(JComboBox<T> combo, Function<T, String> toText) {
        this.combo = combo;
        this.toText = toText;

        this.combo.setEditable(true);

        JTextField editor = getEditorField();

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { scheduleFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { scheduleFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { scheduleFilter(); }
        });

        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(editor::selectAll);
            }
        });
    }

    public void setItems(List<T> items, boolean includeNullAsFirst) {
        master = new ArrayList<>();
        if (includeNullAsFirst) master.add(null);
        if (items != null) master.addAll(items);

        // impostiamo model completo, senza popup e senza filtrare
        setModelFrom(master);
    }

    public List<T> getMasterItems() {
        return new ArrayList<>(master);
    }

    public String getDisplayText(T item) {
        if (item == null) return "";
        String t = toText.apply(item);
        return t == null ? "" : t;
    }

    public JComboBox<T> getCombo() { return combo; }

    public JTextField getEditorField() {
        return (JTextField) combo.getEditor().getEditorComponent();
    }

    private void scheduleFilter() {
        if (isUpdatingModel) return;

        // debounce: se già pianificato, non pianificare di nuovo
        if (pendingUpdate) return;
        pendingUpdate = true;

        SwingUtilities.invokeLater(() -> {
            pendingUpdate = false;
            if (isUpdatingModel) return;
            applyFilterNow(getEditorText());
        });
    }

    private void applyFilterNow(String text) {
        if (isUpdatingModel) return;

        String q = normalize(text);

        // Nota: non tocchiamo editor.setText qui dentro; cambiamo solo model in modo safe (fuori dal lock doc)
        List<T> filtered = new ArrayList<>();
        for (T item : master) {
            if (item == null) {
                filtered.add(null);
                continue;
            }
            String label = normalize(toText.apply(item));
            if (q.isEmpty() || label.contains(q)) {
                filtered.add(item);
            }
        }

        Object selectedBefore = combo.getSelectedItem();

        isUpdatingModel = true;
        try {
            setModelFrom(filtered);

            // prova a mantenere selezione se è un item presente
            if (selectedBefore != null) {
                combo.setSelectedItem(selectedBefore);
            }

            // popup solo se visibile e focus sull'editor
            boolean canShowPopup = combo.isShowing() && getEditorField().isFocusOwner();
            if (canShowPopup) combo.setPopupVisible(true);
        } finally {
            isUpdatingModel = false;
        }
    }

    private String getEditorText() {
        Object editorItem = combo.getEditor().getItem();
        return editorItem == null ? "" : editorItem.toString();
    }

    private void setModelFrom(List<T> items) {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T it : items) model.addElement(it);
        combo.setModel(model);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }
}