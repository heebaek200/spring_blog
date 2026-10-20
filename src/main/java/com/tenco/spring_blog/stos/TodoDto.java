package com.tenco.spring_blog.stos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoDto {
    private Integer userId;
    private Integer id;
    private String title;
    private boolean completed;

}
