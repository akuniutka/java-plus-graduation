package ru.practicum.ewm.compilation.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;

import java.util.List;

@Component
public class CompilationMapper {

    public Compilation mapToCompilation(final NewCompilationDto dto) {
        if (dto == null) {
            return null;
        }
        final Compilation compilation = new Compilation();
        compilation.setEventIds(dto.events());
        compilation.setPinned(dto.pinned());
        compilation.setTitle(dto.title());
        return compilation;
    }

    public CompilationDto mapToDto(final Compilation compilation) {
        if (compilation == null) {
            return null;
        }
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents())
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .build();
    }

    public List<CompilationDto> mapToDto(final List<Compilation> compilations) {
        if (compilations == null) {
            return null;
        }
        return compilations.stream()
                .map(this::mapToDto)
                .toList();
    }
}
