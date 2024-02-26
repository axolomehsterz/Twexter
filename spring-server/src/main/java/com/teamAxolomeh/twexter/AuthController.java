package com.teamAxolomeh.twexter;

import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final DatabaseQueryExecutor databaseQueryExecutor;
  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  public AuthController(DatabaseQueryExecutor databaseQueryExecutor) {
    this.databaseQueryExecutor = databaseQueryExecutor;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginDto data) {
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
      return ResponseEntity.ok("You are now logged in as:  " + data.getUsername());
    } catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody LoginDto data) {
    try {
      String query = "SELECT * FROM users WHERE username = ?";
      Object[] params = new Object[] { data.getUsername() };
      List<?> results = databaseQueryExecutor.query(query, params);
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