package com.davidrt301.priority_tasks.mappers;

import com.davidrt301.priority_tasks.model.dtos.TaskRequest;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;
import com.davidrt301.priority_tasks.model.entities.Category;
import com.davidrt301.priority_tasks.model.entities.Priority;
import com.davidrt301.priority_tasks.model.entities.Task;
import com.davidrt301.priority_tasks.model.entities.User;
import com.davidrt301.priority_tasks.service.PriorityRuleEngine;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class TaskMapper {

    @Autowired
    protected PriorityRuleEngine priorityRuleEngine;

    // Mapeo de Creación: Request + Entidades relacionadas -> Entidad Final
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "priority", ignore = true) // Se calcula en AfterMapping
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")//Nos permite ejecutar código Java puro durante el mapeo. Aquí aseguramos que cada tarea nueva nazca con la fecha y hora actual del servidor.
    @Mapping(target = "completed", constant = "false")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "user", source = "user")//Aquí le decimos explícitamente a MapStruct que los objetos category y user que pasamos como parámetros deben asignarse a los atributos correspondientes de la entidad Task.
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "description", source = "request.description")
    public abstract Task toEntity(TaskRequest request, Category category, User user);

    // Mapeo de Actualización: Actualiza la instancia existente sin crear una nueva
    @Mapping(target = "id", ignore = true)//Ignoramos id, creationDate y completed para que la actualización no borre los datos originales que no deben cambiar.
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "user", source = "user")
    public abstract void updateEntity(TaskRequest request, Category category, User user, @MappingTarget Task task);//@MappingTarget: Esta es la palabra clave más importante aquí. Le dice a MapStruct que no cree un objeto nuevo, sino que tome la instancia task (que ya existe en la DB) y la actualice con los nuevos datos.

    // Mapeo de Salida: Aplana la estructura para el cliente
    @Mapping(target = "categoryName", source = "category.name")//En la Entidad tenemos objetos completos (Category), pero el cliente solo quiere ver el nombre en un String. MapStruct navega por el objeto (category.name) y lo extrae automáticamente. Esto cumple con el Principio de Mínimo Conocimiento: el cliente no necesita conocer toda la estructura de la categoría, solo su nombre.
    @Mapping(target = "username", source = "user.username")
    public abstract TaskResponse toResponse(Task task);

    @AfterMapping
    protected void calculatePriority(TaskRequest request, @MappingTarget Task task) {
        Priority priority = priorityRuleEngine.calculatePriority(
                request.expirationDate().toLocalDate(),
                request.complexity()
        );
        task.setPriority(priority);
    }
}
/* 
@AfterMapping: Se ejecuta justo después de que MapStruct termina de copiar los campos simples.
Es el lugar perfecto para la lógica que requiere cálculos. Aquí usamos el priorityRuleEngine inyectado para decidir si la tarea es URGENT, HIGH, etc., y seteamos el resultado en la entidad antes de que el método termine.
*/