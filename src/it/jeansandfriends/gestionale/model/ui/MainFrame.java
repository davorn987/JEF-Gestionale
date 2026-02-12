package it.jeansandfriends.gestionale.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private static final String CARD_HOME = "HOME";
    private static final String CARD_CONFIG = "CONFIG";
    private static final String CARD_CLIENTI = "CLIENTI";
    private static final String CARD_FORNITORI = "FORNITORI";
    private static final String CARD_IVA = "IVA";
    private static final String CARD_PAGAMENTI_TIPI = "PAGAMENTI_TIPI";
    private static final String CARD_PAGAMENTI = "PAGAMENTI";
    private static final String CARD_NAZIONI = "NAZIONI";
    private static final String CARD_PROVINCE = "PROVINCE";
    private static final String CARD_FATTURA_NUOVA = "FATTURA_NUOVA";
    private static final String CARD_FATTURE_STORICO = "FATTURE_STORICO";

    public MainFrame() {
        super("Gestionale - Jeans & Friends");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        setJMenuBar(createMenuBar());

        contentPanel.add(new HomePanel(), CARD_HOME);
        contentPanel.add(new ConfigPanel(), CARD_CONFIG);
        contentPanel.add(new ClientiPanel(), CARD_CLIENTI);
        contentPanel.add(new FornitoriPanel(), CARD_FORNITORI);
        contentPanel.add(new IvaPanel(), CARD_IVA);
        contentPanel.add(new PagamentiTipiPanel(), CARD_PAGAMENTI_TIPI);
        contentPanel.add(new PagamentiPanel(), CARD_PAGAMENTI);

        contentPanel.add(new NazioniPanel(), CARD_NAZIONI);
        contentPanel.add(new ProvincePanel(), CARD_PROVINCE);

        contentPanel.add(new FatturaNuovaPanel(), CARD_FATTURA_NUOVA);
        contentPanel.add(new FattureStoricoPanel(), CARD_FATTURE_STORICO);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, CARD_HOME);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem config = new JMenuItem("Config (TODO)");
        config.addActionListener(e -> showCard(CARD_CONFIG));
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());
        file.add(config);
        file.addSeparator();
        file.add(exit);

        JMenu anagrafica = new JMenu("Anagrafica");
        JMenuItem clienti = new JMenuItem("Clienti");
        clienti.addActionListener(e -> showCard(CARD_CLIENTI));
        JMenuItem fornitori = new JMenuItem("Fornitori");
        fornitori.addActionListener(e -> showCard(CARD_FORNITORI));
        JMenuItem iva = new JMenuItem("IVA");
        iva.addActionListener(e -> showCard(CARD_IVA));

        JMenuItem tipiPag = new JMenuItem("Tipi di Pagamento");
        tipiPag.addActionListener(e -> showCard(CARD_PAGAMENTI_TIPI));
        JMenuItem pagamenti = new JMenuItem("Pagamenti");
        pagamenti.addActionListener(e -> showCard(CARD_PAGAMENTI));

        JMenuItem nazioni = new JMenuItem("Nazioni");
        nazioni.addActionListener(e -> showCard(CARD_NAZIONI));
        JMenuItem province = new JMenuItem("Province");
        province.addActionListener(e -> showCard(CARD_PROVINCE));

        anagrafica.add(clienti);
        anagrafica.add(fornitori);
        anagrafica.add(iva);
        anagrafica.addSeparator();
        anagrafica.add(tipiPag);
        anagrafica.add(pagamenti);
        anagrafica.addSeparator();
        anagrafica.add(nazioni);
        anagrafica.add(province);

        JMenu fatture = new JMenu("Fatture");
        JMenuItem nuova = new JMenuItem("Nuova (TODO)");
        nuova.addActionListener(e -> showCard(CARD_FATTURA_NUOVA));
        JMenuItem storico = new JMenuItem("Storico (TODO)");
        storico.addActionListener(e -> showCard(CARD_FATTURE_STORICO));
        fatture.add(nuova);
        fatture.add(storico);

        bar.add(file);
        bar.add(anagrafica);
        bar.add(fatture);

        return bar;
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
    }
}