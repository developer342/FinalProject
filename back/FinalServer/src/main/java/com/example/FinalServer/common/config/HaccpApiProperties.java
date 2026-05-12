package com.example.FinalServer.common.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "haccp.api")
public class HaccpApiProperties {

  private String key;
  private String baseUrl;
  private int timeoutSeconds;
}
