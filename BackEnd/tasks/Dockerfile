FROM openjdk:17-jdk-slim

# Directorio de trabajo
WORKDIR /app

ENV APP_NAME=tasks

# Copiar archivos necesarios
COPY .env /app/.env
COPY target/${APP_NAME}-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto 8081 (aplicación) y 5006 (depuración)
EXPOSE 8081 5005

# Establecer las variables de entorno para la depuración remota
ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006"

# Ejecutar el comando Java con JAVA_OPTS
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]