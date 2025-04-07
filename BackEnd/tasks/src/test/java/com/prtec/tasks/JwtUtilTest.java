package com.prtec.tasks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.prtec.tasks.application.utils.JwtUtil;
import com.prtec.tasks.domain.model.dto.ApiResponseDTO;

import io.jsonwebtoken.Claims;

class JwtUtilTest {
	private static final String STR_VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJVU0VSIl0sInVzZXJJZCI6Miwic3ViIjoidXNlciIsImlhdCI6MTc0Mzk2NzAzOSwiZXhwIjoxNzQzOTcwNjM5fQ.U0FdBGyD9uQz5Zucs4DTV_i33bM47X3r-oWAOxPFVAw";
	private static final String STR_VALID_TOKEN_ADMIN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJBRE1JTiIsIlVTRVIiXSwidXNlcklkIjoxLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc0Mzk2NzEwMSwiZXhwIjoxNzQzOTcwNzAxfQ.Nn2ype-3jxQD8igOE8ZAKjfo2YfmAO0uDntWDHX_nBM";
	private static final String STR_VALID_TOKEN_NO_ROLES = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2Mzg2NywiZXhwIjoxNzc0Njk5ODY3fQ.I5XQ2jq58uVb49R1awvWWgxzx8l3Q2gfV9JToP6sLFE";
	private static final String STR_VALID_TOKEN_INVALID_ROLES = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WzEyMyx0cnVlLCJVU0VSIl0sInN1YiI6InRlc3RVc2VyIiwiaWF0IjoxNzQzMTYzOTAwLCJleHAiOjE3NzQ2OTk5MDB9.a7VVT9IjWheF1f5259FeYfO8PmJ7Ibs0eDVn4UC_yE0";

	private JwtUtil jwtUtil;

	@Mock
	private Claims claims;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		jwtUtil = new JwtUtil();
	}

	@Test
	void testGetUsernameFromToken() {
		assertEquals("user", jwtUtil.getUsernameFromToken(STR_VALID_TOKEN));
	}

	@Test
	void testGetRolesFromTokenWithValidRoles() {
		List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(STR_VALID_TOKEN_ADMIN);
		assertEquals(2, roles.size());
		assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("USER")));
		assertTrue(roles.stream().anyMatch(role -> role.getAuthority().equals("ADMIN")));
	}

	@Test
	void testGetRolesFromTokenWithNoRoles() {
		List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(STR_VALID_TOKEN_NO_ROLES);
		assertTrue(roles.isEmpty());
	}

	@Test
	void testGetRolesFromTokenWithInvalidRoles() {
		List<GrantedAuthority> roles = jwtUtil.getRolesFromToken(STR_VALID_TOKEN_INVALID_ROLES);
		assertEquals(1, roles.size());
		assertEquals("USER", roles.get(0).getAuthority());
	}

	@Test
	void testIsTokenExpired() {
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2Mzk4OCwiZXhwIjoxNzc0Njk5OTg4fQ.pSSy9NZwFZ_EcsE26E5ziD4z-rp3qEAQ92BI11qlbOI";
		assertFalse(jwtUtil.isTokenExpired(token));
	}

	@Test
	void testIsTokenValid() {
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTc0MzE2MzcyNiwiZXhwIjoxNzc0Njk5NzI2fQ.h5kyxeiPoyD3xWelg4x-OP8kXJB0Bg_E0njP1WCTl_A";

		// Subclase anónima para sobreescribir isTokenExpired
		JwtUtil customJwtUtil = new JwtUtil() {
			@Override
			public boolean isTokenExpired(String token) {
				return false; // simula que el token NO está expirado
			}
		};
		
		// Mock RestTemplate
		RestTemplate restTemplateMock = mock(RestTemplate.class);
		ReflectionTestUtils.setField(customJwtUtil, "restTemplate", restTemplateMock);

		// Respuesta simulada
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("valid", true);


		ApiResponseDTO<Map<String, Object>> successResponse = new ApiResponseDTO<>();
		successResponse.setStatus(ApiResponseDTO.Status.SUCCESS);
		successResponse.setData(responseData);

		ResponseEntity<ApiResponseDTO<Map<String, Object>>> responseEntity = ResponseEntity.ok(successResponse);

		// Tipado explícito
		ParameterizedTypeReference<ApiResponseDTO<Map<String, Object>>> typeRef = new ParameterizedTypeReference<>() {};

		when(restTemplateMock.exchange(
				anyString(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(typeRef))).thenReturn(responseEntity);

		assertTrue(customJwtUtil.isTokenValid(token, "testUser"));
		assertFalse(customJwtUtil.isTokenValid(token, "wrongUser"));
	}
}