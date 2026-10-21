package com.tenco.spring_blog.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
public class UserPersistenceRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    // 단위 테스트할 메서드를 설계
    @Test
    public void save_회원가입_테스트() {
        // given: 회원 가입 시 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        Assertions.assertThat(user.getId()).isNull();
        System.out.println("저장 전 User : " + user);

        // when: 회원 가입 실행
        User savedUser = userJpaRepository.save(user);

        // then: 저장된 결과를 검증
        // 1. 자동 생성된 ID 값 확인
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedUser.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@email.com");

        // 3. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        // 영속성 컨텍스트는 같은 엔티티에 대해 같은 인스턴스를 보장한다.
        Assertions.assertThat(user).isSameAs(savedUser); // true, false

    }

    @Test
    public void findByUsername_존재하지_않는_사용자_테스트() {
        // given
//        String username = "admin";
        String username = "xxxxx";

        // when
        User notFoundUser = userJpaRepository.findByUsername(username).orElse(null);

        // then
        // null 여부 확인
        Assertions.assertThat(notFoundUser).isNull();
    }
}
