package ru.practicum.ewm.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationService {

    Compilation save(Compilation compilation);

    List<Compilation> findAll(Boolean pinned, Pageable pageable);

    Compilation getById(long id);

    Compilation update(long id, UpdateCompilationRequest patch);

    void deleteById(long id);
}
