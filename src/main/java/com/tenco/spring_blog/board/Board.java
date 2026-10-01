package com.tenco.spring_blog.board;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Table(name = "board_tb")
@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String username;
    private Timestamp createdAt;
}
