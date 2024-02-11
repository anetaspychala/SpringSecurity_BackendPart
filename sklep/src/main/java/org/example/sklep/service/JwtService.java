package org.example.sklep.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Service
public class JwtService {
//Jason web token, uzywane do bezpiecznej autoryzacji, za pomoca tokenu

    //wygenerowany secret z pythona
    public static final String SECRET = "B0FFD4C056ECEAB1D800F38284DD334D194C3B3436237D824404403D5C09B5EA";

    //sluzy do walidacji tokent, czy nazwy sie zgadza i czy token nie wygasl
    public void validateToken(final String token) throws ExpiredJwtException, IllegalArgumentException {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    //zwraca klucz uzywany do podpisywania i generowania tokenu
    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    //tworzy token jwt, na podstawie nazwy uzytkownika
    public String generateToken(String username, int exp){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, exp);
    }

    //tworzy nowy token jwt
    public String createToken(Map<String, Object> claims, String username, int exp){
       return Jwts.builder()
               .setClaims(claims)
               .setSubject(username)
               .setIssuedAt(new Date(System.currentTimeMillis()))
               .setExpiration(new Date(System.currentTimeMillis()+exp))
               .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    //odswiezenie tokenu zeby nie wygasl
    private String getSubject(final String token){
        return Jwts
                .parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String refreshToken(final String token, int exp){
        String username = getSubject(token);
        return generateToken(username, exp);
    }
}
