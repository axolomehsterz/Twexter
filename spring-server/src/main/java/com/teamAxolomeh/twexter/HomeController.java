package com.teamAxolomeh.twexter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;

@RestController
public class HomeController {

  private final DatabaseQueryExecutor databaseQueryExecutor;

  @Autowired
  public HomeController(DatabaseQueryExecutor databaseQueryExecutor, Environment env) {
    this.databaseQueryExecutor = databaseQueryExecutor;
  }

  @GetMapping("/home")
  public List<?> someMethod() {
    String sql = "SELECT * FROM users;";
    List<?> results = databaseQueryExecutor.query(sql, null);
    return results;
  }

}