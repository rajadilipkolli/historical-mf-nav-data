package com.github.rajadilipkolli.dailynav.app;

import com.github.rajadilipkolli.dailynav.config.DailyNavAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DailyNavAutoConfiguration.class)
public class DailyNavApplication {

  public static void main(String[] args) {
    SpringApplication.run(DailyNavApplication.class, args);
  }
}
