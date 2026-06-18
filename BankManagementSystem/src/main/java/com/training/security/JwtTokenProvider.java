package com.training.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs
    )
    {
        this.secretKey= Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.jwtTokenExpirationMs=jwtExpirationMs;
    }

    public String generateToken(Authentication authentication)
    {
        UserDetails  userDetails=(UserDetails)authentication.getPrincipal();
        return generateTokenFromEmail(userDetails.getUsername());
    }

    public String generateTokenFromEmail(String email)
    {
        Date now=new Date();
        Date expiryDate=new Date(now.getTime()+jwtTokenExpirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token)
    {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token)
    {

        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }
        catch(MalformedJwtException ex)
        {
            log.error("Invalid JWT Token:{}",ex.getMessage());
        }
        catch (ExpiredJwtException ex) {
            log.error("Expired JWT Token: {}",ex.getMessage());
        }
        catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT Token: {}",ex.getMessage());
        }
        catch(IllegalArgumentException ex)
        {
            log.error("JWT claims string is empty: {}",ex.getMessage());
        }
        return false;

    }
}
