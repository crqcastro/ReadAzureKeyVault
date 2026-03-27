package br.com.cesarcastro.presentation.ui;

import br.com.cesarcastro.application.port.out.HistoryRepository;
import br.com.cesarcastro.application.service.KeyVaultSecretsService;
import br.com.cesarcastro.domain.model.HistoryEntry;
import br.com.cesarcastro.domain.model.KeyVaultConnectionSettings;
import br.com.cesarcastro.domain.model.SecretEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class KeyVaultGui extends JFrame {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final KeyVaultSecretsService keyVaultSecretsService;
    private final HistoryRepository historyRepository;
    private final List<HistoryEntry> historyEntries = new ArrayList<>();

    // Componentes inicializados nos métodos de construção do painel
    private JTextField uriField;
    private JTextField clientIdField;
    private JPasswordField clientSecretField;
    private JTextField tenantIdField;
    private JButton connectButton;
    private JProgressBar progressBar;
    private JComboBox<HistoryEntry> historyCombo;
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public KeyVaultGui(KeyVaultSecretsService keyVaultSecretsService, HistoryRepository historyRepository) {
        super("Azure Key Vault Reader");
        this.keyVaultSecretsService = Objects.requireNonNull(keyVaultSecretsService, "keyVaultSecretsService é obrigatório");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository é obrigatório");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 680);
        setMinimumSize(new Dimension(700, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildSecretsPanel(), BorderLayout.CENTER);

        statusLabel = new JLabel("Preencha os dados e clique em Conectar.");
        statusLabel.setBorder(new EmptyBorder(4, 12, 6, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        add(statusLabel, BorderLayout.SOUTH);

        connectButton.addActionListener(e -> connect());
        loadHistoryEntries();
    }

    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(12, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        uriField = new JTextField("", 40);
        clientIdField = new JTextField("", 40);
        clientSecretField = new JPasswordField("", 40);
        tenantIdField = new JTextField("", 40);

        addFormRow(formPanel, gbc, 0, "Key Vault URI:", uriField);
        addFormRow(formPanel, gbc, 1, "Client ID:", clientIdField);
        addFormRow(formPanel, gbc, 2, "Client Secret:", clientSecretField);
        addFormRow(formPanel, gbc, 3, "Tenant ID:", tenantIdField);

        var fieldWatcher = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateConnectButton(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateConnectButton(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateConnectButton(); }
        };
        uriField.getDocument().addDocumentListener(fieldWatcher);
        clientIdField.getDocument().addDocumentListener(fieldWatcher);
        clientSecretField.getDocument().addDocumentListener(fieldWatcher);
        tenantIdField.getDocument().addDocumentListener(fieldWatcher);

        historyCombo = new JComboBox<>();
        historyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof HistoryEntry entry ? entry.displayLabel() : "Selecione um histórico...");
                return this;
            }
        });
        historyCombo.addItem(null);
        historyCombo.setSelectedIndex(0);

        JButton loadHistoryButton = new JButton("Carregar");
        loadHistoryButton.setToolTipText("Preenche os campos e exibe os segredos salvos. Clique em Conectar para buscar dados atualizados.");
        loadHistoryButton.addActionListener(e -> loadFromHistory());

        JPanel historyPanel = new JPanel(new BorderLayout(4, 0));
        historyPanel.add(historyCombo, BorderLayout.CENTER);
        historyPanel.add(loadHistoryButton, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Histórico:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(historyPanel, gbc);

        connectButton = new JButton("Conectar e Listar Segredos");
        connectButton.setFocusPainted(false);
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectButton.setEnabled(false);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(180, 22));
        progressBar.setString("Carregando segredos...");
        progressBar.setStringPainted(true);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.add(connectButton);
        buttonRow.add(progressBar);

        gbc.gridx = 1; gbc.gridy = 5; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(buttonRow, gbc);

        return formPanel;
    }

    private JPanel buildSecretsPanel() {
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Filtrar variáveis...");

        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setBorder(new EmptyBorder(0, 12, 6, 12));
        searchPanel.add(new JLabel("Filtrar:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new String[]{"Variável", "Valor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(580);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(sorter); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(sorter); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(sorter); }
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem copyValue = new JMenuItem("Copiar valor");
        copyValue.addActionListener(e -> copySelectedValue(table));
        JMenuItem copyLine = new JMenuItem("Copiar linha (VAR=valor)");
        copyLine.addActionListener(e -> copySelectedLine(table));
        popup.add(copyValue);
        popup.add(copyLine);
        table.setComponentPopupMenu(popup);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Segredos"));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        return centerPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void connect() {
        KeyVaultConnectionSettings settings = KeyVaultConnectionSettings.fromUserInput(
                uriField.getText(),
                clientIdField.getText(),
                new String(clientSecretField.getPassword()),
                tenantIdField.getText()
        );

        try {
            settings.validateRequiredFields();
        } catch (IllegalArgumentException exception) {
            setStatus(exception.getMessage(), Color.RED);
            return;
        }

        setLoading(true);
        tableModel.setRowCount(0);
        setStatus("Conectando ao Key Vault...", Color.DARK_GRAY);

        SwingWorker<List<SecretEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SecretEntry> doInBackground() {
                return keyVaultSecretsService.loadSecrets(settings);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<SecretEntry> secrets = get();
                    renderSecrets(secrets);
                    setStatus("Carregados " + secrets.size() + " segredos.", new Color(0, 128, 0));
                    saveHistory(settings, secrets);
                    loadHistoryEntries();
                } catch (Exception exception) {
                    setStatus("Erro: " + extractErrorMessage(exception), Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void saveHistory(KeyVaultConnectionSettings settings, List<SecretEntry> secrets) {
        try {
            historyRepository.save(HistoryEntry.from(LocalDateTime.now().format(DT_FMT), settings, secrets));
        } catch (RuntimeException exception) {
            setStatus("Aviso: não foi possível salvar o histórico. " + extractErrorMessage(exception), Color.ORANGE.darker());
        }
    }

    private void loadHistoryEntries() {
        historyEntries.clear();
        historyEntries.addAll(historyRepository.loadAll());

        historyCombo.removeAllItems();
        historyCombo.addItem(null);
        for (int index = historyEntries.size() - 1; index >= 0; index--) {
            historyCombo.addItem(historyEntries.get(index));
        }
    }

    private void loadFromHistory() {
        HistoryEntry entry = (HistoryEntry) historyCombo.getSelectedItem();
        if (entry == null) {
            setStatus("Selecione um histórico para carregar.", new Color(180, 100, 0));
            return;
        }

        uriField.setText(entry.uri());
        clientIdField.setText(entry.clientId());
        clientSecretField.setText(""); // clientSecret não é persistido por segurança; informe novamente
        tenantIdField.setText(entry.tenantId());
        renderSecrets(entry.secrets());
        setStatus("Histórico de " + entry.timestamp() + " carregado (" + entry.secrets().size()
                + " segredos). Informe o Client Secret e clique em Conectar para buscar dados atualizados.",
                new Color(0, 100, 180));
    }

    private void renderSecrets(List<SecretEntry> secrets) {
        tableModel.setRowCount(0);
        for (SecretEntry secret : secrets) {
            tableModel.addRow(new Object[]{secret.key(), secret.value()});
        }
    }

    private void updateConnectButton() {
        connectButton.setEnabled(areRequiredFieldsFilled());
    }

    private void setLoading(boolean loading) {
        connectButton.setEnabled(!loading && areRequiredFieldsFilled());
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
    }

    private boolean areRequiredFieldsFilled() {
        return !uriField.getText().isBlank()
                && !clientIdField.getText().isBlank()
                && clientSecretField.getPassword().length > 0
                && !tenantIdField.getText().isBlank();
    }

    private void filterTable(TableRowSorter<DefaultTableModel> sorter) {
        String text = searchField.getText();
        sorter.setRowFilter(text == null || text.isBlank() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    private void copySelectedValue(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        Object value = table.getValueAt(row, 1);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(value)), null);
    }

    private void copySelectedLine(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        String key = String.valueOf(table.getValueAt(row, 0));
        String value = String.valueOf(table.getValueAt(row, 1));
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(key + "=" + value), null);
    }

    private String extractErrorMessage(Exception exception) {
        Throwable rootCause = exception.getCause() != null ? exception.getCause() : exception;
        return rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.getClass().getSimpleName();
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    public static void launch(KeyVaultSecretsService keyVaultSecretsService, HistoryRepository historyRepository) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new KeyVaultGui(keyVaultSecretsService, historyRepository).setVisible(true);
        });
    }
}
