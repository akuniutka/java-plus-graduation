package ru.practicum.ewm.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationController {

    private final CompilationService service;
    private final CompilationMapper compilationMapper;

    @GetMapping
    public List<CompilationDto> findAll(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
            @RequestParam(defaultValue = "10") @Positive final int size
    ) {
        log.info("Received request for compilations: pinned = {}, from = {}, size = {}", pinned, from, size);
        final Pageable pageable = PageRequest.of(from / size, size);
        final List<Compilation> compilations = service.findAll(pinned, pageable);
        final List<CompilationDto> dtos = compilationMapper.mapToDto(compilations);
        log.info("Responded with requested compilations: pinned = {}, from = {}, size = {}", pinned, from, size);
        log.debug("Requested compilations = {}", dtos);
        return dtos;
    }

    @GetMapping("/{id}")
    public CompilationDto getById(@PathVariable final long id) {
        log.info("Received request for compilation: id = {}", id);
        final Compilation compilation = service.getById(id);
        final CompilationDto dto = compilationMapper.mapToDto(compilation);
        log.info("Responded with requested compilation: id = {}", dto.id());
        log.debug("Requested compilation = {}", dto);
        return dto;
    }
}
