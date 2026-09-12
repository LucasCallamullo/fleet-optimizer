package com.routes.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;                                    

import feign.Response;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Feign Decoder that transparently unwraps the ApiResponse envelope returned
 * by downstream microservices.
 *
 * Why this is needed:
 * - All microservices now wrap successful responses in ApiResponse<T>.
 * - Feign clients that expect the raw payload (List<FleetVehicleDTO>, etc.)
 *   would fail to deserialize the wrapped body.
 * - This decoder detects the envelope shape and returns only the data node.
 *
 * Behavior:
 * - If the response body contains the ApiResponse envelope (data, status, success),
 *   the decoder returns the value of the data node deserialized to the target type.
 * - If the data node is null, the decoder returns null.
 * - If the response is not wrapped, the decoder delegates to SpringDecoder, which
 *   applies the standard Spring HttpMessageConverters.
 *
 * When to use it:
 * - Register it in the Feign configuration used by internal clients (ms-routes,
 *   ms-packages, etc.).
 * - Do not use it for clients that need access to the full ApiResponse (status,
 *   timestamp). Those clients should declare ApiResponse<T> as the return type
 *   and use a plain decoder.
 *
 * @see feign.codec.Decoder
 * @see org.springframework.cloud.openfeign.support.SpringDecoder
 */
public class ApiResponseUnwrapDecoder implements Decoder {

    private final Decoder delegate;
    private final ObjectMapper objectMapper;

    /**
     * Creates a decoder that unwraps ApiResponse envelopes.
     *
     * @param delegate     the fallback decoder used for non-wrapped responses
     * @param objectMapper the Jackson mapper used to parse and convert JSON
     */
    public ApiResponseUnwrapDecoder(Decoder delegate, ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
    }

    /**
     * Decodes a Feign response into the requested Java type.
     *
     * Steps:
     * 1. If the response has no body, delegate to the fallback decoder.
     * 2. Read the body bytes and parse them as a JSON tree.
     * 3. If the tree contains the ApiResponse envelope (data, status, success),
     *    extract the data node and convert it to the target type.
     * 4. If the data node is null, return null.
     * 5. Otherwise, delegate to the fallback decoder so standard Spring
     *    HttpMessageConverters handle the payload.
     *
     * @param response the Feign response
     * @param type     the target Java type to deserialize to
     * @return the deserialized object, or null if the data node is null
     * @throws IOException if reading or parsing the body fails
     */
    @Override
    public Object decode(Response response, Type type) throws IOException {
        // Step 1: No body, nothing to unwrap.
        if (response.body() == null) {
            return delegate.decode(response, type);
        }

        // Step 2: Read and parse the body once.
        byte[] body = response.body().asInputStream().readAllBytes();
        JsonNode root = objectMapper.readTree(body);

        // Step 3: Detect the ApiResponse envelope.
        if (root.has("data") && root.has("status") && root.has("success")) {
            JsonNode data = root.get("data");

            // Step 4: Null data node means the service returned no payload.
            if (data == null || data.isNull()) {
                return null;
            }

            // Step 5: Deserialize the data node to the requested type.
            return objectMapper.readValue(
                    data.traverse(objectMapper),
                    objectMapper.constructType(type)
            );
        }

        // Step 6: Not wrapped, let the fallback decoder handle it.
        return delegate.decode(response, type);
    }
}
