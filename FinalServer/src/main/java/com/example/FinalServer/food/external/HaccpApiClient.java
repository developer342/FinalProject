package com.example.FinalServer.food.external;


import com.example.FinalServer.food.dto.HaccpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class HaccpApiClient {

  @Qualifier("haccpWebClient")
  private final WebClient haccpWebClient;
  private final ObjectMapper objectMapper;

  public HaccpResponse searchByName(String serviceKey, String prdlstNm, int pageNo, int numOfRows) {
    String body = haccpWebClient.get()
            .uri(b -> b
                    .path("/getCertImgListServiceV3")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("returnType", "json")
                    .queryParam("prdlstNm", prdlstNm)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .build())
            .header(HttpHeaders.ACCEPT, "*/*")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    return parse(body);
  }

  public HaccpResponse getByReportNo(String serviceKey, String prdlstReportNo) {
    String body = haccpWebClient.get()
            .uri(b -> b
                    .path("/getCertImgListServiceV3")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("returnType", "json")
                    .queryParam("prdlstReportNo", prdlstReportNo)
                    .build())
            .header(HttpHeaders.ACCEPT, "*/*")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    return parse(body);
  }

  private HaccpResponse parse(String body) {
    try {
      return objectMapper.readValue(body, HaccpResponse.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse HACCP response", e);
    }
  }
}
