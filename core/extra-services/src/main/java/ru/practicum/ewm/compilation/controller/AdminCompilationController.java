package ru.practicum.ewm.compilation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController extends HttpRequestResponseLogger {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto save(@RequestBody @Valid final NewCompilationDto requestDto,
            final HttpServletRequest request) {
        logHttpRequest(request, requestDto);
        final CompilationDto responseDto = compilationService.save(requestDto);
        logHttpResponse(request, responseDto);
        return responseDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id, final HttpServletRequest request) {
        logHttpRequest(request);
        compilationService.delete(id);
        logHttpResponse(request);
    }

    @PatchMapping("/{id}")
    public CompilationDto update(@PathVariable final long id,
            @RequestBody @Valid final UpdateCompilationRequest requestDto,
            final HttpServletRequest request) {
        logHttpRequest(request, requestDto);
        final CompilationDto responseDto = compilationService.update(id, requestDto);
        logHttpResponse(request, responseDto);
        return responseDto;
    }
}
