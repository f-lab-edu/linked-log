package flab.Linkedlog.config;

import flab.Linkedlog.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String username = jwtUtil.validateToken(token);
                String role = (String) jwtUtil.getClaimsFromToken(token).get("roles");

                if (role == null || role.isEmpty()) {
                    throw new RuntimeException("empty roles in JWT token");
                }

                if (username != null) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(username, null, List.of(authority))
                    );
                }
            } catch (RuntimeException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}