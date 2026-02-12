package it.jeansandfriends.gestionale.ui.components;

import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public final class ComboSelectionUtils {

    private ComboSelectionUtils() {}

    /**
     * Sincronizza il testo dell'editor col displayText dell'item selezionato.
     * Da chiamare dopo prefill e dopo ripristini.
     */
    public static <T> void syncEditorToSelection(FilterableComboBox<T> fcombo) {
        JComboBox<T> combo = fcombo.getCombo();
        JTextField editor = fcombo.getEditorField();

        Object selObj = combo.getSelectedItem();
        if (selObj == null) {
            editor.setText("");
            return;
        }

        @SuppressWarnings("unchecked")
        T sel = (T) selObj;
        editor.setText(fcombo.getDisplayText(sel));
    }

    /**
     * Obbligo scelta esistente:
     * - se editor ha testo ma selectedItem NON è un item valido (tipicamente String), blocca e ripristina lastValid
     * - se selectedItem è un item (oggetto) o null (se consentito), OK
     */
    public static <T> boolean requireValidSelection(
            FilterableComboBox<T> fcombo,
            boolean allowNullSelection,
            String label,
            T lastValidSelection,
            Consumer<T> setLastValidSelection
    ) {
        JComboBox<T> combo = fcombo.getCombo();
        JTextField editor = fcombo.getEditorField();

        Object selObj = combo.getSelectedItem();

        // Caso: nessuna selezione
        if (selObj == null) {
            if (allowNullSelection) {
                setLastValidSelection.accept(null);
                editor.setText("");
                return true;
            }
            JOptionPane.showMessageDialog(combo, label + ": seleziona un valore dalla lista.", "Validazione", JOptionPane.WARNING_MESSAGE);
            restore(combo, fcombo, lastValidSelection, allowNullSelection, setLastValidSelection);
            return false;
        }

        // Caso: l'editor/combobox ha una String (testo libero non valido)
        if (!(selObj.getClass().isInstance(getAnyItemClass(combo)) || isAssignableToMaster(fcombo, selObj))) {
            // fallback: se non è uno dei nostri item, trattiamolo come non valido
            JOptionPane.showMessageDialog(combo,
                    label + ": valore non valido. Seleziona un elemento esistente dalla lista.",
                    "Validazione",
                    JOptionPane.WARNING_MESSAGE);
            restore(combo, fcombo, lastValidSelection, allowNullSelection, setLastValidSelection);
            return false;
        }

        // Caso: è un item vero (oggetto)
        @SuppressWarnings("unchecked")
        T sel = (T) selObj;

        // Se per qualche motivo è null
        if (sel == null) {
            if (allowNullSelection) {
                setLastValidSelection.accept(null);
                editor.setText("");
                return true;
            }
            JOptionPane.showMessageDialog(combo, label + ": seleziona un valore dalla lista.", "Validazione", JOptionPane.WARNING_MESSAGE);
            restore(combo, fcombo, lastValidSelection, allowNullSelection, setLastValidSelection);
            return false;
        }

        // OK
        setLastValidSelection.accept(sel);
        editor.setText(fcombo.getDisplayText(sel));
        return true;
    }

    /**
     * Determina se selObj è presente tra gli item master (quindi è una selezione valida).
     */
    private static <T> boolean isAssignableToMaster(FilterableComboBox<T> fcombo, Object selObj) {
        for (T it : fcombo.getMasterItems()) {
            if (it == null) continue;
            if (it == selObj) return true; // stessa istanza
        }
        return false;
    }

    /**
     * Prova a trovare la classe degli item (best-effort).
     */
    private static <T> Object getAnyItemClass(JComboBox<T> combo) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object it = combo.getItemAt(i);
            if (it != null) return it;
        }
        return null;
    }

    private static <T> void restore(
            JComboBox<T> combo,
            FilterableComboBox<T> fcombo,
            T lastValid,
            boolean allowNull,
            Consumer<T> setLastValidSelection
    ) {
        if (lastValid != null) {
            combo.setSelectedItem(lastValid);
            setLastValidSelection.accept(lastValid);
        } else if (allowNull) {
            combo.setSelectedItem(null);
            setLastValidSelection.accept(null);
        } else if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
            Object selObj = combo.getSelectedItem();
            try {
                @SuppressWarnings("unchecked")
                T sel = (T) selObj;
                setLastValidSelection.accept(sel);
            } catch (ClassCastException ignore) {}
        }
        syncEditorToSelection(fcombo);
    }
}