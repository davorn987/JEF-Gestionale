package it.jeansandfriends.gestionale.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Home - seleziona una voce dal menu", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}