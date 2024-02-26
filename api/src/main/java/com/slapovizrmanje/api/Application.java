package com.slapovizrmanje.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.slapovizrmanje")
public class Application {

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

}