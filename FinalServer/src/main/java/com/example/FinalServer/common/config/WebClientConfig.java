package com.example.FinalServer.common.config;



import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {

  private final HaccpApiProperties properties;

  public WebClientConfig(HaccpApiProperties properties) {
    this.properties = properties;
  }

  @Bean
  public WebClient haccpWebClient() {
    HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

    return WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
  }
}
