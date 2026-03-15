package com.maggom.app.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Profile("local")
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val start = System.currentTimeMillis()

        filterChain.doFilter(request, response)

        val elapsed = System.currentTimeMillis() - start
        val query = request.queryString?.let { "?$it" } ?: ""

        log.info(">>> {} {}{} | {} ({}ms)",
            request.method,
            request.requestURI,
            query,
            response.status,
            elapsed,
        )
    }
}
