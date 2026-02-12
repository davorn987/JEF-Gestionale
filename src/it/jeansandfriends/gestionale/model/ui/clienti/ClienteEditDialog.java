package it.jeansandfriends.gestionale.ui.clienti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.jeansandfriends.gestionale.dao.ClienteDao;
import it.jeansandfriends.gestionale.dao.IvaDao;
import it.jeansandfriends.gestionale.dao.NazioneDao;
import it.jeansandfriends.gestionale.dao.PagamentoDao;
import it.jeansandfriends.gestionale.dao.ProvinciaDao;
import it.jeansandfriends.gestionale.model.Cliente;
import it.jeansandfriends.gestionale.model.Iva;
import it.jeansandfriends.gestionale.model.Nazione;
import it.jeansandfriends.gestionale.model.Pagamento;
import it.jeansandfriends.gestionale.model.Provincia;
import it.jeansandfriends.gestionale.ui.components.LookupField;
import it.jeansandfriends.gestionale.ui.destinazioni.DestinazioniDialog;

public class ClienteEditDialog extends JDialog {

    private final JTextField txtCodice = new JTextField(10); // read-only
    private final JCheckBox chkAttivo = new JCheckBox("Attivo");

    private final JTextField txtRagioneSociale = new JTextField(30);
    private final JTextField txtIndirizzo = new JTextField(30);
    private final JTextField txtCap = new JTextField(10);
    private final JTextField txtCitta = new JTextField(20);

    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtCellulare = new JTextField(20);
    private final JTextField txtPiva = new JTextField(20);
    private final JTextField txtCf = new JTextField(20);

    private final JTextField txtEmail = new JTextField(25);

    // LookupFields
    private final LookupField<Provincia> lfProvincia = new LookupField<>(p ->
            safe(p.getSigla()) + " - " + safe(p.getNome())
    );

    private final LookupField<Nazione> lfNazione = new LookupField<>(n ->
            safe(n.getCodiceIso2()) + " - " + safe(n.getDescrizioneIt())
    );

    private final LookupField<Iva> lfIva = new LookupField<>(i -> {
        String cod = safe(i.getCodice());
        String desc = safe(i.getDescrizione());
        String pct = String.valueOf(i.getPercentuale());
        if (desc.isEmpty()) return cod + " (" + pct + "%)";
        return cod + " - " + desc + " (" + pct + "%)";
    });

    private final LookupField<Pagamento> lfPagamento = new LookupField<>(p -> {
        String cod = safe(p.getCodice());
        String desc = safe(p.getDescrizione());
        String tipo = safe(p.getTipoPagamentoCodice());
        String base = cod + (desc.isEmpty() ? "" : (" - " + desc));
        if (!tipo.isEmpty()) base = base + " [" + tipo + "]";
        return base;
    });

    private final LookupField<Cliente> lfClienteFatt = new LookupField<>(c ->
            String.format("%06d", c.getCodiceCliente()) + " - " + safe(c.getRagioneSociale())
    );

    private boolean confirmed = false;
    private final Cliente cliente;

    // ID selezionati
    private Long provinciaId = null;
    private Long nazioneId = null;
    private Long ivaId = null;
    private Long pagamentoId = null;

    public ClienteEditDialog(JFrame owner, String title, Cliente clienteToEditOrNull, int codiceClienteForNew) {
        super(owner, title, true);

        if (clienteToEditOrNull != null) {
            this.cliente = cloneCliente(clienteToEditOrNull);
        } else {
            this.cliente = new Cliente();
            this.cliente.setCodiceCliente(codiceClienteForNew);
            this.cliente.setAttivo(true);
        }

        setSize(980, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        txtCodice.setEditable(false);

        bindLookups(owner);

        // UI
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int r = 0;
        addRow(form, gbc, r++, "Codice Cliente", txtCodice, chkAttivo);

        addRow(form, gbc, r++, "Ragione Sociale*", txtRagioneSociale);
        addRow(form, gbc, r++, "Indirizzo", txtIndirizzo);
        addRow(form, gbc, r++, "CAP", txtCap);
        addRow(form, gbc, r++, "Città", txtCitta);

        addRow(form, gbc, r++, "Provincia (opz.)", lfProvincia);
        addRow(form, gbc, r++, "Nazione (opz.)", lfNazione);

        addRow(form, gbc, r++, "Telefono", txtTelefono);
        addRow(form, gbc, r++, "Cellulare", txtCellulare);
        addRow(form, gbc, r++, "P. IVA", txtPiva);
        addRow(form, gbc, r++, "Codice Fiscale", txtCf);

        addRow(form, gbc, r++, "IVA predefinita", lfIva);
        addRow(form, gbc, r++, "Pagamento predefinito", lfPagamento);

        addRow(form, gbc, r++, "Cliente Fatturazione (opz.)", lfClienteFatt);

        addRow(form, gbc, r++, "Email", txtEmail);

        add(form, BorderLayout.CENTER);

        JButton btnDest = new JButton("Destinazioni...");
        btnDest.addActionListener(e -> onDestinazioni());

        JButton btnOk = new JButton("Salva");
        btnOk.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Annulla");
        btnCancel.addActionListener(e -> { confirmed = false; dispose(); });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnDest);
        buttons.add(btnOk);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // Prefill campi testuali
        txtCodice.setText(String.format("%06d", cliente.getCodiceCliente()));
        chkAttivo.setSelected(cliente.isAttivo());

        txtRagioneSociale.setText(nvl(cliente.getRagioneSociale()));
        txtIndirizzo.setText(nvl(cliente.getIndirizzo()));
        txtCap.setText(nvl(cliente.getCap()));
        txtCitta.setText(nvl(cliente.getCitta()));

        txtTelefono.setText(nvl(cliente.getTelefono()));
        txtCellulare.setText(nvl(cliente.getCellulare()));
        txtPiva.setText(nvl(cliente.getPiva()));
        txtCf.setText(nvl(cliente.getCf()));
        txtEmail.setText(nvl(cliente.getEmail()));

        // Prefill lookup da id (pulendo se inattivo/non trovato)
        provinciaId = cliente.getProvinciaId();
        nazioneId = cliente.getNazioneId();
        ivaId = cliente.getIvaId();
        pagamentoId = cliente.getPagamentoId();

        Provincia prov = (provinciaId == null) ? null : new ProvinciaDao().findById(provinciaId);
        lfProvincia.setValue(prov);
        if (prov == null) provinciaId = null;

        Nazione naz = (nazioneId == null) ? null : new NazioneDao().findById(nazioneId);
        lfNazione.setValue(naz);
        if (naz == null) nazioneId = null;

        Iva iva = (ivaId == null) ? null : new IvaDao().findById(ivaId);
        lfIva.setValue(iva);
        if (iva == null) ivaId = null;

        Pagamento pag = (pagamentoId == null) ? null : new PagamentoDao().findById(pagamentoId);
        lfPagamento.setValue(pag);
        if (pag == null) pagamentoId = null;

        if (cliente.getFatturazioneClienteId() != null) {
            lfClienteFatt.setValue(new ClienteDao().findById(cliente.getFatturazioneClienteId()));
        } else {
            lfClienteFatt.setValue(null);
        }
    }

    private void bindLookups(JFrame owner) {
        // Provincia: SOLO ATTIVE
        lfProvincia.bindSelector(owner,
                "Seleziona Provincia",
                v -> new ProvinciaDao().findAll(false),
                new String[] { "Sigla", "Nome" },
                new Function[] {
                        (Function<Provincia, Object>) p -> p.getSigla(),
                        (Function<Provincia, Object>) p -> p.getNome()
                });

        // Nazione: SOLO ATTIVE
        lfNazione.bindSelector(owner,
                "Seleziona Nazione",
                v -> new NazioneDao().findAll(false),
                new String[] { "ISO2", "Descrizione" },
                new Function[] {
                        (Function<Nazione, Object>) n -> n.getCodiceIso2(),
                        (Function<Nazione, Object>) n -> n.getDescrizioneIt()
                });

        // IVA (tutte)
        lfIva.bindSelector(owner,
                "Seleziona IVA",
                v -> new IvaDao().findAll(),
                new String[] { "Codice", "Descrizione", "%" },
                new Function[] {
                        (Function<Iva, Object>) i -> i.getCodice(),
                        (Function<Iva, Object>) i -> i.getDescrizione(),
                        (Function<Iva, Object>) i -> i.getPercentuale()
                });

        // Pagamento (tutti)
        lfPagamento.bindSelector(owner,
                "Seleziona Pagamento",
                v -> new PagamentoDao().findAll(),
                new String[] { "Codice", "Descrizione", "Tipo" },
                new Function[] {
                        (Function<Pagamento, Object>) p -> p.getCodice(),
                        (Function<Pagamento, Object>) p -> p.getDescrizione(),
                        (Function<Pagamento, Object>) p -> p.getTipoPagamentoCodice()
                });

        // Cliente Fatturazione (attivi, escluso me)
        lfClienteFatt.bindSelector(owner,
                "Seleziona Cliente Fatturazione",
                v -> loadClientiFatturazione(),
                new String[] { "Codice", "Ragione Sociale" },
                new Function[] {
                        (Function<Cliente, Object>) c -> String.format("%06d", c.getCodiceCliente()),
                        (Function<Cliente, Object>) c -> c.getRagioneSociale()
                });
    }

    private List<Cliente> loadClientiFatturazione() {
        List<Cliente> all = new ClienteDao().findAll(false);
        List<Cliente> filtered = new ArrayList<>();
        for (Cliente c : all) {
            if (cliente.getId() != null && cliente.getId().equals(c.getId())) continue;
            filtered.add(c);
        }
        return filtered;
    }

    private void onDestinazioni() {
        if (cliente.getId() == null) {
            JOptionPane.showMessageDialog(this,
                    "Prima salva il cliente, poi potrai gestire le destinazioni.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFrame owner = (JFrame) getOwner();
        DestinazioniDialog dlg = new DestinazioniDialog(owner, cliente.getId(),
                String.format("%06d", cliente.getCodiceCliente()) + " - " + nvl(cliente.getRagioneSociale()));
        dlg.setVisible(true);
    }

    private void onSave() {
        String ragSoc = txtRagioneSociale.getText().trim();
        if (ragSoc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ragione Sociale è obbligatoria.", "Validazione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Provincia prov = lfProvincia.getValue();
        provinciaId = (prov == null ? null : prov.getId());

        Nazione naz = lfNazione.getValue();
        nazioneId = (naz == null ? null : naz.getId());

        Iva iva = lfIva.getValue();
        ivaId = (iva == null ? null : iva.getId());

        Pagamento pag = lfPagamento.getValue();
        pagamentoId = (pag == null ? null : pag.getId());

        Cliente fatt = lfClienteFatt.getValue();
        Long fattId = (fatt == null ? null : fatt.getId());

        if (cliente.getId() != null && fattId != null) {
            String loopMsg = validateNoFatturazioneLoop(cliente.getId(), fattId);
            if (loopMsg != null) {
                JOptionPane.showMessageDialog(this, loopMsg, "Validazione", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        cliente.setAttivo(chkAttivo.isSelected());
        cliente.setRagioneSociale(ragSoc);

        cliente.setIndirizzo(emptyToNull(txtIndirizzo.getText()));
        cliente.setCap(emptyToNull(txtCap.getText()));
        cliente.setCitta(emptyToNull(txtCitta.getText()));

        cliente.setProvinciaId(provinciaId);
        cliente.setNazioneId(nazioneId);

        cliente.setTelefono(emptyToNull(txtTelefono.getText()));
        cliente.setCellulare(emptyToNull(txtCellulare.getText()));
        cliente.setPiva(emptyToNull(txtPiva.getText()));
        cliente.setCf(emptyToNull(txtCf.getText()));

        cliente.setIvaId(ivaId);
        cliente.setPagamentoId(pagamentoId);

        cliente.setFatturazioneClienteId(fattId);

        cliente.setEmail(emptyToNull(txtEmail.getText()));

        confirmed = true;
        dispose();
    }

    private String validateNoFatturazioneLoop(long thisClienteId, long newFatturazioneClienteId) {
        ClienteDao dao = new ClienteDao();

        List<Long> chain = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        Long current = newFatturazioneClienteId;
        int guard = 0;

        while (current != null && guard++ < 200) {
            if (current.equals(thisClienteId)) {
                String path = buildFriendlyPath(thisClienteId, chain, dao);
                return "Impostazione non valida: crea un ciclo di fatturazione.\n\nCatena:\n" + path;
            }
            if (!visited.add(current)) {
                String path = buildFriendlyPath(thisClienteId, chain, dao);
                return "Impostazione non valida: rilevato ciclo nella catena di fatturazione.\n\nCatena:\n" + path;
            }
            chain.add(current);
            current = dao.findFatturazioneClienteId(current);
        }
        return null;
    }

    private static String buildFriendlyPath(long thisClienteId, List<Long> chain, ClienteDao dao) {
        StringBuilder sb = new StringBuilder();
        sb.append(labelClienteSafe(dao.findById(thisClienteId)));
        for (Long id : chain) {
            sb.append(" -> ").append(labelClienteSafe(dao.findById(id)));
        }
        sb.append(" -> ").append(labelClienteSafe(dao.findById(thisClienteId)));
        return sb.toString();
    }

    private static String labelClienteSafe(Cliente c) {
        if (c == null) return "(cliente non trovato)";
        String cod = String.format("%06d", c.getCodiceCliente());
        String rag = c.getRagioneSociale() == null ? "" : c.getRagioneSociale();
        return cod + " " + rag;
    }

    public boolean isConfirmed() { return confirmed; }
    public Cliente getCliente() { return cliente; }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field, JCheckBox optionalCheck) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);

        if (optionalCheck != null) {
            gbc.gridx = 2; gbc.weightx = 0;
            panel.add(optionalCheck, gbc);
        }
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JPanel customField) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(customField, gbc);
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String nvl(String s) { return s == null ? "" : s; }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Cliente cloneCliente(Cliente src) {
        Cliente c = new Cliente();
        c.setId(src.getId());
        c.setCodiceCliente(src.getCodiceCliente());
        c.setAttivo(src.isAttivo());

        c.setRagioneSociale(src.getRagioneSociale());
        c.setIndirizzo(src.getIndirizzo());
        c.setCap(src.getCap());
        c.setCitta(src.getCitta());

        c.setProvinciaId(src.getProvinciaId());
        c.setNazioneId(src.getNazioneId());

        c.setTelefono(src.getTelefono());
        c.setCellulare(src.getCellulare());
        c.setPiva(src.getPiva());
        c.setCf(src.getCf());

        c.setIvaId(src.getIvaId());
        c.setPagamentoId(src.getPagamentoId());
        c.setFatturazioneClienteId(src.getFatturazioneClienteId());

        c.setEmail(src.getEmail());
        c.setNote(src.getNote());
        return c;
    }
}