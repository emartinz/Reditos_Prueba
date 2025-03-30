#!/bin/sh

# Sobreescribe el index.html para inyectar las variables en el <head>
sed -i "s|<!-- ENV_VARS -->|<script>window.AUTH_URL='${AUTH_URL}'; window.TASK_URL='${TASK_URL}';</script>|g" /usr/share/nginx/html/index.html

# Arranca Nginx
exec "$@"