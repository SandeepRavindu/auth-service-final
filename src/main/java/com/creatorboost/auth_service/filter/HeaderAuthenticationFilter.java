/*package com.creatorboost.auth_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
*/
/**
 * Reads X-User-Email header (set by API Gateway) and populates SecurityContext for downstream controllers.
 */
/*public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_HEADER = "X-User-Email";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String email = request.getHeader(USER_HEADER);

        if (email != null && !email.isBlank()) {
            // Create an authenticated token with no authorities (adjust if you want roles)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            auth.setDetails(request.getRemoteAddr());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear the security context after request is done to avoid leakage across requests
            SecurityContextHolder.clearContext();
        }
    }
}*/
