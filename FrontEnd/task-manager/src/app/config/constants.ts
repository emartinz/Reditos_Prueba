// URLs base de los microservicios
const urlAuth = 'http://localhost:8080';
const urlTask = 'http://localhost:8081';

export const constants = {
    // URLs base de los microservicios
    urlAuth,
    urlTask,
    
    // Endpoints del servicio de autenticación
    auth: {
        baseUrl: `${urlAuth}/api`,
        login: `${urlAuth}/api/login`,
        register: `${urlAuth}/api/register`,
        refreshToken: `${urlAuth}/api/refresh-token`
    },
    
    // Endpoints del servicio de tareas
    task: {
        baseUrl: `${urlTask}/api`,
        tasks: `${urlTask}/api/tasks`,
        user: `${urlTask}/api/user`
    }
};