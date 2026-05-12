package com.example.finalapp.ledger.parse.util;

import java.text.Normalizer;

public class LedgerTextCleaner {

    private LedgerTextCleaner() {}

    /** OCR 원문을 거의 그대로 반환
     * - 유니코드 정규화(NFKC)만 적용
     * - CRLF/CR → \n 통일
     * - 제어문자(개행 제외)만 제거
     * - 줄/공백은 건드리지 않음
     */
    public static String preserveRaw(String raw) {
        if (raw == null) return "";
        String s = Normalizer.normalize(raw, Normalizer.Form.NFKC);

        // 개행 통일
        s = s.replace("\r\n", "\n").replace('\r', '\n');
        // 개행 제외 제어문자 제거
        s = s.replaceAll("[\\p{Cntrl}&&[^\\n]]", "");

        return s;
    }


}

