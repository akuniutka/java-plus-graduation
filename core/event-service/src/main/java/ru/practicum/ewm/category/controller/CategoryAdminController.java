package ru.practicum.ewm.category.controller;

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
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.dto.CategoryCreateDto;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.model.CategoryPatch;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.category.dto.CategoryUpdateDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final CategoryService service;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@RequestBody @Valid final CategoryCreateDto dtoIn) {
        log.info("Received request to add new category: name = {}", dtoIn.name());
        log.debug("New category = {}", dtoIn);
        Category category = categoryMapper.mapToCategory(dtoIn);
        category = service.save(category);
        final CategoryDto dtoOut = categoryMapper.mapToDto(category);
        log.info("Responded with category added: id = {}. name = {}", dtoOut.id(), dtoOut.name());
        log.debug("Category added = {}", dtoOut);
        return dtoOut;
    }

    @PatchMapping("/{id}")
    public CategoryDto update(@PathVariable final long id, @RequestBody @Valid final CategoryUpdateDto dtoIn) {
        log.info("Received request to update category: id = {}", id);
        log.debug("Category patch = {}", dtoIn);
        final CategoryPatch patch = categoryMapper.mapToCategoryPatch(dtoIn);
        final Category category = service.update(id, patch);
        final CategoryDto dtoOut = categoryMapper.mapToDto(category);
        log.info("Responded with updated category: id = {}", dtoOut.id());
        log.debug("Updated category = {}", dtoOut);
        return dtoOut;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        log.info("Received request to delete category: id = {}", id);
        service.deleteById(id);
        log.info("Responded with {} to category delete request: id = {}", HttpStatus.NO_CONTENT, id);
    }
}
