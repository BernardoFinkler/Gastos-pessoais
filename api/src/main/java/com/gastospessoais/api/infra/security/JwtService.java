package com.gastospessoais.api.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationAccess;
    private final long expirationRefresh;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-access}") long expirationAccess,
            @Value("${jwt.expiration-refresh}") long expirationRefresh
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationAccess = expirationAccess;
        this.expirationRefresh = expirationRefresh;
    }

    public String gerarAccessToken(UUID usuarioId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationAccess))
                .signWith(secretKey)
                .compact();
    }

    public String gerarRefreshToken(UUID usuarioId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationRefresh))
                .signWith(secretKey)
                .compact();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public UUID extrairUsuarioId(String token) {
        return UUID.fromString(extrairClaims(token).get("usuarioId", String.class));
    }

    public boolean isTokenValido(String token) {
        try {
            return extrairClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}