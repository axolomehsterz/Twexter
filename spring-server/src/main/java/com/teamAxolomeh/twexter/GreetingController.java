package com.teamAxolomeh.twexter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

  @RequestMapping(value = "/**/{[path:[^\\.]*}")
  public String forwardToRounting() {
    return "forward:/index.html";
  }

}