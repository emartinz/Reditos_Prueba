# Etapa 1: Construcción de la aplicación Angular
FROM node:18 AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar package.json y package-lock.json antes de instalar dependencias
COPY package*.json ./

# Instalar dependencias (sin generar archivos innecesarios)
RUN npm ci

# Copiar el resto de la aplicación
COPY . .

# Reemplazar las variables en constants.ts antes de compilar
ARG AUTH_URL
ARG TASK_URL

RUN sed -i "s|urlAuth:\s*[^,]*|urlAuth: '${AUTH_URL}'|" src/app/config/constants.ts && \
	sed -i "s|urlTask:\s*[^,]*|urlTask: '${TASK_URL}'|" src/app/config/constants.ts

# Construir la aplicación en modo producción
RUN npm run build --prod

# Use Nginx to serve the built Angular app
FROM nginx:alpine

# Copiar la configuración personalizada de Nginx
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# Copiar la aplicación Angular compilada al directorio de Nginx
COPY --from=0 /app/dist/task-manager/browser /usr/share/nginx/html

# Expose port for Angular app
EXPOSE 80

# Start the Nginx server
ENTRYPOINT [ "nginx" ]
CMD [ "-g", "daemon off;" ]