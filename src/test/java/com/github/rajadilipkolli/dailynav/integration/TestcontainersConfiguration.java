package com.github.rajadilipkolli.dailynav.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.BindMode;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  OllamaContainer ollama() {
    return new OllamaContainer(DockerImageName.parse("ollama/ollama"))
        .withFileSystemBind(
            System.getProperty("user.home") + "/.ollama", "/root/.ollama", BindMode.READ_WRITE);
  }
}
