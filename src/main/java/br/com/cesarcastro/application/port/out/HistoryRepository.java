package br.com.cesarcastro.application.port.out;

import br.com.cesarcastro.domain.model.HistoryEntry;

import java.util.List;

public interface HistoryRepository {

    void save(HistoryEntry entry);

    List<HistoryEntry> loadAll();
}

