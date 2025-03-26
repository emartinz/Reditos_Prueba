package com.prtec.auth.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.prtec.auth.domain.model.entities.Role;
import com.prtec.auth.domain.model.entities.User;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Esta clase inicializa y carga datos en la base de datos
 * durante la fase de arranque de la aplicación.
 *
 * <p>Esta clase puede ser utilizada para:
 * <ul>
 *     <li>Crear datos de prueba para entornos de desarrollo o pruebas.</li>
 *     <li>Configurar datos predeterminados como roles, usuarios o configuraciones.</li>
 *     <li>Realizar migraciones o actualizaciones de datos necesarias.</li>
 * </ul>
 *
 * <p>Se recomienda utilizarla solo en entornos de desarrollo o pruebas, y no en producción.</p>
 *
 * @author Edgar Andres
 * @version 1.0
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final EntityManager entityManager;

    public DatabaseInitializer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Crear roles si no existen
        if (entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'USER'").getResultList().isEmpty()) {
            entityManager.persist(new Role("USER"));
        }
        if (entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'ADMIN'").getResultList().isEmpty()) {
            entityManager.persist(new Role("ADMIN"));
        }

        // Verificar si el usuario admin existe
        if (entityManager.createQuery("SELECT u FROM User u WHERE u.username = 'admin'").getResultList().isEmpty()) {
            Role userRole = (Role) entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'USER'").getSingleResult();
            Role adminRole = (Role) entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'ADMIN'").getSingleResult();

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("$2a$10$S1PRLXpqzlxPU.qWz83OAufKK8d3nc6UEjTRzLJdMWZflNw7UIPVW"); // T3stP4$$
            admin.setRoles(Arrays.asList(userRole, adminRole));

            entityManager.persist(admin);
        }
    }
}