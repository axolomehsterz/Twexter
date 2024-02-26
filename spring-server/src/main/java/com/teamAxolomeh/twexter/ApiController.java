package com.teamAxolomeh.twexter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  @RequestMapping("/api")
  public String getApi() {
    System.out.println("working");
    return "Hello apiiiiiiiiiii";
  }

  @RequestMapping("/api2")
  public String getAnotherApi() {
    System.out.println("working");
    return "Got api 2";
  }

}
