package com.teamAxolomeh.twexter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.Cookie;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class IsLoggedInInterceptor implements HandlerInterceptor {

  private Environment env;
  private String jwtSecret;
  private SecretKey key;

  @Autowired
  public IsLoggedInInterceptor(Environment env) {
    this.env = env;
    jwtSecret = env.getProperty("SUPER_SECRET", "Uh oh, the secret is missing");
    key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public Claims decodeJWT(String jwtToken) {
    return Jwts.parserBuilder()
        // If you want to verify the signature, you must specify the key here
        .setSigningKey(key) // Set the signing key here
        .build()
        .parseClaimsJws(jwtToken)
        .getBody();
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    request.getCookies();
    Cookie jwt = CookieUtils.getCookie(request, "ssid");
    if (jwt == null) {
      return false;
    }
    Claims claims = decodeJWT(jwt.getValue());
    request.setAttribute("user", claims);
    return true;
  }
}
