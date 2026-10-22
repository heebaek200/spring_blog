package com.tenco.spring_blog.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController // @Controller + @ResponseBody
public class RestAgentController {

    @Value("${agent.gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 제미나이에게 묻는다. 이 내용을 교정해달라고.
    @PostMapping("/api/agent/proofread")
    public ResponseEntity<String> proofread(
            @RequestParam(name = "content") String content) {

        // 요청할 외부 API 주소
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

        // 요청 헤더 작성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        // 요청 바디 작성
        ProofreadRequestDto requestDto = new ProofreadRequestDto(
                List.of(new ProofreadRequestDto.Content(
                        List.of(new ProofreadRequestDto.Content.Part(
                                "다음 블로그 내용의 맞춤법과 띄어쓰기를 교정해 주세요. 몇 줄 띄워서 간단한 농담도 던져주세요.\\n\\n" +
                                content
                        ))
                ))
        );

        // 바디, 헤더의 HttpEntity 제작
        HttpEntity<ProofreadRequestDto> httpEntitiy = new HttpEntity<>(requestDto, headers);

        try {
            // 요청 보내기 - exchange
            ResponseEntity<ProofreadResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntitiy,
                    ProofreadResponseDto.class
            );

            log.info("교정 전 : {}", content);
            log.info("교정 후 : {}", response.getBody().candidates.getFirst().content.parts.getFirst().text);

            return ResponseEntity.ok()
                    .body(response.getBody().candidates.getFirst().content.parts.getFirst().text);
        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body("외부 API 요청에 실패했습니다.");
        }
    }

    public record ProofreadRequestDto (
        List<Content> contents
    ) {
        public record Content(
            List<Part> parts
        ) {
            public record Part(String text) {}
        }
    }


    public record ProofreadResponseDto (
        List<Candidate> candidates
    ) {
        public record Candidate (
            Content content
        ) {
            public record Content (
                    List<Part> parts
            ) {
                public record Part (
                        String text
                ) {}
            }
        }
    }


}
