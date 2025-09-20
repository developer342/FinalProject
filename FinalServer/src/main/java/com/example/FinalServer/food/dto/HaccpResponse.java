package com.example.FinalServer.food.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HaccpResponse {

  @JsonProperty("body")
  private Body body;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Body {

    @JsonProperty("pageNo")
    private Integer pageNo;

    @JsonProperty("numOfRows")
    private Integer numOfRows;

    @JsonProperty("totalCount")
    private Integer totalCount;

    @JsonProperty("items")
    private List<Item> items;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Item {

    @JsonProperty("prdlstReportNo")
    private String prdlstReportNo;

    @JsonProperty("prdlstNm")
    private String prdlstNm;

    @JsonProperty("prdkind")
    private String prdkind;

    @JsonProperty("manufacture")
    private String manufacture;

    @JsonProperty("seller")
    private String seller;

    @JsonProperty("capacity")
    private String capacity;

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("imgurl1")
    private String imgurl1;

    @JsonProperty("rawmtrl")
    private String rawmtrl;

    @JsonProperty("allergy")
    private String allergy;
  }
}
