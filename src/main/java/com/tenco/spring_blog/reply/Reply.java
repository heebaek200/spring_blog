package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * 댓글 엔티티
 *  단방향 연관관계
 *  Reply -> Board (N:1)
 *  Reply -> User (N:1)
 */
@Getter
@NoArgsConstructor
@Table(name = "reply_tb")
@Entity
public class Reply {

    @Builder
    public Reply(String comment, User user, Board board) {
        this.comment = comment;
        this.user = user;
        this.board = board;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String comment;

    @CreationTimestamp
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 편의 메서드 - 댓글 작성자 본인인지 확인
    public boolean isOwner(Long userId) {
        if (userId == null) return false;   // 로그인하지 않음
        return user.getId().equals(userId);
    }

}
