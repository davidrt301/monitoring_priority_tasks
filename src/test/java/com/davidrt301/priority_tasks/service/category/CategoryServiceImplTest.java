package com.davidrt301.priority_tasks.service.category;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.davidrt301.priority_tasks.exceptions.BusinessException;
import com.davidrt301.priority_tasks.exceptions.ResourceNotFoundException;
import com.davidrt301.priority_tasks.mappers.CategoryMapper;
import com.davidrt301.priority_tasks.model.dtos.CategoryRequest;
import com.davidrt301.priority_tasks.model.dtos.CategoryResponse;
import com.davidrt301.priority_tasks.model.entities.Category;
import com.davidrt301.priority_tasks.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    private static final Long CAT_ID_1 = 1L;
    private static final Long CAT_ID_2 = 2L;
    private static final String CAT_NAME_1 = "Trabajo";
    private static final String CAT_NAME_2 = "Familia";
    private static final String UPDATED_NAME = "Trabajo Actualizado";
    private static final String EXISTING_OTHER_NAME = "Estudio";

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void init(){
        category = createTestCategory(CAT_ID_1, CAT_NAME_1);
        categoryRequest = new CategoryRequest(CAT_NAME_1);
        categoryResponse = createTestResponse(CAT_ID_1, CAT_NAME_1);
    }

    @Nested
    @DisplayName("Pruebas de Creación")
    class CreateTests {
        @Test
        @DisplayName("Debe crear una categoría con éxito")
        void shouldCreateCategory_WhenCorrectData(){
            when(categoryRepository.existsByName(anyString())).thenReturn(false);
            when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(category);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            CategoryResponse result = categoryService.create(categoryRequest);

            assertAll("Verificación de Creación",
                () -> assertNotNull(result),
                () -> assertEquals(CAT_NAME_1, result.name()),
                () -> verify(categoryRepository).save(any(Category.class))
            );
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando la categoría ya existe")
        void shouldThrowException_WhenCategoryExists() {
            when(categoryRepository.existsByName(CAT_NAME_1)).thenReturn(true);

            assertThrows(BusinessException.class, () -> categoryService.create(categoryRequest));
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda")
    class FindTests {
        @Test
        @DisplayName("Debe retornar una categoría por ID")
        void shouldReturnCategory_WhenIdExists() {
            when(categoryRepository.findById(CAT_ID_1)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

            CategoryResponse result = categoryService.findById(CAT_ID_1);

            assertAll("Verificación de Búsqueda",
                () -> assertNotNull(result),
                () -> assertEquals(CAT_ID_1, result.id())
            );
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no existe")
        void shouldThrowException_WhenIdNotFound() {
            when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(CAT_ID_1));
        }

        @Test
        @DisplayName("Debe retornar una lista de categorias")
        void shouldReturnAllCategories() {
            Category cat2 = createTestCategory(CAT_ID_2, CAT_NAME_2);
            CategoryResponse res2 = createTestResponse(CAT_ID_2, CAT_NAME_2);

            when(categoryRepository.findAll()).thenReturn(List.of(category, cat2));
            when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);
            when(categoryMapper.toResponse(cat2)).thenReturn(res2);

            List<CategoryResponse> result = categoryService.findAll();   
        
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Pruebas de Actualización")
    class UpdateTests {
        @Test
        @DisplayName("Debe actualizar una categoría si existe")
        void shouldUpdateCategory_WhenExists() {
            CategoryRequest updateReq = new CategoryRequest(UPDATED_NAME);
            CategoryResponse updatedRes = createTestResponse(CAT_ID_1, UPDATED_NAME);

            when(categoryRepository.findById(CAT_ID_1)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(updatedRes);

            CategoryResponse result = categoryService.update(CAT_ID_1, updateReq);

            assertEquals(UPDATED_NAME, result.name());
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Debe fallar al actualizar con un nombre que ya pertenece a otra categoría")
        void shouldThrowException_WhenNameAlreadyExistsForOther() {
            CategoryRequest updateReq = new CategoryRequest(EXISTING_OTHER_NAME);
            
            when(categoryRepository.findById(CAT_ID_1)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName(EXISTING_OTHER_NAME)).thenReturn(true);

            assertThrows(BusinessException.class, () -> categoryService.update(CAT_ID_1, updateReq));
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Eliminación")
    class DeleteTests {
        @Test
        @DisplayName("Debe eliminar una categoría si existe")
        void shouldDeleteCategory_WhenExists() {
            when(categoryRepository.findById(CAT_ID_1)).thenReturn(Optional.of(category));
            categoryService.delete(CAT_ID_1);
            verify(categoryRepository).deleteById(CAT_ID_1);
        }
    }

    // --- HELPERS ---

    private Category createTestCategory(Long id, String name) {
        Category cat = new Category();
        cat.setId(id);
        cat.setName(name);
        return cat;
    }

    private CategoryResponse createTestResponse(Long id, String name) {
        return new CategoryResponse(id, name);
    }
}
