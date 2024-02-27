package com.teamAxolomeh.twexter;

import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieSetter {

  public static void setCookie(HttpServletResponse res, String key, String value) {
    Cookie cookie = new Cookie("ssid", value);
    cookie.setPath("/");
    cookie.setMaxAge(86400);
    cookie.setHttpOnly(true);
    // Add cookie to response
    res.addCookie(cookie);
  }
}
