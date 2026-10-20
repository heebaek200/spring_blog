package com.tenco.spring_blog.stos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RestController // @Controller + @ResponseBody
public class RestTemplateController {

    // 테스트 주소 - GET http://localhost:8080/practice/todos/1
    @GetMapping("/practice/todos/{id}")
    public ResponseEntity<TodoDto> getTodoAsString(
            @PathVariable(name = "id") Integer id) {

        // 1. 요청할 외부 API 주소
        String url = "https://jsonplaceholder.typicode.com/todos/" + id;

        // 2. HTTP 요청을 보내는 도구 생성
        RestTemplate restTemplate = new RestTemplate();

        // 3. (외부 API가 요구하는 양식에 맞춰서) 요청 헤더 작성 (응답은 JSON으로 주세요)
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 4. HttpEntitiy (헤더 + 바디)
        HttpEntity request = new HttpEntity(headers);   // GET 요청이므로 body없음

        // 5. 요청 보내기 - exchange(주소, 메서드, Http엔티티(요청 포함), 응답 바디 타입)
        ResponseEntity<TodoDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                TodoDto.class
        );

        // 6. 응답 확인
        log.debug("상태 코드 확인 : " + response.getStatusCode());
        log.debug("응답 헤더 확인 : " + response.getHeaders());
        log.debug("바디 내용 확인 : " + response.getBody());

        // 7. 응답 DTO 형태 구축
        log.debug("userId : {}", response.getBody().getUserId());
        log.debug("id : {}", response.getBody().getUserId());
        log.debug("title : {}", response.getBody().getTitle());
        log.debug("completed : {}", response.getBody().isCompleted());


        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

    // 테스트 주소 : POST http://localhost:8080/practice/posts
    @GetMapping("/practice/posts")
    public ResponseEntity<?> createPost() {
        // 1. 요청할 외부 API 주소
        String url = "https://jsonplaceholder.typicode.com/posts";

        // 2. HTTP 요청을 보내는 도구 생성
        RestTemplate restTemplate = new RestTemplate();

        // 3. (외부 API가 요구하는 양식에 맞춰서) 요청 헤더 작성
        HttpHeaders headers = new HttpHeaders();
        // content-type: application/json <- 요청 바디가 JSON이라는 의미
        headers.setContentType(MediaType.APPLICATION_JSON);
        // accept <- 응답 바디를 JSON으로 달라는 의미
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 4. 요청 바디 작성
        PostDto postDto = PostDto.builder()
                .userId(100)
                .title("오늘은 S to S")
                .body("내용")
                .build();

        // 바디, 헤더의 HttpEntity 제작
        HttpEntity<PostDto> request = new HttpEntity(postDto, headers);

        try {
            // 5. 요청 보내기 - exchange(주소, 메서드, Http엔티티(요청 포함), 응답 바디 타입)
            ResponseEntity<PostDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PostDto.class
            );

            // 6. 응답 확인
            log.debug("상태 코드 확인 : " + response.getStatusCode());
            log.debug("응답 헤더 확인 : " + response.getHeaders());
            log.debug("바디 내용 확인 : " + response.getBody());

            return ResponseEntity
                    .ok()
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body("외부 API 요청에 실패했습니다.");
        }
    }

}
