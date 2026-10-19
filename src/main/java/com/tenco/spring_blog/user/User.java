package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    // 사용자의 역할 목록 (User 1 : UserRole N, 단방향)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false)
    private List<UserRole> roles = new ArrayList<>();

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

    // 역할 추가 메서드 (회원 가입시 기본 USER 부여, 관리자는 직접 지정)
    public void addRole(Role role) {
        // 이미 가진 역할이면 추가하지 않음 (코드에서 1차 방어)
        if (hasRole(role)) {
            return;
        }

        this.roles.add(UserRole.builder()
                .role(role)
                .build()
        );
    }

    // 해당 역할을 가지고 있는지 확인
    public boolean hasRole(Role role) {
        for (UserRole u: this.roles) {
            if (u.getRole() == role) {
                return true;
            }
        }

        return false;
    }

    // 로그인한 사용자의 관리자 여부 확인 메서드
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

}
