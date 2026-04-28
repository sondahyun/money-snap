package com.tripline.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int httpStatusCode,
        String responseMessage,
        T data,
        String errorMessage
) {

    public static <T> ApiResponse<T> ok(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data, null);
    }

    public static ApiResponse<Void> fail(int statusCode, String errorMessage) {
        return new ApiResponse<>(statusCode, null, null, errorMessage);
    }
}
