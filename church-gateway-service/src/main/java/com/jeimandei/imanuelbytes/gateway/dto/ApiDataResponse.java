package com.jeimandei.imanuelbytes.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Thin wrapper that maps the {@code data} field from the backend {@code ApiResponse<T>}
 * envelope so RestTemplate can extract typed payloads without access to the
 * common module's generic ApiResponse class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDataResponse<T> {

    private T data;

    public ApiDataResponse() {}

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
