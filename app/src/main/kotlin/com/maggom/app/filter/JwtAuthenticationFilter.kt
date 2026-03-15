package com.maggom.app.filter

import com.maggom.auth.port.out.TokenPort
import com.maggom.common.exception.TokenExpiredException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenPort: TokenPort,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.method == "OPTIONS" || isPublicPath(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = extractToken(request)
        if (token == null) {
            writeUnauthorized(response, "인증이 필요합니다.")
            return
        }

        val email = try {
            tokenPort.extractEmail(token)
        } catch (e: TokenExpiredException) {
            writeUnauthorized(response, e.message!!)
            return
        }

        if (email == null) {
            writeUnauthorized(response, "유효하지 않은 액세스 토큰입니다.")
            return
        }

        val role = tokenPort.extractRole(token) ?: "MEMBER"

        if (isAdminPath(request.requestURI) && role != "ADMIN") {
            writeForbidden(response, "관리자 권한이 필요합니다.")
            return
        }

        request.setAttribute("authenticatedEmail", email)
        request.setAttribute("authenticatedRole", role)
        filterChain.doFilter(request, response)
    }

    private fun isPublicPath(uri: String): Boolean {
        return PUBLIC_PATHS.any { uri.startsWith(it) }
    }

    private fun isAdminPath(uri: String): Boolean {
        return uri.startsWith("/api/v1/admin/")
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null

        return header.removePrefix("Bearer ").trim()
    }

    private fun writeUnauthorized(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        response.writer.write("""{"message": "$message"}""")
    }

    private fun writeForbidden(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json;charset=UTF-8"
        response.writer.write("""{"message": "$message"}""")
    }

    companion object {
        private val PUBLIC_PATHS = listOf(
            "/api/v1/auth/",
            "/api/v1/subscriptions/count",
        )
    }
}
