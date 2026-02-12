package it.jeansandfriends.gestionale.app;

import javax.swing.SwingUtilities;

import it.jeansandfriends.gestionale.db.Database;
import it.jeansandfriends.gestionale.ui.MainFrame;

public class Main {

    public static void main(String[] args) {
        // 1) Inizializza DB (crea file e tabelle se mancano)
        Database.init();

        // 2) Avvia UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });
    }
}
