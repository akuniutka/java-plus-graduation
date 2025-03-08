package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.CategoryPatch;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public Category save(final Category category) {
        final Category savedCategory = repository.save(category);
        log.info("Added new category: id = {}, name = {}", savedCategory.getId(), savedCategory.getName());
        log.debug("Category added = {}", savedCategory);
        return savedCategory;
    }

    @Override
    public List<Category> findAll(final Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }

    @Override
    public Category getById(final long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Category.class, id));
    }

    @Override
    public Category update(final long id, final CategoryPatch patch) {
        final Category category = getById(id);
        applyPatch(category, patch);
        final Category savedCategory = repository.save(category);
        log.info("Updated category: id = {}", savedCategory.getId());
        log.debug("Updated category = {}", savedCategory);
        return savedCategory;
    }

    @Override
    @Transactional
    public void deleteById(final long id) {
        if (repository.delete(id) == 1) {
            log.info("Deleted category: id = {}", id);
        }
    }

    private void applyPatch(final Category category, final CategoryPatch patch) {
        Optional.ofNullable(patch.name()).ifPresent(category::setName);
    }
}
