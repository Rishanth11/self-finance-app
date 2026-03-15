package com.pro.finance.selffinanceapp.config;

import com.pro.finance.selffinanceapp.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final Key key = Keys.hmacShaKeyFor(
            "replace_with_a_very_long_secret_key_change_in_prod_please!"
                    .getBytes()
    );

    private final long expirationMs = 1000L * 60 * 60 * 24; // 24 hours

    /**
     * Generate token from UserDetails only (authorities + sub).
     * Used as fallback — no name claim.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList());
        return buildToken(claims, userDetails.getUsername());
    }

    /**
     * Generate token with the User entity — includes the name claim.
     * Call this from your AuthController at login/register.
     */
    public String generateToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList());
        // ← This is the fix: store the actual name in the token
        claims.put("name", user.getName());
        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("name", String.class);
    }

    public List<String> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("authorities", List.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username)
                    && !extractExpiration(token).before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}