package com.teamAxolomeh.twexter;

import java.util.Map;

import javax.crypto.SecretKey;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final DatabaseQueryExecutor databaseQueryExecutor;
  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  private Environment env;
  private String jwtSecret;

  @Autowired
  public AuthController(DatabaseQueryExecutor databaseQueryExecutor, Environment env) {
    this.env = env;
    jwtSecret = env.getProperty("SUPER_SECRET", "Uh oh, the secret is missing");
    this.databaseQueryExecutor = databaseQueryExecutor;
  }

  private String generateToken(Map<String, Object> map) {
    Claims claims = new DefaultClaims();
    map.forEach(claims::put);
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour expiration
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(HttpServletResponse res, @RequestBody LoginDto data) {
    try {
      final String query = "SELECT * FROM users WHERE username = ?";
      final Object[] params = new Object[] { data.getUsername() };
      List<Map<String, Object>> results = databaseQueryExecutor.query(query, params);
      if (results.size() != 1) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("Invalid username or password");
      }
      final String hashedPassword = results.get(0).get("password").toString();
      final String enteredPassword = data.getPassword();
      boolean isPasswordMatch = passwordEncoder.matches(enteredPassword, hashedPassword);
      if (!isPasswordMatch) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("Invalid username or password");
      }

      final String token = generateToken(results.get(0));
      CookieSetter.setCookie(res, "ssid", token);
      final String responseJson = "{\"username\": \"" + data.getUsername() + "\"}";
      return ResponseEntity.ok().body(responseJson);

    } catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody LoginDto data) {
    try {
      String query = "SELECT * FROM users WHERE username = ?";
      Object[] params = new Object[] { data.getUsername() };
      List<Map<String, Object>> results = databaseQueryExecutor.query(query, params);
      if (results.size() != 0) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body("Invalid username");
      }
      String hashedPassword = passwordEncoder.encode(data.getPassword());
      query = "INSERT INTO users (username, password) VALUES (?, ?) RETURNING *;";
      params = new Object[] { data.getUsername(), hashedPassword };
      results = databaseQueryExecutor.query(query, params);
      return ResponseEntity.ok("A new user was created: " + data.getUsername());
    } catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}