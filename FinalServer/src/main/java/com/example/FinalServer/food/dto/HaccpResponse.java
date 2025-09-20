package com.example.FinalServer.food.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HaccpResponse {

  @JsonProperty("body")
  private Body body;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Body {

    @JsonProperty("pageNo")
    @JsonAlias({"PAGENO"})
    private Integer pageNo;

    @JsonProperty("numOfRows")
    @JsonAlias({"NUMOFROWS"})
    private Integer numOfRows;

    @JsonProperty("totalCount")
    @JsonAlias({"TOTALCOUNT"})
    private Integer totalCount;

    // 일부 응답은 items가 배열, 일부는 { item: [...] } 구조일 수 있어
    // 여기서는 배열 형태를 기본으로 두고, 파서는 HaccpApiClient에서 그대로 사용
    @JsonProperty("items")
    @JsonAlias({"ITEMS"})
    private List<Item> items;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Item {

    @JsonProperty("prdlstReportNo")
    @JsonAlias({"PRDLST_REPORT_NO"})
    private String prdlstReportNo;

    @JsonProperty("prdlstNm")
    @JsonAlias({"PRDLST_NM"})
    private String prdlstNm;

    @JsonProperty("prdkind")
    @JsonAlias({"PRDKIND", "PRDLST_DCNM", "PRDLST_CDNM"})
    private String prdkind;

    @JsonProperty("manufacture")
    @JsonAlias({"MANUFACTURE", "BSSH_NM"})
    private String manufacture;

    @JsonProperty("seller")
    @JsonAlias({"SELLER", "SALER_NM"})
    private String seller;

    @JsonProperty("capacity")
    @JsonAlias({"CAPACITY", "PRDLST_STANDARD"})
    private String capacity;

    @JsonProperty("barcode")
    @JsonAlias({"BARCODE"})
    private String barcode;

    @JsonProperty("imgurl1")
    @JsonAlias({"IMGURL1", "IMG_URL1"})
    private String imgurl1;

    @JsonProperty("rawmtrl")
    @JsonAlias({"RAWMTRL", "RAWMTRL_NM"})
    private String rawmtrl;

    @JsonProperty("allergy")
    @JsonAlias({"ALLERGY", "ALLERGY_INFO"})
    private String allergy;
  }
}
