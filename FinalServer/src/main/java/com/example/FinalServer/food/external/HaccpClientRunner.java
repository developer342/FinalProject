package com.example.FinalServer.food.external;


import com.example.FinalServer.common.config.HaccpApiProperties;
import com.example.FinalServer.food.dto.HaccpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "haccp.runner.enabled", havingValue = "false")
@Profile("dev")
public class HaccpClientRunner implements ApplicationRunner {

  private final HaccpApiProperties properties;
  private final HaccpApiClient haccpApiClient;

  @Override
  public void run(ApplicationArguments args) {
    String key = properties.getKey();
    HaccpResponse res = haccpApiClient.searchByName(key, "초코", 1, 10);

    int count = res != null
            && res.getBody() != null
            && res.getBody().getItems() != null
            ? res.getBody().getItems().size()
            : 0;

    log.info("HACCP search items count: {}", count);
  }
}
