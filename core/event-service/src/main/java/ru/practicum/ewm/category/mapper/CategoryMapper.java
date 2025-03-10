package ru.practicum.ewm.category.mapper;

import ru.practicum.ewm.category.dto.CategoryCreateDto;
import ru.practicum.ewm.category.dto.CategoryUpdateDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.model.CategoryPatch;

import java.util.List;

public interface CategoryMapper {

    Category mapToCategory(CategoryCreateDto dto);

    Category mapToCategory(Long id);

    CategoryPatch mapToCategoryPatch(CategoryUpdateDto dto);

    CategoryDto mapToDto(Category category);

    List<CategoryDto> mapToDto(List<Category> categories);
}
