package com.packages.interceptor;

import com.packages.exception.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercepts all successful responses in Spring MVC and wraps them in a
 * consistent ApiResponse envelope before they are serialized to the client.
 *
 * This advice runs after the controller method has returned but before the
 * selected HttpMessageConverter writes the body. It mirrors the behavior of
 * the WebFlux ApiResponseAdviceHandler, keeping the response contract
 * identical across both stacks.
 *
 * Wrapping rules:
 * - If the body is already an ApiResponse, it is returned unchanged.
 * - If the body is an ErrorResponse, it is returned unchanged
 *   (the GlobalExceptionHandler already formatted it).
 * - If the body is null, an empty ApiResponse is returned using the real
 *   HTTP status.
 * - Otherwise, the body is wrapped in an ApiResponse carrying the real
 *   HTTP status.
 *
 * The HTTP status is read from the underlying HttpServletResponse rather
 * than being hardcoded, so responses such as 201 Created or 202 Accepted
 * are reflected correctly in the envelope.
 */
@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Determines whether this advice applies to the given return type.
     *
     * Always returns true. The decision to wrap or skip is made inside
     * beforeBodyWrite, where the actual body is available.
     *
     * @param returnType    the controller method return type
     * @param converterType the selected message converter type
     * @return always true
     */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Wraps the response body in an ApiResponse envelope when needed.
     *
     * The HTTP status is resolved from the underlying servlet response so
     * that statuses set via @ResponseStatus, ResponseEntity, or
     * programmatically are preserved in the envelope.
     *
     * @param body                  the value returned by the controller
     * @param returnType            the controller method return type
     * @param selectedContentType   the negotiated content type
     * @param selectedConverterType the selected message converter type
     * @param request               the current server request
     * @param response              the current server response
     * @return the wrapped body, or the original body if wrapping is skipped
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // Resolve the real HTTP status instead of hardcoding 200.
        int status = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse) {
            status = servletResponse.getServletResponse().getStatus();
        }

        // Already wrapped: return as-is.
        if (body instanceof ApiResponse) {
            return body;
        }

        // ErrorResponse is already formatted by GlobalExceptionHandler.
        if (body instanceof ErrorResponse) {
            return body;
        }

        // Empty body: return an empty envelope with the real status.
        if (body == null) {
            return new ApiResponse<>(status, null);
        }

        // Wrap the raw payload in an envelope with the real status.
        return new ApiResponse<>(status, body);
    }
}