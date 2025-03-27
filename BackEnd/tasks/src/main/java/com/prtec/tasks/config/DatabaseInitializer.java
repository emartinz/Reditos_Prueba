package com.prtec.tasks.config;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.prtec.tasks.domain.model.entity.Task;
import com.prtec.tasks.domain.model.entity.UserDetails;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Esta clase inicializa y carga datos en la base de datos
 * durante la fase de arranque de la aplicación.
 *
 * <p>Se recomienda utilizarla solo en entornos de desarrollo o pruebas, y no en producción.</p>
 *
 * @author Edgar Andres
 * @version 1.2
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final EntityManager entityManager;

    public DatabaseInitializer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // Buscar usuarios en la base de datos
            UserDetails admin = entityManager.find(UserDetails.class, 1L);
            if (admin == null) {
                admin = new UserDetails();
                admin.setUserId(1L);
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setFirstName("Edgar");
                admin.setLastName("Martinez");
                entityManager.persist(admin);
            } else {
                admin = entityManager.merge(admin); // Convertir en entidad gestionada
            }

            UserDetails user = entityManager.find(UserDetails.class, 2L);
            if (user == null) {
                user = new UserDetails();
                user.setUserId(2L);
                user.setUsername("user");
                user.setEmail("user@example.com");
                user.setFirstName("Andres");
                user.setLastName("Zapata");
                entityManager.persist(user);
            } else {
                user = entityManager.merge(user);
            }

            // Asegurar que user y admin son entidades gestionadas
            admin = entityManager.merge(admin);
            user = entityManager.merge(user);

            // Lista de tareas
            List<Task> tasks = List.of(
                new Task("Hacer ejercicio", "Rutina de 30 minutos", Task.TaskStatus.EN_PROGRESO, Task.TaskPriority.ALTA, LocalDate.now(), user),
                new Task("Leer un libro", "Capítulo 3 del libro actual", Task.TaskStatus.PENDIENTE, Task.TaskPriority.MEDIA, LocalDate.now(), user),
                new Task("Preparar el desayuno", "Tostadas con café", Task.TaskStatus.COMPLETADA, Task.TaskPriority.ALTA, LocalDate.now(), user),
                new Task("Revisar correos", "Responder correos pendientes", Task.TaskStatus.PENDIENTE, Task.TaskPriority.BAJA, LocalDate.now(), user),
                new Task("Hacer compras", "Lista: leche, pan, huevos", Task.TaskStatus.EN_PROGRESO, Task.TaskPriority.ALTA, LocalDate.now(), user),
                new Task("Llamar a un amigo", "Conversar sobre el proyecto", Task.TaskStatus.PENDIENTE, Task.TaskPriority.MEDIA, LocalDate.now(), user),
                new Task("Organizar documentos", "Clasificar archivos importantes", Task.TaskStatus.COMPLETADA, Task.TaskPriority.BAJA, LocalDate.now(), user),
                new Task("Cocinar la cena", "Preparar una ensalada saludable", Task.TaskStatus.EN_PROGRESO, Task.TaskPriority.MEDIA, LocalDate.now(), user),
                new Task("Estudiar programación", "Repasar patrones de diseño", Task.TaskStatus.PENDIENTE, Task.TaskPriority.ALTA, LocalDate.now(), user),
                new Task("Ver una película", "Seleccionar película para la noche", Task.TaskStatus.PENDIENTE, Task.TaskPriority.BAJA, LocalDate.now(), user),

                new Task("Revisar copias de seguridad", "Ordenar habitación", Task.TaskStatus.COMPLETADA, Task.TaskPriority.BAJA, LocalDate.now(), admin),
                new Task("Reunión", "Revisar agenda y prioridades", Task.TaskStatus.PENDIENTE, Task.TaskPriority.ALTA, LocalDate.now(), admin)
            );

            // Guardar tareas en la base de datos
            tasks.forEach(task -> entityManager.persist(task));

            logger.info("Datos iniciales cargados correctamente.");
        } catch (Exception e) {
            logger.error("Error al inicializar la base de datos: {}", e.getMessage(), e);
        }
    }
}