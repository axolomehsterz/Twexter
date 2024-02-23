package com.example.teamaxolomeh.twexter;
// package com.example.teamaxolomeh.controller;

import org.springframework.stereotype.Controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

// @Controller
// public class HomeController {
//     @RequestMapping(value = "/{path:[^\\.]*}")
//     public String redirect() {
//         System.out.println("working");
//         return "forward:/";
//     }
// }

@Controller
public class HomeController {

    // @GetMapping("")
    // public ModelAndView home() {
    //     ModelAndView mav=new ModelAndView("index");
    //     return mav;
    // }

    // @RequestMapping("/error")
    // public String error() {
    //     System.out.println("I am error!");
    //     return "forward:/";
    // }
}
