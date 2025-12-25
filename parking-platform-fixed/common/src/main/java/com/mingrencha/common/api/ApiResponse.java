package com.mingrencha.common.api;

import java.time.Instant;

public record ApiResponse<T>(boolean success, String message, T data, Instant ts) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, "OK", data, Instant.now()); }
  public static <T> ApiResponse<T> fail(String msg) { return new ApiResponse<>(false, msg, null, Instant.now()); }
}
