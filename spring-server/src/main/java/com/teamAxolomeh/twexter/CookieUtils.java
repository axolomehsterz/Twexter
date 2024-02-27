package com.teamAxolomeh.twexter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtils {

  public static Cookie getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    System.out.println(cookies);
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        System.out.println(cookie.getName());
        System.out.println("--------");
        if (name.equals(cookie.getName())) {
          return cookie;
        }
      }
    }
    return null; // Cookie not found
  }
}