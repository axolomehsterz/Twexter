package com.teamAxolomeh.twexter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GreetingController {

  @RequestMapping(value = "/**/{[path:[^\\.]*}")
  public String forwardToRounting() {
    return "forward:/index.html";
  }

}