package com.example.FinalServer.gpt.service;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptService {
	
	@Value("${openai.api.key}")
	private String apiKey;
	
	private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
	
	/**
	 * GPT API 호출 (OCR 텍스트를 영수증 형식으로 정제)
	 */
	public String refineReceiptText(String rawText) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(apiKey);
			
			String systemPrompt =
					"너는 OCR로 인식된 영수증 텍스트를 사람이 읽기 쉽고, 서버에서 파싱하기 쉬운 구조로 정리하는 보조자야.\n\n" +
							
							"출력은 반드시 아래 순서를 따른다:\n" +
							"1) 상호명: [매장명 또는 가맹점명]\n" +
							"2) 날짜: [YYYY-MM-DD 형식]\n" +
							"3) 결제방법: [카드, 현금, 간편결제 등 / 명확하지 않으면 '없음']\n" +
							"4) 항목 목록 — 표 머리(상품명, 단가, 수량, 금액)를 포함한다.\n" +
							"5) 합계: [숫자]원\n" +
							"6) 카테고리: [식비, 음료, 간식, 생활용품, 교통, 문화, 기타 중 하나]\n\n" +
							
							"출력 규칙:\n" +
							"- 영수증에 존재하지 않는 정보는 절대 추측하거나 임의로 추가하지 않는다.\n" +
							"- 결제방법 관련 단어가 없으면 반드시 ‘결제방법: 없음’으로 출력한다.\n" +
							"- 표 형태를 만들기 위한 특수문자(│, ─, |, =)는 사용하지 않는다.\n" +
							"- 항목은 공백 기반 정렬만 유지한다.\n" +
							"- 상호명은 ‘영수증’, ‘합계’, ‘전화번호’, ‘주소’, ‘메뉴’ 등의 문장을 제외한다.\n" +
							"- 날짜는 OCR에서 발견된 값을 그대로 사용한다.\n" +
							"- 금액은 숫자만 쓰거나 ‘원’을 붙여도 된다.\n\n" +
							
							"상품 목록은 반드시 아래 형식을 따른다:\n" +
							"  상품명   단가   수량   금액\n" +
							"  콜라 500ml   1500    2    3000\n" +
							"  감자칩       2500    1    2500\n\n" +
							
							"- 각 영수증(상호명~카테고리)은 하나의 묶음으로 출력한다.\n" +
							"- 인식되지 않은 항목은 ‘없음’으로 표시한다.\n" +
							"- 카테고리는 영수증 전체 특성을 보고 자연스럽게 결정한다.\n" +
							"  예: 음식·음료·식당·편의점 → 식비 / 버스·지하철·택시 → 교통 / 영화·책·게임 → 문화 / 생활용품 → 생활 / 그 외는 기타\n\n" +
							
							"출력 예시:\n" +
							"1) 상호명: 이마트24\n" +
							"2) 날짜: 2025-10-10\n" +
							"3) 결제방법: 카드\n" +
							"4)\n" +
							"상품명   단가   수량   금액\n" +
							"콜라 500ml   1500    2    3000\n" +
							"감자칩       2500    1    2500\n" +
							"5) 합계: 5500원\n" +
							"6) 카테고리: 식비";
			
			Map<String, Object> body = Map.of(
					"model", "gpt-4o-mini",
					"messages", List.of(
							Map.of("role", "system", "content", systemPrompt),
							Map.of("role", "user", "content", rawText)
					)
			);
			
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
			
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<Map> response =
					restTemplate.exchange(GPT_URL, HttpMethod.POST, request, Map.class);
			
			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				List<Map<String, Object>> choices =
						(List<Map<String, Object>>) response.getBody().get("choices");
				
				if (choices != null && !choices.isEmpty()) {
					Map<String, Object> message =
							(Map<String, Object>) choices.get(0).get("message");
					
					if (message != null && message.containsKey("content")) {
						return message.get("content").toString().trim();
					}
				}
			}
			
			return "GPT 응답이 비어 있습니다.";
			
		} catch (Exception e) {
			return "GPT 요청 실패: " + e.getMessage();
		}
	}
	
	
	/**
	 * 서버 시작 시 환경변수(GPT 키) 확인용
	 */
	@PostConstruct
	public void checkKey() {
		System.out.println("GPT KEY loaded: " + (apiKey != null));
	}
}