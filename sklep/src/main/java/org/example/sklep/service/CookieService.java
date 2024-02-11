package org.example.sklep.service;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public Cookie genaratedCookie(String name, String value, int exp){
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(exp);
        return cookie;
    }
}
