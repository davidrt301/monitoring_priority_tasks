package com.davidrt301.priority_tasks.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import com.davidrt301.priority_tasks.model.entities.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t JOIN FETCH t.user WHERE t.id = :id")
    Optional<Task> findWithUserById(@Param("id") Long id);

    /*
     * Qué hace: Recupera todas las tareas que pertenecen a un usuario específico.
     * Lógica: Busca en la tabla de tareas todas aquellas donde la columna de
     * relación user_id coincida con el ID proporcionado.
     * SQL equivalente: SELECT * FROM tasks WHERE user_id = ?;
     */
    // Filtrado optimizado en base de datos (SQL)
    List<Task> findByUserId(Long userId);

    /*
     * Qué hace: Filtra las tareas de un usuario que además pertenecen a una
     * categoría específica.
     * Lógica: Es una consulta de conjunción (AND). Es muy útil para vistas donde el
     * usuario quiere ver, por ejemplo, solo sus tareas de "Trabajo" o "Hogar".
     * SQL equivalente: SELECT * FROM tasks WHERE user_id = ? AND category_id = ?;
     */
    List<Task> findByUserIdAndCategoryId(Long userId, Long categoryId);

    /*
     * Qué hace: Busca las tareas vencidas de un usuario en particular.
     * Lógica: Aplica tres filtros estrictos:
     * Que la tarea pertenezca al usuario (UserId).
     * Que la tarea no esté terminada (CompletedFalse).
     * Que la fecha de vencimiento sea anterior a la hora actual
     * (ExpirationDateBefore).
     * Clean Code Tip: Nota que pasas LocalDateTime now como parámetro. Esto es
     * excelente para la testabilidad, ya que permite simular "el ahora" en tus
     * pruebas unitarias sin depender del reloj del sistema.
     * SQL equivalente: SELECT * FROM tasks WHERE user_id = ? AND completed = false
     * AND due_date < ?;
     */
    List<Task> findByUserIdAndCompletedFalseAndExpirationDateBefore(Long userId, LocalDateTime now);

    /*
     * Qué hace: Recupera todas las tareas vencidas de todo el sistema, sin filtrar
     * por usuario.
     * Lógica: Es similar al anterior, pero global. Es ideal para procesos
     * automáticos de notificación o tableros de administración que necesiten ver
     * qué tareas están pendientes y fuera de plazo en toda la aplicación.
     * SQL equivalente: SELECT * FROM tasks WHERE completed = false AND due_date <
     * ?;
     */
    List<Task> findByCompletedFalseAndExpirationDateBefore(LocalDateTime now);
}
