package com.homeapp.auth.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void usesCorrelationIdFromRequestHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "test-ola-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // "Dalsza część aplikacji" w teście tylko zapamiętuje, co było w MDC w trakcie zapytania.
        AtomicReference<String> duringRequest = new AtomicReference<>();
        FilterChain chain = (req, res) -> duringRequest.set(MDC.get("correlationId"));

        filter.doFilter(request, response, chain);

        assertThat(duringRequest.get()).isEqualTo("test-ola-1");
        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("test-ola-1");
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void replacesMissingOrInvalidHeaderWithNewId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "zły ID\nINFO fałszywa linijka");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader("X-Correlation-Id"))
            .isNotEqualTo("zły ID\nINFO fałszywa linijka")
            .hasSize(36);
    }
}
