package com.example.FinalServer.ledger.service;

import com.example.FinalServer.auth.entity.User;
import com.example.FinalServer.ledger.dto.EntryRequest;
import com.example.FinalServer.ledger.dto.EntryResponse;
import com.example.FinalServer.ledger.dto.ParseRequest;
import com.example.FinalServer.ledger.dto.ParseResponse;
import com.example.FinalServer.ledger.entity.Entry;
import com.example.FinalServer.ledger.entity.EntryItem;
import com.example.FinalServer.ledger.repository.EntryItemRepository;
import com.example.FinalServer.ledger.repository.EntryRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EntryService {
    
    private final EntryRepository entryRepository;
    private final EntryItemRepository entryItemRepository;
    private final ParseService parseService;
    
    /**
     * 가계부 항목 저장 (로그인 사용자 연동 + 기본값 보정 + rawText 자동 파싱 + 품목 저장)
     */
    @Transactional
    public EntryResponse create(EntryRequest req, User user) {
        
        // rawText 파싱
        if (req.getRawText() != null && !req.getRawText().isBlank()) {
            ParseRequest parseReq = new ParseRequest(req.getRawText());
            ParseResponse parsed = parseService.parse(parseReq);
            
            if (parsed != null) {
                req.setMerchant(parsed.getMerchant());
                
                if (parsed.getDate() != null && parsed.getDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
                    try {
                        req.setDate(LocalDate.parse(parsed.getDate()));
                    } catch (Exception ignored) {
                        req.setDate(null);
                    }
                }
                
                req.setTotal(parsed.getTotal());
                req.setPaymentMethod(parsed.getPaymentMethod());
                req.setCategory(parsed.getCategory());
                req.setItems(parsed.getItems());
            }
        }
        
        // 기본값 보정
        if (req.getDate() == null) req.setDate(LocalDate.now());
        req.setMerchant(cap(req.getMerchant(), 120));
        req.setPaymentMethod(cap(req.getPaymentMethod(), 20));
        req.setCategory(cap(req.getCategory(), 30));
        req.setCurrency(cap(req.getCurrency(), 3));
        
        if (req.getRawText() != null && req.getRawText().length() > 200_000) {
            req.setRawText(req.getRawText().substring(0, 200_000));
        }
        
        if (req.getMerchant() == null) req.setMerchant("미상");
        if (req.getPaymentMethod() == null) req.setPaymentMethod("UNKNOWN");
        
        // ★ 변경: total이 null이면 품목(amount) 합계로 자동 보정
        if (req.getTotal() == null) {
            long sum = 0L;
            
            if (req.getItems() != null) {
                sum = req.getItems().stream()
                        .mapToLong(i -> {
                            if (i.getAmount() != null) return i.getAmount().longValue();
                            if (i.getPrice() != null && i.getQuantity() != null)
                                return (long) i.getPrice() * i.getQuantity();
                            return 0L;
                        })
                        .sum();
            }
            
            req.setTotal(sum);
        }
        
        Entry entry = req.toEntity();
        entry.setUser(user);
        
        Entry saved = entryRepository.save(entry);
        
        // 품목 저장
        if (req.getItems() != null && !req.getItems().isEmpty()) {
            List<EntryItem> items = req.getItems().stream()
                    .map(i -> EntryItem.builder()
                            .name(i.getName())
                            .quantity(i.getQuantity())
                            .price(i.getPrice())
                            .amount(i.getAmount() != null
                                    ? i.getAmount()
                                    : (i.getQuantity() != null && i.getPrice() != null
                                    ? i.getQuantity() * i.getPrice()
                                    : 0))
                            .entry(saved)
                            .build())
                    .toList();
            
            entryItemRepository.saveAll(items);
            saved.setItems(items);
        }
        
        return EntryResponse.from(saved);
    }
    
    /**
     * 기간 조건으로 항목 조회
     */
    @Transactional(readOnly = true)
    public Page<EntryResponse> findRange(LocalDate from, LocalDate to, Pageable pageable) {
        return entryRepository.findByEntryDateBetween(from, to, pageable).map(EntryResponse::from);
    }
    
    /**
     * 기간 조건으로 CSV 내보내기 (품목 포함)
     */
    @Transactional(readOnly = true)
    public void exportCsv(LocalDate from, LocalDate to, HttpServletResponse resp) {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"ledger_" + from + "-" + to + ".csv\"; filename*=UTF-8''ledger_" + from + "-" + to + ".csv");
        
        try (PrintWriter w = resp.getWriter()) {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
            w.write('\uFEFF'); // BOM (엑셀 한글깨짐 방지)
            
            List<Entry> entries = entryRepository.findByEntryDateBetween(from, to, Pageable.unpaged()).getContent();
            
            for (Entry e : entries) {
                w.println("날짜,상호명,결제수단,카테고리,총액");
                
                String date = csv(safe(e.getEntryDate() != null ? e.getEntryDate().toString() : ""));
                String merchant = csv(neutralizeForSheet(safe(e.getMerchant())));
                String pay = csv(neutralizeForSheet(safe(e.getPaymentMethod())));
                String category = csv(neutralizeForSheet(safe(e.getCategory())));
                String total = csv(nf.format(Objects.requireNonNullElse(e.getTotalAmount(), 0L)) + "원");
                
                w.println(String.join(",", date, merchant, pay, category, total));
                w.println();
                
                w.println("품목명,수량,단가,금액");
                
                if (e.getItems() != null && !e.getItems().isEmpty()) {
                    for (EntryItem item : e.getItems()) {
                        String name = csv(neutralizeForSheet(safe(item.getName())));
                        String qty = csv(String.valueOf(Objects.requireNonNullElse(item.getQuantity(), 0)));
                        String price = csv(nf.format(Objects.requireNonNullElse(item.getPrice(), 0)) + "원");
                        String amount = csv(nf.format(Objects.requireNonNullElse(item.getAmount(), 0)) + "원");
                        w.println(String.join(",", name, qty, price, amount));
                    }
                }
                w.println();
            }
            
            w.flush();
        } catch (Exception ex) {
            throw new RuntimeException("CSV export failed", ex);
        }
    }
    
    /**
     * 기간 조건으로 XLSX 내보내기 (품목 포함, SXSSF 안정 버전)
     */
    @Transactional(readOnly = true)
    public void exportXlsx(LocalDate from, LocalDate to, HttpServletResponse resp) {
        String filename = String.format("ledger_%s_%s.xlsx", from, to);
        
        resp.reset();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        try (SXSSFWorkbook wb = new SXSSFWorkbook(null, 100, true, true);
             OutputStream os = resp.getOutputStream()) {
            
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA); // 숫자 포맷
            Sheet sh = wb.createSheet("Ledger");
            int r = 0;
            
            List<Entry> entries = entryRepository.findByEntryDateBetween(from, to, Pageable.unpaged()).getContent();
            
            for (Entry e : entries) {
                Row header = sh.createRow(r++);
                header.createCell(0).setCellValue("날짜");
                header.createCell(1).setCellValue("상호명");
                header.createCell(2).setCellValue("결제수단");
                header.createCell(3).setCellValue("카테고리");
                header.createCell(4).setCellValue("총액");
                
                Row entryRow = sh.createRow(r++);
                entryRow.createCell(0).setCellValue(e.getEntryDate() != null ? e.getEntryDate().toString() : "");
                entryRow.createCell(1).setCellValue(neutralizeForSheet(safe(e.getMerchant())));
                entryRow.createCell(2).setCellValue(neutralizeForSheet(safe(e.getPaymentMethod())));
                entryRow.createCell(3).setCellValue(neutralizeForSheet(safe(e.getCategory())));
                entryRow.createCell(4).setCellValue(nf.format(Objects.requireNonNullElse(e.getTotalAmount(), 0L)) + "원");
                
                r++;
                
                Row itemHeader = sh.createRow(r++);
                itemHeader.createCell(0).setCellValue("품목명");
                itemHeader.createCell(1).setCellValue("수량");
                itemHeader.createCell(2).setCellValue("단가");
                itemHeader.createCell(3).setCellValue("금액");
                
                if (e.getItems() != null && !e.getItems().isEmpty()) {
                    for (EntryItem item : e.getItems()) {
                        Row itemRow = sh.createRow(r++);
                        itemRow.createCell(0).setCellValue(neutralizeForSheet(safe(item.getName())));
                        itemRow.createCell(1).setCellValue(Objects.requireNonNullElse(item.getQuantity(), 0));
                        itemRow.createCell(2).setCellValue(nf.format(Objects.requireNonNullElse(item.getPrice(), 0)) + "원");
                        itemRow.createCell(3).setCellValue(nf.format(Objects.requireNonNullElse(item.getAmount(), 0)) + "원");
                    }
                }
                
                r++;
            }
            
            for (int i = 0; i <= 4; i++) {
                sh.setColumnWidth(i, 20 * 256);
            }
            
            wb.write(os);
            os.flush();
            wb.dispose();
        } catch (Exception ex) {
            throw new RuntimeException("XLSX export failed", ex);
        }
    }
    
    private static String cap(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }
    
    /**
     * Excel/CSV 수식 주입 방지를 위한 안전 문자열 변환 (강화 통합판)
     * (=, +, -, @, ;, ', " 로 시작하거나 유니코드 공백·제어문자 포함 시 ' 접두)
     */
    private String neutralizeForSheet(String value) {
        if (value == null) return "";
        
        String trimmed = value.stripLeading();
        Pattern dangerous = Pattern.compile("^[\\p{C}\\p{Z}\\s]*[=+\\-@;'\"]", Pattern.UNICODE_CASE);
        
        if (dangerous.matcher(trimmed).find()) {
            return "'" + value;
        }
        return value;
    }
    
    /**
     * CSV 형식에 맞게 문자열 변환
     */
    private String csv(String s) {
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needQuote ? "\"" + v + "\"" : v;
    }
    
    /**
     * null-safe 문자열 반환
     */
    private String safe(String s) {
        return s == null ? "" : s;
    }
    
}
