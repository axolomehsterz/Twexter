package com.teamAxolomeh.twexter;

import java.util.Map;

import javax.crypto.SecretKey;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
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
  private String githubClientId;
  private String githubClientSecret;

  public static class GitHubResponse {

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    // No-argument constructor
    public GitHubResponse() {
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}


  @Autowired
  public AuthController(DatabaseQueryExecutor databaseQueryExecutor, Environment env) {
    this.env = env;
    jwtSecret = env.getProperty("SUPER_SECRET", "Uh oh, the secret is missing");
    githubClientId = env.getProperty("GITHUB_CLIENT_ID", "Missing github client id");
    githubClientSecret = env.getProperty("GITHUB_CLIENT_SECRET", "Missing github secret");

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

  @GetMapping
  public ModelAndView redirectToGithubLogin() {
    final String redirectUrl = "https://github.com/login/oauth/authorize?client_id=" + this.githubClientId;
    return new ModelAndView("redirect:" + redirectUrl);
  }

  @GetMapping("/callback")
  public String githubCallback(@RequestParam Map<String, String> params) {
    try {

      class Body {
        public Body(String client_id, String client_secret, String code) {
          this.client_id = client_id;
          this.client_secret = client_secret;
          this.code = code;
        }

        @JsonProperty("client_id")
        private String client_id;

        @JsonProperty("client_secret")
        private String client_secret;

        @JsonProperty("code")
        private String code;
      }


      

      Body body = new Body(githubClientId, githubClientSecret, params.get("code"));
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Body> request = new HttpEntity<>(body, headers);
      String url = "https://github.com/login/oauth/access_token";
      RestTemplate restTemplate = new RestTemplate();
      Map<String, String> token = restTemplate.postForObject(url, request, Map.class);
      String headerValue = "Bearer " + token.get("access_token");

      // token: Bearer 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
      //        Bearer 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

      // Get github username
      // String splitTokenValue = tokenValue.split("&")[0].split("=")[1];
      headers = new HttpHeaders();
      headers.set("Authorization", headerValue);
      url = "https://api.github.com/user";
      request = new HttpEntity<>(headers);
      ResponseEntity<GitHubResponse> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        request,
        GitHubResponse.class
      );
        // url, request, GitHubResponse.class);
      System.out.println("email: " + response.getBody().getEmail());
    // Redirect to /feed?user= + {username}
      return "I won!";
    } catch (Exception e) {
      // TODO: handle exception
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      return e.getMessage();
    }
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