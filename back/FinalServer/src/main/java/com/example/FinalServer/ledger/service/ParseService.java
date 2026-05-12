package com.example.FinalServer.ledger.service;

import com.example.FinalServer.ledger.dto.EntryItemRequest;
import com.example.FinalServer.ledger.dto.EntryItemResponse;
import com.example.FinalServer.ledger.dto.ParseRequest;
import com.example.FinalServer.ledger.dto.ParseResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * OCR 결과 텍스트를 구조화하여 상호명, 날짜, 결제방법, 품목목록, 합계, 카테고리를 추출
 */
@Service
public class ParseService {
    
    private static final int MERCHANT_MAX = 120;
    private static final int PAYMETHOD_MAX = 20;
    private static final int CATEGORY_MAX = 30;
    
    /** GPT 정제 텍스트 파싱 (DB 저장은 EntryService에서 처리) */
    public ParseResponse parse(ParseRequest req) {
        if (req == null || req.getRawText() == null || req.getRawText().isBlank()) {
            throw new IllegalArgumentException("rawText is required");
        }
        String txt = req.getRawText().trim();
        return parseStructuredText(txt);
    }
    
    /** GPT 정제 텍스트 전용 파서 (A형 + B형 가능한 것만 처리) */
    private ParseResponse parseStructuredText(String text) {
        List<String> lines = text.lines()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());
        
        String merchant = null;
        String payment = null;
        Long total = null;
        String category = null;
        LocalDate date = null;
        
        List<EntryItemRequest> items = new ArrayList<>();
        boolean inItemSection = false;
        
        Pattern merchantPattern = Pattern.compile("상호명\\s*[:：]\\s*(.+)");
        Pattern datePattern = Pattern.compile("(\\d{4}[-./]\\d{1,2}[-./]\\d{1,2})");
        Pattern paymentPattern = Pattern.compile("결제.?방법\\s*[:：]\\s*(.+)");
        Pattern totalPattern = Pattern.compile("합계\\s*[:：]?\\s*([\\d,]+)원?");
        Pattern categoryPattern = Pattern.compile("카테고리\\s*[:：]\\s*(.+)");
        
        Pattern tableItemPattern = Pattern.compile("^(.+?)\\s+(\\d{2,6})\\s+(\\d{1,3})\\s+(\\d{2,8})$");
        Pattern simpleQtyPattern = Pattern.compile("^(.+?)\\s+(\\d{1,2})\\s+(\\d{3,8})$");
        Pattern simplePricePattern = Pattern.compile("^(.+?)\\s+(\\d{2,8})$");
        
        for (String line : lines) {
            
            if (line.contains("상품명") && line.contains("금액")) {
                inItemSection = true;
                continue;
            }
            
            if (inItemSection) {
                
                if (line.contains("합계") || line.contains("총액")) {
                    Matcher mt = totalPattern.matcher(line);
                    if (mt.find()) total = parseMoneyToken(mt.group(1));
                    inItemSection = false;
                    continue;
                }
                
                Matcher mA = tableItemPattern.matcher(line);
                if (mA.find()) {
                    String name = cleanInline(mA.group(1));
                    Integer price = parseIntSafe(mA.group(2));
                    Integer qty = parseIntSafe(mA.group(3));
                    Integer amt = parseIntSafe(mA.group(4));
                    items.add(EntryItemRequest.builder()
                            .name(name)
                            .quantity(qty)
                            .price(price)
                            .amount(amt)
                            .build());
                    continue;
                }
                
                Matcher mB1 = simpleQtyPattern.matcher(line);
                if (mB1.find()) {
                    String name = cleanInline(mB1.group(1));
                    Integer qty = parseIntSafe(mB1.group(2));
                    Integer amt = parseIntSafe(mB1.group(3));
                    items.add(EntryItemRequest.builder()
                            .name(name)
                            .quantity(qty)
                            .price(amt)
                            .amount(amt)
                            .build());
                    continue;
                }
                
                Matcher mB2 = simplePricePattern.matcher(line);
                if (mB2.find()) {
                    String name = cleanInline(mB2.group(1));
                    Integer amt = parseIntSafe(mB2.group(2));
                    items.add(EntryItemRequest.builder()
                            .name(name)
                            .quantity(1)
                            .price(amt)
                            .amount(amt)
                            .build());
                    continue;
                }
                
                continue;
            }
            
            Matcher m1 = merchantPattern.matcher(line);
            if (m1.find() && merchant == null) {
                merchant = cleanInline(m1.group(1));
                continue;
            }
            
            Matcher m2 = datePattern.matcher(line);
            if (m2.find() && date == null) {
                date = parseDate(m2.group(1));
                continue;
            }
            
            Matcher m3 = paymentPattern.matcher(line);
            if (m3.find() && payment == null) {
                payment = mapPayment(m3.group(1));
                continue;
            }
            
            Matcher m4 = totalPattern.matcher(line);
            if (m4.find() && total == null) {
                total = parseMoneyToken(m4.group(1));
                continue;
            }
            
            Matcher m5 = categoryPattern.matcher(line);
            if (m5.find() && category == null) {
                category = cleanInline(m5.group(1));
            }
        }
        
        if (merchant == null) merchant = inferMerchant(lines);
        if (date == null) date = LocalDate.now();
        
        return ParseResponse.builder()
                .date(date.format(DateTimeFormatter.ISO_DATE))
                .merchant(clip(merchant, MERCHANT_MAX))
                .total(total)
                .paymentMethod(clip(payment, PAYMETHOD_MAX))
                .category(clip(category, CATEGORY_MAX))
                .items(items)
                .confidence(1.0)
                .build();
    }
    
    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
    
    private LocalDate parseDate(String s) {
        if (s == null) return null;
        s = s.replace('.', '-').replace('/', '-');
        try {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yy-MM-dd"));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
    
    private String mapPayment(String t) {
        if (t == null) return null;
        String low = t.toLowerCase(Locale.ROOT);
        if (low.contains("없음")) return null;
        if (low.contains("현금")) return "CASH";
        if (low.contains("카드") || low.contains("신용") || low.contains("체크")) return "CARD";
        if (low.contains("페이")) return "PAY";
        return null;
    }
    
    private String inferMerchant(List<String> lines) {
        for (String s : lines) {
            String clean = s.replaceAll("\\s+", "");
            if (clean.length() < 2) continue;
            if (clean.matches(".*(영수증|합계|항목|결제|날짜|메뉴|상품명|테이블|카테고리).*")) continue;
            if (clean.matches(".*\\d{2,3}-\\d{3,4}-\\d{4}.*")) continue;
            if (clean.matches(".*(http|https)://.*")) continue;
            return cleanInline(s);
        }
        return null;
    }
    
    private Long parseMoneyToken(String tok) {
        if (tok == null || tok.isEmpty()) return null;
        String digits = tok.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String cleanInline(String s) {
        return s == null ? "" : s.replaceAll("[\\t\\r\\n]+", " ").trim();
    }
    
    private String clip(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }
}


