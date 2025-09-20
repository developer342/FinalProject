package com.example.FinalServer.food.external;

import com.example.FinalServer.food.dto.HaccpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

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
    return parseFlexible(body);
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
    return parseFlexible(body);
  }

  private HaccpResponse parseFlexible(String body) {
    try {
      JsonNode root = objectMapper.readTree(body == null ? "" : body);
      JsonNode bodyNode = pick(root, "body", "BODY");
      JsonNode itemsNode = pick(bodyNode, "items", "ITEMS");

      ArrayNode itemArray = objectMapper.createArrayNode();
      if (itemsNode != null) {
        if (itemsNode.isArray()) {
          itemArray = (ArrayNode) itemsNode;
        } else if (itemsNode.isObject()) {
          JsonNode wrapped = pick(itemsNode, "item", "ITEM");
          if (wrapped != null) {
            if (wrapped.isArray()) itemArray = (ArrayNode) wrapped;
            else if (wrapped.isObject()) itemArray.add(wrapped);
          }
        }
      }

      List<HaccpResponse.Item> items = new ArrayList<>();
      for (JsonNode node : itemArray) {
        JsonNode n = pick(node, "item", "ITEM");
        if (n == null || n.isNull()) n = node;
        HaccpResponse.Item it = HaccpResponse.Item.builder()
                .prdlstReportNo(text(n, "prdlstReportNo", "PRDLST_REPORT_NO"))
                .prdlstNm(text(n, "prdlstNm", "PRDLST_NM"))
                .prdkind(text(n, "prdkind", "PRDKIND"))
                .manufacture(text(n, "manufacture", "MANUFACTURE"))
                .seller(text(n, "seller", "SELLER"))
                .capacity(text(n, "capacity", "CAPACITY", "PRDLST_STANDARD"))
                .barcode(text(n, "barcode", "BARCODE"))
                .imgurl1(text(n, "imgurl1", "IMGURL1", "IMG_URL1"))
                .rawmtrl(text(n, "rawmtrl", "RAWMTRL", "RAWMTRL_NM"))
                .allergy(text(n, "allergy", "ALLERGY", "ALLERGY_INFO"))
                .build();
        items.add(it);
      }

      HaccpResponse.Body bodyDto = HaccpResponse.Body.builder()
              .pageNo(asInt(bodyNode, "pageNo", "PAGENO"))
              .numOfRows(asInt(bodyNode, "numOfRows", "NUMOFROWS"))
              .totalCount(asInt(bodyNode, "totalCount", "TOTALCOUNT"))
              .items(items)
              .build();

      return HaccpResponse.builder().body(bodyDto).build();
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse HACCP response", e);
    }
  }

  private JsonNode pick(JsonNode node, String... keys) {
    if (node == null) return null;
    for (String k : keys) {
      JsonNode n = node.get(k);
      if (n != null && !n.isNull()) return n;
    }
    return null;
  }

  private String text(JsonNode node, String... keys) {
    JsonNode n = pick(node, keys);
    if (n == null || n.isNull()) return null;
    if (n.isTextual()) return n.textValue();
    return n.asText();
  }

  private Integer asInt(JsonNode node, String... keys) {
    JsonNode n = pick(node, keys);
    if (n == null || n.isNull()) return null;
    if (n.isInt()) return n.intValue();
    if (n.isTextual()) {
      try { return Integer.parseInt(n.textValue()); } catch (NumberFormatException ignored) {}
    }
    return null;
  }
}
