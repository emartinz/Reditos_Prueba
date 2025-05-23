package com.prtec.tasks;

import com.prtec.tasks.adapter.in.controller.TaskController;
import com.prtec.tasks.application.service.TaskService;
import com.prtec.tasks.application.service.UserDetailsService;
import com.prtec.tasks.application.utils.AuthUtils;
import com.prtec.tasks.application.utils.JwtUtil;
import com.prtec.tasks.domain.model.entity.Task;
import com.prtec.tasks.domain.model.entity.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"custom.security.public-paths=/v2/api-docs/**,/v3/api-docs/**,/swagger-resources/**,/swagger-ui/**,/webjars/**" })
class TaskControllerTest {
	private static final String STR_VALID_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJBRE1JTiIsIlVTRVIiXSwidXNlcklkIjoxLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc0MzE3NTYzMSwiZXhwIjoxNzc0NzExNjMxfQ.pCsYxqdqrL8AUu5_4ryahzTpr1jZXRRiRmP8IPsRPzI";
	private static final String STR_INVALID_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2Mzk4OCwiZXhwIjoxNzc0Njk5OTg4fQ.pSSy9NZwFZ_EcsE26E5ziD4z-rp3qEAQ92BI11qlbOI";

	@InjectMocks
	private TaskController taskController;

	@Mock
	private TaskService taskService;

	@Mock
	private UserDetailsService userDetailsService;

	@Spy
	private JwtUtil jwtUtil = new JwtUtil();

	@Spy
	private AuthUtils authUtils = new AuthUtils(jwtUtil);

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
	}

	@Test
	void testCreateTask_Success() throws Exception {
		Task task = new Task();
		task.setTitle("New Task");
		task.setDescription("Task Description");

		UserDetails userDetails = new UserDetails();
		userDetails.setId(3L);
		userDetails.setUsername("testuser");
		task.setUserDetails(userDetails);

		doReturn(userDetails.getId()).when(jwtUtil).getUserIdFromToken(anyString());
		doReturn(userDetails.getUsername()).when(jwtUtil).getUsernameFromToken(anyString());
		when(userDetailsService.findOrCreateUser(anyLong(), anyString())).thenReturn(userDetails);
		when(taskService.createTask(any())).thenReturn(task);

		mockMvc.perform(post("/api/tasks/create")
				.header("Authorization", STR_VALID_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\": \"New Task\", \"description\": \"Task Description\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("Tarea creada"))
				.andExpect(jsonPath("$.data.title").value("New Task"));
	}

	@Test
	void testGetAllTasksForUser_Success() throws Exception {
		Task task = new Task();
		task.setTitle("Task 1");

		Pageable pageable = PageRequest.of(0, 10);
		doReturn(true).when(authUtils).hasRole(anyString(), anyString());
		doReturn("testuser").when(jwtUtil).getUsernameFromToken(anyString());
		when(taskService.getTasksByUser(anyString(), any()))
				.thenReturn(new PageImpl<>(new ArrayList<>(List.of(task)), pageable, 1));

		mockMvc.perform(get("/api/tasks/getAll")
				.header("Authorization", STR_VALID_TOKEN)
				.param("page", "0")
				.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("OK"))
				.andExpect(jsonPath("$.data.content[0].title").value("Task 1"));
	}

	@Test
	void testCreateTask_Unauthorized() throws Exception {
		// Configurar el mock para que devuelva un error 401
		doReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado"))
				.when(authUtils).getTokenFromAuthHeader(anyString());

		mockMvc.perform(post("/api/tasks/create")
				.header("Authorization", STR_INVALID_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\": \"New Task\", \"description\": \"Task Description\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("No autorizado"));
	}

	@Test
	void testGetAllTasksForUser_Forbidden() throws Exception {
		when(jwtUtil.getUsernameFromToken(STR_INVALID_TOKEN)).thenReturn("invaliduser");
		when(taskService.getTasksByUser(anyString(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/tasks/getAll")
				.header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("No tienes permiso para acceder a esta acción"));
	}

	@Test
	void testUpdateTask_Success() throws Exception {
		Task task = new Task();
		task.setTitle("Task 1");
		task.setDescription("Updated task description");

		doReturn(true).when(authUtils)
				.isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class));

		doReturn(List.of(new SimpleGrantedAuthority("USER"))).when(jwtUtil)
				.getRolesFromToken(STR_VALID_TOKEN);

		when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(task);

		mockMvc.perform(put("/api/tasks/{id}", 1L)
				.header("Authorization", STR_VALID_TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\": \"Task 1\", \"description\": \"Updated task description\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("Tarea actualizada"))
				.andExpect(jsonPath("$.data.title").value("Task 1"))
				.andExpect(jsonPath("$.data.description").value("Updated task description"));
	}

	@Test
	void testUpdateTask_Unauthorized() throws Exception {
		Task task = new Task();
		task.setTitle("Task 1");

		String authHeader = STR_INVALID_TOKEN;

		doReturn(false).when(authUtils)
				.isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), anyBoolean());

		mockMvc.perform(put("/api/tasks/{id}", 1L)
				.header("Authorization", authHeader)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\": \"Task 1\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("No autorizado para actualizar esta tarea"));
	}

	@Test
	void testDeleteTask_Success() throws Exception {
		String authHeader = STR_VALID_TOKEN;

		doReturn(true).when(authUtils)
				.isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), eq(true));

		when(taskService.deleteTask(anyLong())).thenReturn(true);

		mockMvc.perform(delete("/api/tasks/{id}", 1L)
				.header("Authorization", authHeader))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("Tarea eliminada"));
	}

	@Test
	void testDeleteTask_Unauthorized() throws Exception {
		String authHeader = STR_INVALID_TOKEN;

		doReturn(false).when(authUtils)
				.isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), eq(true));

		mockMvc.perform(delete("/api/tasks/{id}", 1L)
				.header("Authorization", authHeader))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("No autorizado para borrar esta tarea"));
	}

	@Test
	void testDeleteTask_NotFound() throws Exception {
		String authHeader = STR_VALID_TOKEN;

		doReturn(true)
				.when(authUtils).isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), eq(true));
		when(taskService.deleteTask(anyLong())).thenReturn(false);

		mockMvc.perform(delete("/api/tasks/{id}", 1L)
				.header("Authorization", authHeader))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("Tarea no encontrada"));
	}

	@Test
	void testGetAllTasksForAdmin_Success() throws Exception {
		Task task = new Task();
		task.setTitle("Task 1");

		String authHeader = STR_VALID_TOKEN;

		doReturn(true).when(authUtils).isAdminUser(anyString());
		doReturn(List.of(new SimpleGrantedAuthority("ADMIN"))).when(jwtUtil).getRolesFromToken(authHeader);

		Pageable pageable = PageRequest.of(0, 10);
		when(taskService.getAllTasks(any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>(List.of(task)), pageable, 1));

		mockMvc.perform(get("/api/tasks/admin/getAll")
				.header("Authorization", authHeader)
				.param("page", "0")
				.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("Lista de tareas"))
				.andExpect(jsonPath("$.data.content[0].title").value("Task 1"));
	}

	@Test
	void testGetAllTasksForAdmin_Unauthorized() throws Exception {
		String authHeader = STR_VALID_TOKEN;

		doReturn(false).when(authUtils).isAdminUser(anyString());

		mockMvc.perform(get("/api/tasks/admin/getAll")
				.header("Authorization", authHeader)
				.param("page", "0")
				.param("size", "10"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("No tienes permiso para acceder a esta acción"));
	}

	@Test
	void testGetTaskById_Success() throws Exception {
		Long taskId = 1L;
		String authHeader = STR_VALID_TOKEN;

		Task task = new Task();
		task.setId(taskId);
		task.setTitle("Test Task");

		doReturn(true)
				.when(authUtils).isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), eq(true));

		when(taskService.getTaskById(anyLong())).thenReturn(task);

		mockMvc.perform(get("/api/tasks/{id}", taskId)
				.header("Authorization", authHeader))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("Tarea encontrada"))
				.andExpect(jsonPath("$.data.id").value(taskId))
				.andExpect(jsonPath("$.data.title").value("Test Task"));
	}

	@Test
	void testGetTaskById_Unauthorized() throws Exception {
		Long taskId = 1L;
		String authHeader = STR_INVALID_TOKEN;

		doReturn(false)
				.when(authUtils).isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), eq(true));

		mockMvc.perform(get("/api/tasks/{id}", taskId)
				.header("Authorization", authHeader))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("No autorizado para ver esta tarea"));
	}

	@Test
	void testGetTaskById_NotFound() throws Exception {
		Long taskId = 1L;
		String authHeader = STR_VALID_TOKEN;

		doReturn(true)
				.when(authUtils).isTaskOwnedByUser(anyString(), anyLong(), any(TaskService.class), eq(true));

		when(taskService.getTaskById(anyLong())).thenReturn(null);

		mockMvc.perform(get("/api/tasks/{id}", taskId)
				.header("Authorization", authHeader))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("Tarea no encontrada"));
	}

	@Test
	void testGetTasksFiltered_Success() throws Exception {
		String authHeader = STR_VALID_TOKEN;
		String status = Task.TaskStatus.EN_PROGRESO.toString();
		String priority = Task.TaskPriority.ALTA.toString();
		String title = "Test Task";
		int page = 0;
		int size = 10;

		Task task = new Task();
		task.setTitle("Test Task");

		doReturn(1L).when(jwtUtil).getUserIdFromToken(anyString());
		doReturn(ResponseEntity.ok("token")).when(authUtils).getTokenFromAuthHeader(anyString());

		Pageable pageable = PageRequest.of(0, 10);
		when(taskService.getTasksByFilters(anyLong(), eq(title), eq(status), eq(priority), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>(List.of(task)), pageable, 1));

		mockMvc.perform(get("/api/tasks/filter")
				.header("Authorization", authHeader)
				.param("status", status)
				.param("priority", priority)
				.param("title", title)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.message").value("Lista de tareas filtradas"))
				.andExpect(jsonPath("$.data.content[0].title").value("Test Task"));
	}

	@Test
	void testGetTasksFiltered_Unauthorized() throws Exception {
		String authHeader = "Bear invalid-token";

		doReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token no autorizado."))
				.when(authUtils).getTokenFromAuthHeader(anyString());

		mockMvc.perform(get("/api/tasks/filter")
				.header("Authorization", authHeader)
				.param("status", "In Progress")
				.param("priority", "High")
				.param("title", "Test Task")
				.param("page", "0")
				.param("size", "10"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.message").value("Token no autorizado."));
	}
}