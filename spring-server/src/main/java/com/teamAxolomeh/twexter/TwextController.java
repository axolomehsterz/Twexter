package com.teamAxolomeh.twexter;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/twext")
public class TwextController {
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  private final DatabaseQueryExecutor databaseQueryExecutor;

  @Autowired
  public TwextController(DatabaseQueryExecutor databaseQueryExecutor) {
    this.databaseQueryExecutor = databaseQueryExecutor;
  }

  @GetMapping
  public ResponseEntity<?> getAllTwexts(HttpServletRequest req, HttpServletResponse res) {
    try {
      final String query = "SELECT u.*, t.content FROM users u JOIN twexta t ON t.user_id=?;";
      Claims user = (Claims) req.getAttribute("user");
      Object[] params = new Object[] { user.get("_id") };
      List<Map<String, Object>> results = databaseQueryExecutor.query(query, params);
      return ResponseEntity.ok().body(results);
    } catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  } 

  // @PostMapping("/")
  // public ResponseEntity<String> login(HttpServletResponse res, @RequestBody LoginDto data) {

  // }
}
