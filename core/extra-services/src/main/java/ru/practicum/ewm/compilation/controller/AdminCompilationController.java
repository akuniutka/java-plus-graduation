package ru.practicum.ewm.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {

    private final CompilationService service;
    private final CompilationMapper compilationMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto save(@RequestBody @Valid final NewCompilationDto dtoIn) {
        log.info("Received request to add new compilation: title = {}", dtoIn.title());
        log.debug("New compilation = {}", dtoIn);
        Compilation compilation = compilationMapper.mapToCompilation(dtoIn);
        compilation = service.save(compilation);
        final CompilationDto dtoOut = compilationMapper.mapToDto(compilation);
        log.info("Responded with compilation added: id = {}, title = {}", dtoOut.id(), dtoOut.title());
        log.debug("Compilation added = {}", dtoOut);
        return dtoOut;
    }

    @PatchMapping("/{id}")
    public CompilationDto update(
            @PathVariable final long id,
            @RequestBody @Valid final UpdateCompilationRequest dtoIn
    ) {
        log.info("Received request to update compilation: id = {}", id);
        log.debug("Compilation patch = {}", dtoIn);
        final Compilation compilation = service.update(id, dtoIn);
        final CompilationDto dtoOut = compilationMapper.mapToDto(compilation);
        log.info("Responded with updated compilation: id = {}", id);
        log.debug("Updated compilation = {}", dtoIn);
        return dtoOut;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable final long id) {
        log.info("Received request to delete compilation: id = {}", id);
        service.deleteById(id);
        log.info("Responded with {} to compilation delete request: id = {}", HttpStatus.NO_CONTENT, id);
    }
}
