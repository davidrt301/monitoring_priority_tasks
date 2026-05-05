package com.davidrt301.priority_tasks.service.category;

import com.davidrt301.priority_tasks.exceptions.BusinessException;
import com.davidrt301.priority_tasks.exceptions.ResourceNotFoundException;
import com.davidrt301.priority_tasks.mappers.CategoryMapper;
import com.davidrt301.priority_tasks.model.dtos.CategoryRequest;
import com.davidrt301.priority_tasks.model.dtos.CategoryResponse;
import com.davidrt301.priority_tasks.repository.CategoryRepository;
import com.davidrt301.priority_tasks.model.entities.Category;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {//contiene el código real que ejecuta esa lógica de negocio. Es la "realización" de la promesa.

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException("La categoría ya existe: " + request.name());
        }
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new BusinessException("Ya existe otra categoría con el nombre: " + request.name());
        }

        category.setName(request.name());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void seedDefaultCategories() {
        List<String> defaultCategories = List.of(
            "Trabajo", "Estudio", "Desarrollo", "Salud", 
            "Finanzas", "Personal", "Hogar"
        );

        defaultCategories.stream()
            .filter(name -> !categoryRepository.existsByName(name))
            .forEach(this::saveCategory);//.forEach(name -> saveCategory(name));   
    }
    
    private void saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
    }
}
