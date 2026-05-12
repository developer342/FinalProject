package com.example.app.common.config;

import com.example.FinalServer.common.config.HaccpApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class HaccpWebClientConfig {

  private final HaccpApiProperties properties;

  @Bean
  public WebClient haccpWebClient() {
    HttpClient http = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
    return WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(http))
            .build();
  }
}
