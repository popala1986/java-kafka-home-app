package com.homeapp.auth.correlation;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Każde zapytanie HTTP przechodzi najpierw przez ten filtr: bierze ID z nagłówka albo tworzy nowe,
// wpisuje je do MDC (od teraz każda linijka logu w tym zapytaniu ma ten ID) i odsyła je w odpowiedzi.
// HIGHEST_PRECEDENCE = filtr działa jako pierwszy, przed Spring Security, więc nawet 401 i 403 mają ID w logach.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String correlationId = CorrelationId.orNew(request.getHeader(CorrelationId.HTTP_HEADER));
        MDC.put(CorrelationId.MDC_KEY, correlationId);
        response.setHeader(CorrelationId.HTTP_HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            // Wątek wraca do puli i obsłuży kolejne zapytanie. Bez sprzątania dostałby stary ID.
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
