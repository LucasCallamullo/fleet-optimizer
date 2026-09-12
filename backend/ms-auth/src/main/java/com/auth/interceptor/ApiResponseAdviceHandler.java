package com.auth.interceptor;

import com.auth.exception.ErrorResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

/**
 * Intercepts WebFlux controller return values and wraps them into a standard ApiResponse format.
 */
@Component
// Step 1: Set high priority order so this handler executes before standard WebFlux result handlers.
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiResponseAdviceHandler implements HandlerResultHandler {

    private final ResponseBodyResultHandler delegate;

    // Step 2a: Synthetic MethodParameter describing the wrapped return type (ApiResponse<Object>).
    // This is required because Jackson uses the MethodParameter to resolve the target type when
    // serializing the response body. Reusing the original MethodParameter (e.g. Mono<Map<String,Object>>)
    // while returning an ApiResponse causes a "Type definition error" during serialization.
    private static final MethodParameter WRAPPED_RETURN_TYPE;

    static {
        try {
            Method dummyMethod = ApiResponseAdviceHandler.class
                    .getDeclaredMethod("wrappedReturnTypeDummy");
            WRAPPED_RETURN_TYPE = new MethodParameter(dummyMethod, -1);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to create synthetic MethodParameter for ApiResponse", e);
        }
    }

    // Step 2: Inject Spring's standard ResponseBodyResultHandler lazily to process final rendering.
    public ApiResponseAdviceHandler(@Lazy ResponseBodyResultHandler delegate) {
        this.delegate = delegate;
    }

    // Step 3: Delegate support checking to the default response body result handler.
    @Override
    public boolean supports(HandlerResult result) {
        return delegate.supports(result);
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        // Step 4: Resolve the effective HTTP status for this result (201, 204, etc.).
        HttpStatus status = resolveStatus(result);

        // Step 5: Skip wrapping entirely for statuses that must not carry a body (204, 304).
        // Injecting a JSON body into a 204 would violate the HTTP specification.
        if (status == HttpStatus.NO_CONTENT || status == HttpStatus.NOT_MODIFIED) {
            return delegate.handleResult(exchange, result);
        }

        Object body = result.getReturnValue();

        // Step 6: Intercept reactive Mono streams and map internal payload before passing down to WebFlux.
        if (body instanceof Mono<?>) {
            Mono<?> monoBody = (Mono<?>) body;

            // Step 7: Map inner Mono element to ApiResponse and fallback to an empty success structure if Mono is empty.
            Mono<Object> wrappedMono = monoBody
                    .map(payload -> wrapPayload(payload, status))
                    .defaultIfEmpty(new ApiResponse<>(status.value(), null));

            // Step 8: Construct a new HandlerResult containing the modified Mono publisher.
            // NOTE: We must use WRAPPED_RETURN_TYPE instead of the original MethodParameter so that
            // Jackson resolves the correct target type (ApiResponse) during serialization.
            HandlerResult newResult = new HandlerResult(
                    result.getHandler(),
                    wrappedMono,
                    WRAPPED_RETURN_TYPE
            );
            return delegate.handleResult(exchange, newResult);
        }

        // Step 9: Handle synchronous (non-reactive) object return values directly.
        Object wrappedBody = wrapPayload(body, status);

        // Step 9a: Use the synthetic MethodParameter for the same reason as in the reactive branch.
        HandlerResult newResult = new HandlerResult(
                result.getHandler(),
                wrappedBody,
                WRAPPED_RETURN_TYPE
        );
        return delegate.handleResult(exchange, newResult);
    }

    /**
     * Step 10: Resolve the effective HTTP status for the given handler result.
     *
     * Priority order:
     *   1. If the handler is a HandlerMethod annotated with @ResponseStatus, use that status.
     *   2. If the return type is Mono<Void> (or void), assume 204 No Content.
     *   3. Otherwise, default to 200 OK.
     *
     * Note: We intentionally do NOT read exchange.getResponse().getStatusCode() here because this
     * handler runs at HIGHEST_PRECEDENCE, before Spring has applied @ResponseStatus to the exchange.
     */
    private HttpStatus resolveStatus(HandlerResult result) {
        // Step 10a: Check for @ResponseStatus on the controller method.
        if (result.getHandler() instanceof HandlerMethod handlerMethod) {
            ResponseStatus annotation = handlerMethod.getMethodAnnotation(ResponseStatus.class);
            if (annotation != null) {
                return annotation.code();
            }
        }

        // Step 10b: Detect Mono<Void> / void return types and treat them as 204 No Content.
        ResolvableType returnResolvable = ResolvableType.forMethodParameter(result.getReturnTypeSource());
        Class<?> generic = returnResolvable.getGeneric(0).resolve();
        if (Void.class.equals(generic) || void.class.equals(generic)) {
            return HttpStatus.NO_CONTENT;
        }

        // Step 10c: Default to 200 OK for everything else.
        return HttpStatus.OK;
    }

    // Step 11: Apply conditional evaluation identical to standard MVC ResponseBodyAdvice.
    private Object wrapPayload(Object body, HttpStatus status) {
        // Step 12: Bypass wrapping if the response is already packaged inside an ApiResponse object.
        if (body instanceof ApiResponse) {
            return body;
        }

        // Step 13: Bypass wrapping if the response originates from GlobalExceptionHandler as an ErrorResponse.
        if (body instanceof ErrorResponse) {
            return body;
        }

        // Step 14: Bypass wrapping if the body is a ResponseEntity; let Spring handle it.
        // (ResponseEntity carries its own status and headers.)
        if (body instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getBody();
        }

        // Step 15: Handle null payload references by returning a standard empty success structure.
        if (body == null) {
            return new ApiResponse<>(status.value(), null);
        }

        // Step 16: Wrap raw domain model or DTO into standard ApiResponse structure.
        return new ApiResponse<>(status.value(), body);
    }

    // Step 17: Dummy method used only to obtain a MethodParameter that describes ApiResponse<Object>.
    // This method is never invoked; it exists solely to provide type metadata to Jackson.
    @SuppressWarnings("unused")
    private ApiResponse<Object> wrappedReturnTypeDummy() {
        return null;
    }
}