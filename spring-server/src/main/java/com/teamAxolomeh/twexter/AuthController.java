package com.teamAxolomeh.twexter;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/auth")
public class AuthController {

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginDto data) {
    System.out.println(data.getUsername());
    return Map.of(
            "key", "value",
            "foo", "bar"
        );
  }

}