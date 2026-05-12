package com.example.FinalServer.ledger.controller;

import com.example.FinalServer.auth.entity.User;
import com.example.FinalServer.auth.repository.UserRepository;
import com.example.FinalServer.common.util.JwtTokenProvider;
import com.example.FinalServer.ledger.dto.EntryRequest;
import com.example.FinalServer.ledger.dto.EntryResponse;
import com.example.FinalServer.ledger.dto.ParseRequest;
import com.example.FinalServer.ledger.dto.ParseResponse;
import com.example.FinalServer.ledger.service.EntryService;
import com.example.FinalServer.ledger.service.ParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;
    private final ParseService parseService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;

    /**
     * 가계부 항목 직접 저장 (로그인 사용자 연동)
     */
    @PostMapping
    public EntryResponse create(
            @RequestBody EntryRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 1. Authorization 헤더에서 Bearer 토큰 추출
        String token = authHeader.replace("Bearer ", "");

        // 2. JWT 토큰에서 userId 추출
        Long userId = jwtProvider.getUserId(token);

        // 3. userId로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        // 4. DB에 저장 (EntryService 호출 시 user 포함)
        return entryService.create(request, user);
    }

    /** 기간별 목록 조회(페이지네이션) */
    @GetMapping
    public Page<EntryResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        LocalDate start = (from != null) ? from : LocalDate.now().minusDays(30);
        LocalDate end = (to != null) ? to : LocalDate.now();
        return entryService.findRange(start, end, pageable);
    }

    /** 기간별 CSV 다운로드 */
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response
    ) {
        LocalDate start = (from != null) ? from : LocalDate.now().minusDays(30);
        LocalDate end = (to != null) ? to : LocalDate.now();
        entryService.exportCsv(start, end, response);
    }

    /** 기간별 XLSX 다운로드 (CSV와 컬럼 동일: 날짜,상호명,카테고리,결제수단,총액) */
    @GetMapping("/export.xlsx")
    public void exportXlsx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response
    ) throws IOException {
        LocalDate start = (from != null) ? from : LocalDate.now().minusDays(30);
        LocalDate end   = (to != null) ? to   : LocalDate.now();
        entryService.exportXlsx(start, end, response);
    }

    @PostMapping("/parse")
    public EntryResponse parseAndSave(
            @RequestBody ParseRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 1. Authorization 헤더에서 Bearer 토큰 추출
        String token = authHeader.replace("Bearer ", "");

        // 2. JWT에서 userId 추출
        Long userId = jwtProvider.getUserId(token);

        // 3. userId로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        // 4. EntryRequest 구성 (rawText만 포함)
        EntryRequest entryReq = EntryRequest.builder()
                .rawText(request.getRawText()) // 나머지는 Service에서 자동 파싱
                .build();

        // 5. DB 저장 (Service가 자동 파싱 및 품목저장 수행)
        EntryResponse saved = entryService.create(entryReq, user);

        // 6. 결과 반환 (저장된 항목 포함)
        return saved;
    }
}
