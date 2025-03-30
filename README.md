# Proyecto: Sistema de Gestión de Tareas

Este proyecto fue desarrollado como parte de una prueba técnica. Este proyecto consiste en un **Gestor de Tareas con JWT** es una aplicación basada en microservicios que permite gestionar tareas de manera eficiente y segura. Utiliza autenticación y autorización mediante JSON Web Tokens (JWT) para garantizar el acceso adecuado a los recursos.

<!-- TOC tocDepth:2..3 chapterDepth:2..6 -->
## Tabla de Contenido
- [Proyecto: Sistema de Gestión de Tareas](#proyecto-sistema-de-gestión-de-tareas)
  - [Tabla de Contenido](#tabla-de-contenido)
  - [Tecnologias Utilizadas](#tecnologias-utilizadas)
    - [Backend (Microservicios)](#backend-microservicios)
    - [FrontEnd](#frontend)
  - [Características principales](#características-principales)
  - [Diagrama: Arquitectura del proyecto](#diagrama-arquitectura-del-proyecto)
  - [Base de Datos](#base-de-datos)
    - [Microservicio Auth](#microservicio-auth)
    - [Microservicio Task](#microservicio-task)
  - [Cobertura de Codigo con JaCoCo](#cobertura-de-codigo-con-jacoco)
    - [Cómo ejecutar JaCoCo](#cómo-ejecutar-jacoco)
  - [Instalación](#instalación)
    - [Requisitos](#requisitos)
  - [Configuración](#configuración)
  - [Instalación y Ejecución](#instalación-y-ejecución)
  - [Endpoints principales](#endpoints-principales)
    - [Registro y Autenticación](#registro-y-autenticación)
    - [Gestión de Tareas](#gestión-de-tareas)

<!-- /TOC -->

## Tecnologias Utilizadas ##
### Backend (Microservicios) ###
* Spring Boot - Framework principal para el desarrollo de los microservicios.
* Spring Security - Implementación de seguridad y autenticación basada en JWT.
* Spring Data JPA - Interacción con bases de datos.
* MySQL - Base de datos relacional utilizada para persistencia.
* Hibernate - ORM para el manejo de entidades.
* Swagger OpenAPI - Documentación y prueba de API.
* Lombok - Reducción de código boilerplate.
* JUnit & Mockito - Pruebas unitarias.

### FrontEnd ###
* Angular - Framework para el desarrollo de la aplicación cliente.
* Bootstrap - Diseño moderno e interactivo.
* RxJS - Manejo de programación reactiva.
* JWT Helper - Manejo de tokens JWT en el cliente.
* HttpClient - Comunicación con los microservicios.

## Características principales 

- **Arquitectura Hexagonal**: El proyecto sigue el patrón de arquitectura hexagonal para promover una clara separación de responsabilidades y facilitar la mantenibilidad y escalabilidad.
- **Swagger**: El proyecto incluye integración con **Swagger** para generar la documentación de la API de manera automática. Puedes acceder a la interfaz de Swagger en la siguiente URL:
  * [Microservicio Auth: http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
  * [Microservicio Task: http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **JWT (JSON Web Token)**: El proyecto usa el estándar JWT, utilizado para la autenticación y autorización de usuarios en aplicaciones web y acceso a recursos de microservicios.
- **Jacoco**: JaCoCo (Java Code Coverage) es una herramienta para medir la cobertura de código en pruebas unitarias. Se ha integrado en este proyecto para asegurar la calidad del código y mejorar la detección de errores en la fase de pruebas. [más información aquí.](#cobertura-de-codigo-con-jacoco)
- **Angular y Bootstrap**: Para facilidad de manejo de componentes y responsive design

## Diagrama: Arquitectura del proyecto ##

![Diagrama del servicio](./recursos/Arquitectura.png)

## Base de Datos

Los modelos de la base de datos están representados en los siguientes diagramas:

### Microservicio Auth
![Microservicio Auth](./recursos/BD_Auth.png)
### Microservicio Task 
![Microservicio Task](./recursos/BD_Task.png)

## Cobertura de Codigo con JaCoCo

### Cómo ejecutar JaCoCo
Para generar el reporte de cobertura de código, ejecuta el siguiente comando:
```bash
cd ${Ruta proyecto deseado}
mvn clean verify
```
Este comando generará un informe en el proyecto en la ruta: `target/site/jacoco/index.html`, podra abrir el informe con el navegador web.

## Instalación
### Requisitos

- **Java 17 o superior**
- **MySQL** para la base de datos
- **Docker** para el despliegue

## Configuración

Antes de ejecutar la aplicación, si lo deseas, puedes modificar el archivo `.env` en la raíz de los proyectos Java, el cual debe contener la siguiente configuración:

```env
DB_NAME=${Nombre de la Base de Datos}
DB_HOST=${Servidor}
DB_PORT=${Puerto}
DB_USER=${Usuario}
DB_PASS=${Contraseña}
DB_EXTRA_PARAMS=?serverTimezone=UTC
JWT_SECRET=${Secret key de por lo menos 32 caracteres}$
```

## Instalación y Ejecución

```bash
git clone https://github.com/emartinz/Reditos_Prueba.git
cd Reditos_Prueba
docker compose --env-file .env -f docker.compose.yml up --build -d
```

## Endpoints principales

### Registro y Autenticación
- **POST** `/auth/register` → Registrar un nuevo usuario
Sirve para crear/registrar usuarios, por defecto la app genera datos de pruebas entre ellos los roles 1: USER, 2: ADMIN

   **Request:**
   ```json
   {
      "username": "user2",
      "password": "T3stP4$$",
      "roles": [
         {"id": 1}
      ]
   }
   ```

   **response:**
   ```json
   {
      "status": "SUCCESS",
      "message": "OK",
      "data": {
         "id": 3,
         "username": "user2",
         "roles": [
               {
                  "id": 1,
                  "name": null
               }
         ]
      }
   }
   ```

- **POST** `/auth/login` → Iniciar sesión y obtener token JWT.

   **Request:**
   ```json
   {
      "username": "admin",
      "password": "T3stP4$$"
   }
   ```
   **Response:** De aqui se obtiene el token que se usara para realizar las solicitudes de **Gestión de tareas**
   ```json
   {
      "status": "SUCCESS",
      "message": "OK",
      "data": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInVzZXJJZCI6Miwic3ViIjoidXNlciIsImlhdCI6MTc0MzMyMDAyMCwiZXhwIjoxNzQzMzIzNjIwfQ.tT_NeRV0f2cUDwvcpJCBCLfzRrm-TCVzK7Izx4BNHvA"
   }
   ```

### Gestión de Tareas

- **GET** `/api/tasks/getAll?page=0&size=10` → Obtener todas las tareas
  - **Parámetros de consulta:**
    - `page`: El número de página para la paginación.
    - `size`: El número de elementos por página.

  **Request:**
  - **Headers:**
    - `Authorization: Bearer {token}`

   ```json
   {
      "status": "SUCCESS",
      "message": "OK",
      "data": {
         "content": [
               {
                  "id": 1,
                  "title": "Hacer ejercicio",
                  "description": "Rutina de 30 minutos",
                  "status": "EN_PROGRESO",
                  "priority": "ALTA",
                  "dueDate": "2025-03-30",
                  "userDetails": {
                     "username": "user",
                     "email": "user@example.com",
                     "firstName": "Andres",
                     "lastName": "Zapata"
                  },
                  "createdAt": "2025-03-30T07:25:44.386933",
                  "updatedAt": "2025-03-30T07:25:44.386974"
               },
               {
                  "id": 2,
                  "title": "Leer un libro",
                  "description": "Capítulo 3 del libro actual",
                  "status": "PENDIENTE",
                  "priority": "MEDIA",
                  "dueDate": "2025-03-30",
                  "userDetails": {
                     "username": "user",
                     "email": "user@example.com",
                     "firstName": "Andres",
                     "lastName": "Zapata"
                  },
                  "createdAt": "2025-03-30T07:25:44.396089",
                  "updatedAt": "2025-03-30T07:25:44.396111"
               }
         ],
         "pageable": {
               "pageNumber": 0,
               "pageSize": 10,
               "sort": [],
               "offset": 0,
               "paged": true,
               "unpaged": false
         },
         "totalPages": 1,
         "totalElements": 2,
         "last": false,
         "size": 5,
         "number": 0,
         "sort": [],
         "numberOfElements": 2,
         "first": true,
         "empty": false
      }
   }
   ```
- **POST** `/api/tasks/create` → Crear una nueva tarea
   **Request:**
   - **Headers:** 
      - `Authorization: Bearer {token}`
   ```json
   {
      "title": "Nueva tarea",
      "description": "Descripción de la tarea",
      "status": "EN_PROGRESO",
      "priority": "ALTA",
      "dueDate": "2025-03-30"
   }
   ```

   **Response:**
   ```json
      {
         "status": "SUCCESS",
         "message": "Tarea creada",
         "data": {
            "id": 13,
            "title": "Nueva tarea",
            "description": "Descripción de la tarea",
            "status": "EN_PROGRESO",
            "priority": "ALTA",
            "dueDate": null,
            "userDetails": {
                  "username": "user",
                  "email": "user@example.com",
                  "firstName": "Andres",
                  "lastName": "Zapata"
            },
            "createdAt": "2025-03-30T07:49:13.236712",
            "updatedAt": "2025-03-30T07:49:13.236741"
         }
      }
      ```
- **PUT** `/api/tasks/{id}` → Actualizar una tarea
  **Request:**
  - **Headers:** 
    - `Authorization: Bearer {token}`
   ```json
   {
      "title": "Nueva tarea 3",
      "description": "Descripción de la tarea",
      "status": "EN_PROGRESO",
      "priority": "ALTA",
      "dueDate": null
   }
   ```
   **Response:**
   ```json
   {
      "status": "SUCCESS",
      "message": "Tarea actualizada",
      "data": {
         "id": 13,
         "title": "Nueva tarea 3",
         "description": "Descripción de la tarea",
         "status": "EN_PROGRESO",
         "priority": "ALTA",
         "dueDate": null,
         "userDetails": {
            "username": "user",
            "email": "user@example.com",
            "firstName": "Andres",
            "lastName": "Zapata"
         },
         "createdAt": "2025-03-30T07:49:13.236712",
         "updatedAt": "2025-03-30T07:59:42.855564"
      }
   }
   ```
- **DELETE** `/api/tasks/{id}` → Eliminar una tarea
   **Request:**
  - **Headers:** 
    - `Authorization: Bearer {token}`
   **Response:**
   ```json
   {
   "status": "SUCCESS",
   "message": "Tarea eliminada",
   "data": null
   }
   ```

