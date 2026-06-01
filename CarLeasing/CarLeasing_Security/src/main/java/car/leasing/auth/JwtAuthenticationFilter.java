package car.leasing.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization"); // достаём токен из заголовка

        if (authHeader == null || !authHeader.startsWith("Bearer ")) { // носитель
            chain.doFilter(request, response); // нет токена — пропускаем дальше
            return;
        }

        String token = authHeader.substring(7);
        String login = JwtUtil.extractLogin(token);
        List<SimpleGrantedAuthority> roles = JwtUtil.extractRoles(token).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        if (login != null && JwtUtil.isValidToken(token, login)) {
            UsernamePasswordAuthenticationToken auth =  // создаём Authentication с ролями
                    new UsernamePasswordAuthenticationToken(
                            login,
                            null,
                            roles);
            SecurityContextHolder.getContext().setAuthentication(auth); //кладём в SecurityContext
        }
        chain.doFilter(request, response);
    }
}
