package com.example.FinalServer.food.controller;


import com.example.FinalServer.food.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ImportController {

  private final ImportService importService;

  @GetMapping("/admin/import/haccp")
  public String importHaccp(
          @RequestParam(defaultValue = "") String prdlstNm,
          @RequestParam(defaultValue = "1") int pageNo,
          @RequestParam(defaultValue = "10") int numOfRows
  ) {
    importService.importPage(prdlstNm, pageNo, numOfRows);
    return "Import executed for page " + pageNo + " with " + numOfRows + " rows.";
  }
}
