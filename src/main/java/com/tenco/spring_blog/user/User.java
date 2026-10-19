package com.tenco.spring_blog.user;

import com.tenco.spring_blog.board.BoardRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique 제약
    @Column(unique = true)
    private String username;
    @Setter
    private String password;
    @Column(unique = true)
    private String email;

    @CreationTimestamp
    private Timestamp createdAt;

    // 프로필 이미지 파일 이름 (선택 사항이므로 null)가능
    private String profileImage;

    // 블랙리스트
    @ColumnDefault("false")
    private Boolean banned = false;

    @Builder
    public User(String username, String password, String email, String profileImage) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
    }

    public void update(UserRequest.UpdateDto updateDto) {
        this.password = updateDto.getPassword();
    }

    // 프로필 이미지 변경 (null 이면 삭제)
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}
