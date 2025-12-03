package com.datanumia.energyconsumption.s2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "ok")
@NoArgsConstructor
public class ApiResponse {
    private boolean success = true;
    private String message;

    public static ApiResponse error(String message) {
        ApiResponse response = new ApiResponse();
        response.success = false;
        response.message = message;
        return response;
    }
}
