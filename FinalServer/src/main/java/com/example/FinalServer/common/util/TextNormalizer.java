package com.example.FinalServer.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextNormalizer {

  public static List<String> tokenize(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }
    return Arrays.stream(text.split("[,;/·]"))
            .map(s -> s.replaceAll("\\(.*?\\)", "")) // 괄호 제거
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
  }
}
