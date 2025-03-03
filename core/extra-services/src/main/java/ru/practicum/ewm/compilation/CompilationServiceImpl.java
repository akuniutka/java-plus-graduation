package ru.practicum.ewm.compilation;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationMapper mapper;
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        final BooleanExpression byPinned = pinned != null
                ? QCompilation.compilation.pinned.eq(pinned)
                : Expressions.TRUE; // если pinned = null ищем все подборки без фильтрации
        final List<Compilation> compilations = compilationRepository.findAll(byPinned, pageable).getContent();
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
        return mapper.mapToDto(compilations);
    }

    @Override
    public CompilationDto getById(long id) {
        final Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Compilation.class, id));
        compilation.setEvents(fetchEvents(compilation.getEventIds()));
        return mapper.mapToDto(compilation);
    }

    @Transactional
    @Override
    public CompilationDto save(final NewCompilationDto requestDto) {
        final Compilation compilation = mapper.mapToCompilation(requestDto);
        final Compilation savedCompilation = compilationRepository.save(compilation);
        savedCompilation.setEvents(fetchEvents(compilation.getEventIds()));
        return mapper.mapToDto(savedCompilation);
    }

    @Transactional
    @Override
    public void delete(long id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException(Compilation.class, id);
        }
        compilationRepository.deleteById(id);
    }

    @Transactional
    @Override
    public CompilationDto update(long id, UpdateCompilationRequest requestDto) {
        final Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Compilation.class, id));
        if (requestDto.title() != null) {
            compilation.setTitle(requestDto.title());
        }
        if (requestDto.pinned() != null) {
            compilation.setPinned(requestDto.pinned());
        }
        if (requestDto.events() != null) {
            compilation.setEventIds(requestDto.events());
        }
        final Compilation updatedCompilation = compilationRepository.save(compilation);
        updatedCompilation.setEvents(fetchEvents(updatedCompilation.getEventIds()));
        return mapper.mapToDto(updatedCompilation);
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
