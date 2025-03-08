package ru.practicum.ewm.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.CategoryPatch;

import java.util.List;

public interface CategoryService {

    Category save(Category category);

    List<Category> findAll(Pageable pageable);

    Category getById(long id);

    Category update(long id, CategoryPatch patch);

    void deleteById(long id);
}
