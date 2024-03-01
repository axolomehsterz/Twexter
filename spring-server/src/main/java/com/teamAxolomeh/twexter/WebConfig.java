package com.teamAxolomeh.twexter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private IsLoggedInInterceptor isLoggedInInterceptor;

  public WebConfig(IsLoggedInInterceptor isLoggedInInterceptor) {
    this.isLoggedInInterceptor = isLoggedInInterceptor;
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    if (isLoggedInInterceptor != null) {
      registry.addInterceptor(isLoggedInInterceptor)
          .addPathPatterns("/twext");
    } 
  }
}
