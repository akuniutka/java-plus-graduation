package ru.practicum.ewm.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    Compilation mapToCompilation(final NewCompilationDto dto) {
        if (dto == null) {
            return null;
        }
        final Compilation compilation = new Compilation();
        compilation.setEventIds(dto.events());
        compilation.setPinned(dto.pinned());
        compilation.setTitle(dto.title());
        return compilation;
    }

    CompilationDto mapToDto(final Compilation compilation) {
        if (compilation == null) {
            return null;
        }
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    List<CompilationDto> mapToDto(final List<Compilation> compilations) {
        if (compilations == null) {
            return null;
        }
        return compilations.stream().map(this::mapToDto).toList();
    }
}
