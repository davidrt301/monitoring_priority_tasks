package com.davidrt301.priority_tasks.config;

import com.davidrt301.priority_tasks.service.category.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de la inicialización de datos maestros al arrancar la aplicación.
 * Aplica el principio de Responsabilidad Única (SRP) al separar la carga de datos del resto de la lógica.
 */
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryService categoryService;

    @Override
    public void run(String... args) throws Exception {
        categoryService.seedDefaultCategories();
    }
}
