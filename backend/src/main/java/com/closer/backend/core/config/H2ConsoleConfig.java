package com.closer.backend.core.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "local", "desarrollo" })
public class H2ConsoleConfig {

  @Bean
  ServletRegistrationBean<JakartaWebServlet> h2ConsoleServlet() {
    // En Spring Boot 4 se registra manualmente para exponer /h2-console.
    ServletRegistrationBean<JakartaWebServlet> registrationBean = new ServletRegistrationBean<>(new JakartaWebServlet(),
        "/h2-console/*");
    registrationBean.addInitParameter("webAllowOthers", "false");
    registrationBean.addInitParameter("trace", "false");
    registrationBean.setLoadOnStartup(1);
    return registrationBean;
  }
}
