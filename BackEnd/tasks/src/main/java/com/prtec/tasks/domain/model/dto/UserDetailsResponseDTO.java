package com.prtec.tasks.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDTO {
	private Long userId;
	private String username;
	private String email;
	private String firstName;
	private String lastName;
}