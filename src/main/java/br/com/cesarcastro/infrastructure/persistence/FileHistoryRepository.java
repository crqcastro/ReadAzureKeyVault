package br.com.cesarcastro.infrastructure.persistence;

import br.com.cesarcastro.application.port.out.HistoryRepository;
import br.com.cesarcastro.domain.model.HistoryEntry;
import br.com.cesarcastro.domain.model.SecretEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class FileHistoryRepository implements HistoryRepository {

    private static final Logger log = Logger.getLogger(FileHistoryRepository.class.getName());

    private static final String ENTRY_MARKER = "### ENTRY ###";
    private static final String SECRETS_MARKER = "### SECRETS ###";
    private static final String END_MARKER = "### END ###";

    private final Path historyFile;

    public FileHistoryRepository(Path historyFile) {
        this.historyFile = Objects.requireNonNull(historyFile, "historyFile é obrigatório");
    }

    @Override
    public void save(HistoryEntry entry) {
        try {
            createParentDirectoryIfNeeded();
            try (BufferedWriter writer = Files.newBufferedWriter(
                    historyFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(ENTRY_MARKER);
                writer.newLine();
                writer.write("timestamp=" + entry.timestamp());
                writer.newLine();
                writer.write("uri=" + entry.uri());
                writer.newLine();
                writer.write("clientId=" + entry.clientId());
                writer.newLine();
                writer.write("tenantId=" + entry.tenantId());
                writer.newLine();
                writer.write(SECRETS_MARKER);
                writer.newLine();
                for (SecretEntry secret : entry.secrets()) {
                    writer.write(secret.key() + "=" + escape(secret.value()));
                    writer.newLine();
                }
                writer.write(END_MARKER);
                writer.newLine();
            }
            restrictFilePermissions();
        } catch (IOException exception) {
            throw new UncheckedIOException("Não foi possível salvar o histórico em " + historyFile, exception);
        }
    }

    @Override
    public List<HistoryEntry> loadAll() {
        if (!Files.exists(historyFile)) {
            return List.of();
        }

        List<HistoryEntry> historyEntries = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(historyFile, StandardCharsets.UTF_8)) {
            String line;
            String timestamp = "";
            String uri = "";
            String clientId = "";
            String tenantId = "";
            List<SecretEntry> secrets = new ArrayList<>();
            boolean insideEntry = false;
            boolean insideSecrets = false;

            while ((line = reader.readLine()) != null) {
                if (ENTRY_MARKER.equals(line)) {
                    insideEntry = true;
                    insideSecrets = false;
                    timestamp = "";
                    uri = "";
                    clientId = "";
                    tenantId = "";
                    secrets = new ArrayList<>();
                    continue;
                }

                if (!insideEntry) {
                    continue;
                }

                if (SECRETS_MARKER.equals(line)) {
                    insideSecrets = true;
                    continue;
                }

                if (END_MARKER.equals(line)) {
                    historyEntries.add(new HistoryEntry(timestamp, uri, clientId, tenantId, secrets));
                    insideEntry = false;
                    insideSecrets = false;
                    continue;
                }

                if (!insideSecrets) {
                    int separator = line.indexOf('=');
                    if (separator <= 0) {
                        continue;
                    }
                    String key = line.substring(0, separator);
                    String value = line.substring(separator + 1);
                    switch (key) {
                        case "timestamp" -> timestamp = value;
                        case "uri" -> uri = value;
                        case "clientId" -> clientId = value;
                        case "tenantId" -> tenantId = value;
                        default -> {
                        }
                    }
                    continue;
                }

                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator);
                String value = unescape(line.substring(separator + 1));
                secrets.add(new SecretEntry(key, value));
            }
        } catch (IOException exception) {
            log.warning("Não foi possível carregar o histórico de " + historyFile + ": " + exception.getMessage());
            return List.of();
        }

        return historyEntries;
    }

    private void createParentDirectoryIfNeeded() throws IOException {
        Path parent = historyFile.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private void restrictFilePermissions() {
        try {
            Files.setPosixFilePermissions(historyFile, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
            // Sistema não-POSIX (ex.: Windows) — permissões não configuradas
        } catch (IOException exception) {
            log.warning("Não foi possível restringir permissões do arquivo de histórico: " + exception.getMessage());
        }
    }

    private String escape(String value) {
        return value.replace("\n", "\\n").replace("\r", "\\r");
    }

    private String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\r", "\r");
    }
}
