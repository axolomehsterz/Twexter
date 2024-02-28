package com.teamAxolomeh.twexter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  @RequestMapping("/api")
  public String getApi() {
    return "Hello apiiiiiiiiiii";
  }

  @RequestMapping("/api2")
  public String getAnotherApi() {
    return "Got api 2";
  }

}
