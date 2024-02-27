package com.teamAxolomeh.twexter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private IsLoggedInInterceptor isLoggedInInterceptor;

  @Autowired
  public WebConfig(IsLoggedInInterceptor isLoggedInInterceptor) {
    this.isLoggedInInterceptor = isLoggedInInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(isLoggedInInterceptor)
        .addPathPatterns("/home");
  }
}
