package com.prtec.tasks.domain.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailsRequestDTO {
	private Long userId;
	private String username;
	private String email;
	private String firstName;
	private String lastName;
}