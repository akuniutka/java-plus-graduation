package ru.practicum.ewm.compilation.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.compilation.model.QCompilation;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final EventClient eventClient;
    private final CompilationRepository repository;

    @Override
    public Compilation save(final Compilation compilation) {
        final Compilation savedCompilation = repository.save(compilation);
        log.info("Added new compilation: id = {}, title = {}", savedCompilation.getId(), savedCompilation.getTitle());
        log.debug("Compilation added = {}", savedCompilation);
        savedCompilation.setEvents(fetchEvents(compilation.getEventIds()));
        return savedCompilation;
    }

    @Override
    public List<Compilation> findAll(final Boolean pinned, final Pageable pageable) {
        final BooleanExpression byPinned = pinned != null
                ? QCompilation.compilation.pinned.eq(pinned)
                : Expressions.TRUE; // если pinned = null ищем все подборки без фильтрации
        final List<Compilation> compilations = repository.findAll(byPinned, pageable).getContent();
        final Set<Long> eventIds = compilations.stream()
                .flatMap(compilation -> compilation.getEventIds().stream())
                .collect(Collectors.toSet());
        final Map<Long, EventShortDto> events = fetchEvents(eventIds).stream()
                .collect(Collectors.toMap(EventShortDto::id, Function.identity()));
        compilations.forEach(compilation -> compilation.setEvents(
                compilation.getEventIds().stream()
                        .map(events::get)
                        .collect(Collectors.toSet())
        ));
        return compilations;
    }

    @Override
    public Compilation getById(final long id) {
        final Compilation compilation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(Compilation.class, id));
        compilation.setEvents(fetchEvents(compilation.getEventIds()));
        return compilation;
    }

    @Override
    public Compilation update(final long id, final UpdateCompilationRequest patch) {
        Compilation compilation = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(Compilation.class, id));
        applyPatch(compilation, patch);
        compilation = repository.save(compilation);
        log.info("Updated compilation: id = {}", compilation.getId());
        log.debug("Updated compilation = {}", compilation);
        compilation.setEvents(fetchEvents(compilation.getEventIds()));
        return compilation;
    }

    @Override
    public void deleteById(final long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(Compilation.class, id);
        }
        repository.deleteById(id);
        log.info("Deleted compilation: id = {}", id);
    }

    private void applyPatch(final Compilation compilation, final UpdateCompilationRequest patch) {
        Optional.ofNullable(patch.title()).ifPresent(compilation::setTitle);
        Optional.ofNullable(patch.pinned()).ifPresent(compilation::setPinned);
        Optional.ofNullable(patch.events()).ifPresent(compilation::setEventIds);
    }

    private Set<EventShortDto> fetchEvents(final Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Set.of();
        }
        final InternalEventFilter filter = InternalEventFilter.builder()
                .events(new ArrayList<>(ids))
                .build();
        final Set<EventShortDto> relatedEvents = new HashSet<>(eventClient.findAll(filter));
        if (ids.size() != relatedEvents.size()) {
            final Set<Long> foundEventIds = relatedEvents.stream()
                    .map(EventShortDto::id)
                    .collect(Collectors.toSet());
            Set<Long> missingEventIds = new HashSet<>(ids);
            missingEventIds.removeAll(foundEventIds);
            throw new NotFoundException("Event", missingEventIds);
        }
        return relatedEvents;
    }
}
