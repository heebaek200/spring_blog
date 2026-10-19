package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// [복합 유니크 제약]
// 한 사용자는 같은 역할을 한 번만 가진다.
// columnNames에는 자바 컬럼명이 아니라 DB컬럼명을 사용해야 함.
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "user_role_tb",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_role",
                        columnNames = {
                                "user_id",
                                "role"
                        }
                )
        }
)
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 숫자가 아닌 문자열로 사용함
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private Role role;

    @Builder // id는 자동이므로 빌더에서 제외
    public UserRole(Role role) {
        this.role = role;
    }

}
