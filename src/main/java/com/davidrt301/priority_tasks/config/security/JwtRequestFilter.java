package com.davidrt301.priority_tasks.config.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.davidrt301.priority_tasks.model.dtos.Login.PrincipalInformation;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

//Filtro de seguridad que intercepta cada petición HTTP para validar el token JWT.
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtToken jwtToken;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
            
        var header = request.getHeader("Authorization");

        if(header != null && header.startsWith("Bearer ")){
            var token = header.substring(7);
            
            try{
                Claims claims = jwtToken.extractAllClaims(token);

                // Aquí podrías cargar el usuario y establecer la autenticación en el contexto de seguridad
                var username = claims.getSubject();
                var email = claims.get("email", String.class);
                var role = claims.get("role", String.class);
                if (role == null || role.isBlank()) {
                    role = "ROLE_USER";
                } else if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                var principal = new PrincipalInformation(username, email);
                var userToken = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role)));

                SecurityContextHolder.getContext().setAuthentication(userToken);
            }catch(Exception e){
                logger.error("Token JWT inválido: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

}
