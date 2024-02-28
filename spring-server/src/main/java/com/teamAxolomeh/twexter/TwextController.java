package com.teamAxolomeh.twexter;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  class GetTwextaResponse {
    private List<Map<String, Object>> result;

    GetTwextaResponse(List<Map<String, Object>> content) {
      this.result = content;
    }
    public List<Map<String, Object>> getResult() {
      return this.result;
    }
  }

  class PostTwextaResponse {
    private String result;

    PostTwextaResponse(String content) {
      this.result = content;
    }
    public String getResult() {
      return this.result;
    }
  }

  @GetMapping
  public ResponseEntity<GetTwextaResponse> getAllTwexts(HttpServletRequest req, HttpServletResponse res) {
    try {
      final String query = "SELECT u.*, t.content FROM users u JOIN twexta t ON t.user_id=u._id;";
      List<Map<String, Object>> results = databaseQueryExecutor.query(query, null);
      GetTwextaResponse response = new GetTwextaResponse(results);
      return ResponseEntity.ok().body(response);
    } catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      GetTwextaResponse response = new GetTwextaResponse(null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  @PostMapping
  public ResponseEntity<PostTwextaResponse> login(HttpServletRequest req, HttpServletResponse res, @RequestBody PostTwextaDto data) {
    try {
      Claims user = (Claims) req.getAttribute("user");
      final String query = "INSERT INTO twexta (content, user_id) VALUES (?, ?) RETURNING *;";
      System.out.println(user.get("_id"));
      Object[] params = new Object[] { data.getTwextContent(), user.get("_id") };
      List<Map<String, Object>> results = databaseQueryExecutor.query(query, params);
      final String message = (results != null) ? "Stored the twext" : "DID NOT store the twext";
      PostTwextaResponse response = new PostTwextaResponse(message);
      System.out.println(response);
      return ResponseEntity.ok().body(response);
    }
      catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      PostTwextaResponse response = new PostTwextaResponse(e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }
}
