package com.prtec.auth.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            // Crear roles si no existen
            if (entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'USER'").getResultList().isEmpty()) {
                entityManager.persist(new Role("USER"));
            }
            if (entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'ADMIN'").getResultList().isEmpty()) {
                entityManager.persist(new Role("ADMIN"));
            }
            logger.info("Validacion o creacion de roles base, completados.");

            // Verificar si el usuario admin existe
            if (entityManager.createQuery("SELECT u FROM User u WHERE u.username = 'admin'").getResultList().isEmpty()) {
                Role userRole = (Role) entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'USER'").getSingleResult();
                Role adminRole = (Role) entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'ADMIN'").getSingleResult();

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("$2a$10$S1PRLXpqzlxPU.qWz83OAufKK8d3nc6UEjTRzLJdMWZflNw7UIPVW"); // T3stP4$$
                admin.setRoles(Arrays.asList(userRole, adminRole));

                User user = new User();
                user.setUsername("user");
                user.setPassword("$2a$10$S1PRLXpqzlxPU.qWz83OAufKK8d3nc6UEjTRzLJdMWZflNw7UIPVW"); // T3stP4$$
                user.setRoles(Arrays.asList(userRole));
                
                entityManager.persist(admin);
                entityManager.persist(user);
                logger.info("Usuarios creado.");
            }

            logger.info("Datos iniciales cargados correctamente.");
        } catch (Exception e) {
            logger.error("Error al inicializar la base de datos: {}", e.getMessage(), e);
        }
    }
}