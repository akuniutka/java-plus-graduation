package ru.practicum.ewm.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;

import java.util.List;

@Mapper
public interface CompilationMapper {


    @Mapping(target = "events", ignore = true)
    @Mapping(target = "eventIds", source = "events")
    Compilation mapToCompilation(NewCompilationDto dto);

    CompilationDto mapToDto(Compilation compilation);

    List<CompilationDto> mapToDto(List<Compilation> compilations);
}
