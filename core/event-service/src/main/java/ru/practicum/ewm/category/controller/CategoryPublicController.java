package ru.practicum.ewm.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryPublicController {

    private final CategoryService service;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryDto> findAll(
            @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
            @RequestParam(defaultValue = "10") @Positive final int size
    ) {
        log.info("Received request for categories: from = {}, size = {}", from, size);
        final Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        final List<Category> categories = service.findAll(pageable);
        final List<CategoryDto> dtos = categoryMapper.mapToDto(categories);
        log.info("Responded with requested categories: from = {}, size = {}", from, size);
        log.debug("Requested categories = {}", dtos);
        return dtos;
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable final long id) {
        log.info("Received request for category: id = {}", id);
        final Category category = service.getById(id);
        final CategoryDto dto = categoryMapper.mapToDto(category);
        log.info("Responded with requested category: id = {}", dto.id());
        log.debug("Requested category = {}", dto);
        return dto;
    }
}
