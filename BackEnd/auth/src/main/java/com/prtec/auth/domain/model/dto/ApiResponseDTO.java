package com.prtec.auth.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private Status status;
    private String message;
    private T data;

    public static final String OK = "OK";

    public enum Status {
        SUCCESS("success"),
        ERROR("error");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}