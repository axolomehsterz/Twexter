package com.teamAxolomeh.twexter;

import java.util.Map;
import javax.crypto.SecretKey;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URI;
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
  private String googleClientSecret;
  private String googleClientId;

  public AuthController(DatabaseQueryExecutor databaseQueryExecutor, Environment environment) {
    this.env = environment;
    jwtSecret = env.getRequiredProperty("SUPER_SECRET");
    githubClientId = env.getRequiredProperty("GITHUB_CLIENT_ID");
    githubClientSecret = env.getRequiredProperty("GITHUB_CLIENT_SECRET");
    googleClientId = env.getRequiredProperty("GOOGLE_CLIENT_ID");
    googleClientSecret = env.getRequiredProperty("GOOGLE_CLIENT_SECRET");
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

  @GetMapping("/google")
  public ModelAndView redirectToGoogleLogin() {
    final String redirectUrl = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount" +
        "?response_type=code" +
        "&redirect_uri=" +
        "http://localhost:8080/auth/google/callback" +
        "&scope=email%20profile&" +
        "client_id=" +
        googleClientId +
        "&service=lso&o2v=2" +
        "&theme=glif" +
        "&flowName=GeneralOAuthFlow";
    return new ModelAndView("redirect:" + redirectUrl);
  }

  @GetMapping("/google/callback")
  public RedirectView googleCallback(@RequestParam String code, HttpServletResponse res) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add("code", code);
      map.add("client_id", googleClientId);
      map.add("client_secret", googleClientSecret);
      map.add("redirect_uri", "http://localhost:8080/auth/google/callback");
      map.add("grant_type", "authorization_code");
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
      ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<Map<String, Object>>() {
      };
      MultiValueMap<String, String> requestBody = request.getBody();
      if (requestBody == null) {
        throw new IllegalArgumentException("Request body can not be null");
      }
      RequestEntity<?> requestEntity = RequestEntity
          .post(new URI("https://oauth2.googleapis.com/token"))
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(requestBody);
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
          "https://oauth2.googleapis.com/token",
          HttpMethod.POST,
          requestEntity,
          responseType);
      Map<String, Object> responseBody = response.getBody();
      // Extract access token from response
      if (responseBody == null) {
        throw new IllegalArgumentException("Response body can not be null");
      }
      String accessToken = (String) responseBody.get("access_token");
      // Use access token to call Google People API
      String userInfoUri = "https://people.googleapis.com/v1/people/me?personFields=emailAddresses&access_token="
          + accessToken;
      EmailResponse emailResponse = restTemplate.getForObject(userInfoUri, EmailResponse.class);
      if (emailResponse == null) {
        throw new IllegalArgumentException("Email Response can not be null");
      }
      String username = emailResponse.getEmailAddresses().get(0).getValue();
      // Find or create with email
      List<Map<String, Object>> results = findOrCreateUser(username);
      final String redirectUrl = "/feed?user=" + username;
      final String jwt = generateToken(results.get(0));
      CookieSetter.setCookie(res, "ssid", jwt);
      return new RedirectView(redirectUrl);
    } catch (Exception e) {
      logger.info(e.getMessage());
      final String redirectUrl = "login";
      return new RedirectView(redirectUrl);
    }
  }

  // Returns the user
  private List<Map<String, Object>> findOrCreateUser(String username) {
    final String queryFind = "SELECT users.* FROM users WHERE users.username = ?;";
    final Object[] paramsFind = new Object[] { username };
    List<Map<String, Object>> results = databaseQueryExecutor.query(queryFind, paramsFind);
    // Create a user if they don't exist
    if (results.size() == 0) {
      final String queryCreate = "INSERT INTO users (username) VALUES (?) RETURNING *;";
      final Object[] paramsCreate = new Object[] { username };
      results = databaseQueryExecutor.query(queryCreate, paramsCreate);
    }
    return results;
  }

  @GetMapping("/callback")
  public RedirectView githubCallback(@RequestParam Map<String, String> params, HttpServletResponse res) {
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
      ParameterizedTypeReference<Map<String, String>> typeRef = new ParameterizedTypeReference<Map<String, String>>() {
      };
      ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request,
          typeRef);
      Map<String, String> token = responseEntity.getBody();
      if (token == null) {
        throw new IllegalArgumentException("Token can not be null");
      }
      String headerValue = "Bearer " + token.get("access_token");
      headers = new HttpHeaders();
      headers.set("Authorization", headerValue);
      url = "https://api.github.com/user";
      request = new HttpEntity<>(headers);
      ResponseEntity<GitHubResponse> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          request,
          GitHubResponse.class);
      GitHubResponse responseBody = response.getBody();
      if (responseBody == null) {
        throw new IllegalArgumentException("response body can not be null");
      }
      String username = responseBody.getLogin();
      // Find or create user
      List<Map<String, Object>> results = findOrCreateUser(username);
      final String redirectUrl = "/feed?user=" + username;
      final String jwt = generateToken(results.get(0));
      CookieSetter.setCookie(res, "ssid", jwt);
      return new RedirectView(redirectUrl);
    } catch (Exception e) {
      logger.info("Error!");
      logger.error(e.getLocalizedMessage());
      final String redirectUrl = "login";
      return new RedirectView(redirectUrl);
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
