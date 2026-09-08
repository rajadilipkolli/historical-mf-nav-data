package com.github.rajadilipkolli.dailynav.app;

import com.github.rajadilipkolli.dailynav.config.DailyNavAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DailyNavAutoConfiguration.class)
public class DailyNavApplication {

  /**
   * Launches the DailyNav Spring Boot application.
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(DailyNavApplication.class, args);
  }
}
