package com.example.FinalServer.food.service;

import com.example.FinalServer.common.config.HaccpApiProperties;
import com.example.FinalServer.common.util.HashUtil;
import com.example.FinalServer.common.util.TextNormalizer;
import com.example.FinalServer.food.dto.HaccpResponse;
import com.example.FinalServer.food.entity.*;
import com.example.FinalServer.food.external.HaccpApiClient;
import com.example.FinalServer.food.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

  private final HaccpApiProperties properties;
  private final HaccpApiClient haccpApiClient;
  private final ProductRepository productRepository;
  private final IngredientRepository ingredientRepository;
  private final ProductIngredientRepository productIngredientRepository;
  private final AllergenRepository allergenRepository;
  private final ProductAllergenRepository productAllergenRepository;
  private final SourceItemRepository sourceItemRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void importPage(String prdlstNm, int pageNo, int numOfRows) {
    String key = properties.getKey();
    log.info("=== [IMPORT START] pageNo={} numOfRows={} keyword='{}' ===", pageNo, numOfRows, prdlstNm);

    HaccpResponse res = haccpApiClient.searchByName(key, prdlstNm, pageNo, numOfRows);
    if (res == null || res.getBody() == null || res.getBody().getItems() == null) {
      log.warn("*** [IMPORT SKIP] no data (pageNo={})", pageNo);
      return;
    }

    res.getBody().getItems().forEach(item -> {
      String reportNo = nvl(item.getPrdlstReportNo());
      String name = nvl(item.getPrdlstNm());

      log.info("=== [ITEM START] pageNo={} reportNo='{}' name='{}' ===", pageNo, reportNo, name);
      if (reportNo.isEmpty()) {
        log.warn("*** [SKIP] missing reportNo");
        return;
      }

      Set<String> ingredients = new LinkedHashSet<>(TextNormalizer.tokenize(item.getRawmtrl()));
      Set<String> allergens = new LinkedHashSet<>(TextNormalizer.tokenize(item.getAllergy()));
      List<String> ingredientsSorted = new ArrayList<>(ingredients);
      List<String> allergensSorted = new ArrayList<>(allergens);
      Collections.sort(ingredientsSorted);
      Collections.sort(allergensSorted);

      String canonical = String.join("|",
              nvl(item.getPrdlstNm()),
              nvl(item.getPrdkind()),
              nvl(item.getManufacture()),
              nvl(item.getSeller()),
              nvl(item.getCapacity()),
              nvl(item.getBarcode()),
              nvl(item.getImgurl1()),
              String.join(",", ingredientsSorted),
              String.join(",", allergensSorted)
      );
      String hash = HashUtil.sha256(canonical);

      log.info(">>> [DEDUPE CHECK] externalId='{}'", reportNo);
      if (sourceItemRepository.existsByExternalIdAndHash(reportNo, hash)) {
        log.info("*** [SKIP] unchanged reportNo='{}' name='{}'", reportNo, name);
        return;
      }

      var existing = productRepository.findByPrdlstReportNo(reportNo);
      boolean isNewProduct = existing.isEmpty();

      Product product = existing.orElseGet(() -> productRepository.save(
              Product.builder()
                      .prdlstReportNo(reportNo)
                      .name(nvl(item.getPrdlstNm()))
                      .manufacturer(nvl(item.getManufacture()))
                      .seller(nvl(item.getSeller()))
                      .kind(nvl(item.getPrdkind()))
                      .capacity(nvl(item.getCapacity()))
                      .barcode(nvl(item.getBarcode()))
                      .imgUrl(nvl(item.getImgurl1()))
                      .build()
      ));

      if (isNewProduct) {
        for (String ing : ingredients) {
          Ingredient ingredient = ingredientRepository.findByName(ing)
                  .orElseGet(() -> ingredientRepository.save(Ingredient.builder().name(ing).build()));
          productIngredientRepository.save(
                  ProductIngredient.builder().product(product).ingredient(ingredient).build()
          );
        }
        for (String alg : allergens) {
          Allergen allergen = allergenRepository.findByName(alg)
                  .orElseGet(() -> allergenRepository.save(Allergen.builder().name(alg).build()));
          productAllergenRepository.save(
                  ProductAllergen.builder().product(product).allergen(allergen).build()
          );
        }
      }

      try {
        String rawJson = objectMapper.writeValueAsString(item);
        sourceItemRepository.save(
                SourceItem.builder()
                        .source("HACCP")
                        .externalId(reportNo)
                        .rawJson(rawJson)
                        .hash(hash)
                        .fetchedAt(Instant.now())
                        .build()
        );
      } catch (Exception e) {
        throw new RuntimeException("Failed to persist SourceItem", e);
      }

      log.info("--- [ITEM DONE] reportNo='{}' name='{}' ---", reportNo, product.getName());
    });

    log.info("=== [IMPORT DONE] pageNo={} count={} ===", pageNo, res.getBody().getItems().size());
  }

  private String nvl(String s) {
    return s == null ? "" : s.trim();
  }
}
