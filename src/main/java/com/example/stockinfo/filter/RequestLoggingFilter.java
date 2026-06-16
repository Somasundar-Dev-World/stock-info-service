package com.example.stockinfo.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP Request/Response logging filter.
 *
 * <p>Logs every incoming request and outgoing response with:
 * <ul>
 *   <li>A unique Correlation ID (X-Correlation-Id header) for tracing</li>
 *   <li>HTTP method, URI, client IP</li>
 *   <li>Response HTTP status code</li>
 *   <li>Total processing time in milliseconds</li>
 * </ul>
 *
 * <p>Actuator endpoints (/actuator/**) are logged at DEBUG to reduce noise.
 */
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String MDC_CORRELATION_KEY = "correlationId";
    private static final String MDC_METHOD_KEY = "httpMethod";
    private static final String MDC_URI_KEY = "requestUri";

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Generate or inherit a Correlation ID for distributed tracing
        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // Set correlation ID on the response so clients can trace calls
        response.setHeader(CORRELATION_HEADER, correlationId);

        // Put tracing context into MDC (appears in every log line for this request)
        MDC.put(MDC_CORRELATION_KEY, correlationId);
        MDC.put(MDC_METHOD_KEY, request.getMethod());
        MDC.put(MDC_URI_KEY, request.getRequestURI());

        String  method      = request.getMethod();
        String  uri         = request.getRequestURI();
        String  queryString = request.getQueryString();
        String  clientIp    = getClientIp(request);
        boolean isActuator  = uri.startsWith("/actuator");

        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            if (isActuator) {
                log.debug(">>> REQUEST  [{corr:{}}] {} {} from {}", correlationId, method, fullUri, clientIp);
            } else {
                log.info(">>> REQUEST  [corr:{}] {} {} from {}", correlationId, method, fullUri, clientIp);
            }

            chain.doFilter(request, response);

        } finally {
            long duration   = System.currentTimeMillis() - startTime;
            int  statusCode = response.getStatus();

            // Log outgoing response
            if (isActuator) {
                log.debug("<<< RESPONSE [corr:{}] {} {} → {} ({} ms)",
                        correlationId, method, uri, statusCode, duration);
            } else if (statusCode >= 500) {
                log.error("<<< RESPONSE [corr:{}] {} {} → {} ({} ms) [SERVER ERROR]",
                        correlationId, method, uri, statusCode, duration);
            } else if (statusCode >= 400) {
                log.warn("<<< RESPONSE [corr:{}] {} {} → {} ({} ms) [CLIENT ERROR]",
                        correlationId, method, uri, statusCode, duration);
            } else {
                log.info("<<< RESPONSE [corr:{}] {} {} → {} ({} ms)",
                        correlationId, method, uri, statusCode, duration);
            }

            // Clear MDC to prevent memory leaks in thread pools
            MDC.remove(MDC_CORRELATION_KEY);
            MDC.remove(MDC_METHOD_KEY);
            MDC.remove(MDC_URI_KEY);
        }
    }

    /**
     * Extracts the real client IP, handling reverse proxies (X-Forwarded-For).
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For can contain a chain: "client, proxy1, proxy2"
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
