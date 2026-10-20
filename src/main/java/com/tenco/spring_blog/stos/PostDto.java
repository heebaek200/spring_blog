package com.tenco.spring_blog.stos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

// 서버 측에 던질 데이터
@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDto {
    private String title;
    private String body;
    private Integer userId;

    @Setter
    private Integer id;

    @Builder
    public PostDto(String title, String body, Integer userId) {
        this.title = title;
        this.body = body;
        this.userId = userId;
    }
}
