package com.autoflow.infrastructure.security;

import com.autoflow.infrastructure.security.service.CustomUserDetailsService;
import com.autoflow.infrastructure.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.tokenValido(token)) {
            String email = jwtService.extrairEmail(token);

            if (email != null) {
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                String roleDoToken = jwtService.extrairRole(token);

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + roleDoToken);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(authority)
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/public/")
                || path.startsWith("/auth/")
                || path.equals("/actuator/health")
                || path.equals("/actuator/health/liveness")
                || path.equals("/actuator/health/readiness")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
    }
}
