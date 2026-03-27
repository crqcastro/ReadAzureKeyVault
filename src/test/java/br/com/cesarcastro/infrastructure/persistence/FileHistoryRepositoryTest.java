package br.com.cesarcastro.infrastructure.persistence;

import br.com.cesarcastro.domain.model.HistoryEntry;
import br.com.cesarcastro.domain.model.SecretEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileHistoryRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistAndLoadHistoryEntriesKeepingEscapedValues() {
        FileHistoryRepository repository = new FileHistoryRepository(tempDir.resolve("keyvault-history.txt"));
        HistoryEntry expected = new HistoryEntry(
                "2026-03-27 21:00:00",
                "https://meu-kv.vault.azure.net",
                "client-id",
                "tenant-id",
                List.of(
                        new SecretEntry("API_KEY", "123"),
                        new SecretEntry("MULTILINE", "linha1\nlinha2")
                )
        );

        repository.save(expected);

        List<HistoryEntry> loaded = repository.loadAll();

        assertEquals(List.of(expected), loaded);
    }

    @Test
    void constructor_throwsOnNullHistoryFile() {
        assertThrows(NullPointerException.class, () -> new FileHistoryRepository(null));
    }

    @Test
    void loadAll_returnsEmptyListWhenFileDoesNotExist() {
        var repository = new FileHistoryRepository(tempDir.resolve("nonexistent.txt"));
        assertEquals(List.of(), repository.loadAll());
    }

    @Test
    void loadAll_returnsMultipleEntriesInOrder() {
        var repository = new FileHistoryRepository(tempDir.resolve("history.txt"));
        var entry1 = new HistoryEntry("2026-01-01 10:00:00", "https://kv1.vault.azure.net", "cid1", "tid1",
                List.of(new SecretEntry("K1", "V1")));
        var entry2 = new HistoryEntry("2026-01-02 10:00:00", "https://kv2.vault.azure.net", "cid2", "tid2",
                List.of(new SecretEntry("K2", "V2")));

        repository.save(entry1);
        repository.save(entry2);

        assertEquals(List.of(entry1, entry2), repository.loadAll());
    }

    @Test
    void loadAll_handlesEntryWithNoSecrets() {
        var repository = new FileHistoryRepository(tempDir.resolve("history.txt"));
        var entry = new HistoryEntry("2026-01-01 10:00:00", "https://kv.vault.azure.net", "cid", "tid", List.of());

        repository.save(entry);

        assertEquals(List.of(entry), repository.loadAll());
    }

    @Test
    void save_createsParentDirectoriesWhenTheyDoNotExist() {
        Path nestedFile = tempDir.resolve("subdir").resolve("nested").resolve("history.txt");
        var repository = new FileHistoryRepository(nestedFile);
        var entry = new HistoryEntry("ts", "uri", "cid", "tid", List.of());

        repository.save(entry);

        assertTrue(Files.exists(nestedFile));
    }

    @Test
    void save_throwsUncheckedIOExceptionWhenHistoryFileIsADirectory() throws IOException {
        Path dir = tempDir.resolve("not-a-file");
        Files.createDirectory(dir);
        var repository = new FileHistoryRepository(dir);
        var entry = new HistoryEntry("ts", "uri", "cid", "tid", List.of());

        assertThrows(UncheckedIOException.class, () -> repository.save(entry));
    }

    @Test
    void loadAll_returnsEmptyListOnIOException() throws IOException {
        Path dir = tempDir.resolve("not-a-file");
        Files.createDirectory(dir);
        var repository = new FileHistoryRepository(dir);

        assertEquals(List.of(), repository.loadAll());
    }

    @Test
    void loadAll_skipsLinesBeforeFirstEntryMarker() throws IOException {
        Path file = tempDir.resolve("history.txt");
        Files.writeString(file, """
                some random content before
                another stray line
                ### ENTRY ###
                timestamp=2026-01-01 12:00:00
                uri=https://kv.vault.azure.net
                clientId=cid
                tenantId=tid
                ### SECRETS ###
                ### END ###
                """, StandardCharsets.UTF_8);

        var entries = new FileHistoryRepository(file).loadAll();

        assertEquals(1, entries.size());
        assertEquals("2026-01-01 12:00:00", entries.get(0).timestamp());
    }

    @Test
    void loadAll_skipsMetadataLinesWithNoEqualsSeparator() throws IOException {
        Path file = tempDir.resolve("history.txt");
        Files.writeString(file, """
                ### ENTRY ###
                timestamp=2026-01-01 12:00:00
                malformed-line-with-no-equals
                uri=https://kv.vault.azure.net
                clientId=cid
                tenantId=tid
                ### SECRETS ###
                ### END ###
                """, StandardCharsets.UTF_8);

        var entries = new FileHistoryRepository(file).loadAll();

        assertEquals(1, entries.size());
        assertEquals("https://kv.vault.azure.net", entries.get(0).uri());
    }

    @Test
    void loadAll_ignoresUnknownMetadataKeys() throws IOException {
        Path file = tempDir.resolve("history.txt");
        Files.writeString(file, """
                ### ENTRY ###
                timestamp=2026-01-01 12:00:00
                uri=https://kv.vault.azure.net
                clientId=cid
                tenantId=tid
                unknownKey=shouldBeIgnored
                ### SECRETS ###
                ### END ###
                """, StandardCharsets.UTF_8);

        var entries = new FileHistoryRepository(file).loadAll();

        assertEquals(1, entries.size());
        assertEquals("cid", entries.get(0).clientId());
    }

    @Test
    void loadAll_skipsSecretLinesWithNoEqualsSeparator() throws IOException {
        Path file = tempDir.resolve("history.txt");
        Files.writeString(file, """
                ### ENTRY ###
                timestamp=ts
                uri=uri
                clientId=cid
                tenantId=tid
                ### SECRETS ###
                malformed-secret-line
                VALID_KEY=valid-value
                ### END ###
                """, StandardCharsets.UTF_8);

        var entries = new FileHistoryRepository(file).loadAll();

        assertEquals(1, entries.size());
        assertEquals(1, entries.get(0).secrets().size());
        assertEquals("VALID_KEY", entries.get(0).secrets().get(0).key());
    }
}
