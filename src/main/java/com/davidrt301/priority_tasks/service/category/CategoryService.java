package com.davidrt301.priority_tasks.service.category;

import com.davidrt301.priority_tasks.model.dtos.CategoryRequest;
import com.davidrt301.priority_tasks.model.dtos.CategoryResponse;
import java.util.List;

public interface CategoryService {
    
    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    CategoryResponse findById(Long id);

    List<CategoryResponse> findAll();

    void delete(Long id);

    void seedDefaultCategories();//categorías predeterminadas
}
//declara los métodos que representan la lógica de negocio que el servicio debe ofrecer. Es el "contrato" o la "promesa" de lo que el servicio puede hacer.