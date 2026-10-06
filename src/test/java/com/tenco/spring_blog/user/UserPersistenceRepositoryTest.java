package com.tenco.spring_blog.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(UserPersistenceRepository.class)
@DataJpaTest
public class UserPersistenceRepositoryTest {

    @Autowired
    private UserPersistenceRepository userPersistenceRepository;

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
        User savedUser = userPersistenceRepository.save(user);

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
        User notFoundUser = userPersistenceRepository.findByUsername(username);

        // then
        // null 여부 확인
        Assertions.assertThat(notFoundUser).isNull();
    }

    /**
     * 올바른 사용자명과 비밀번호로 로그인용 조회를 수행한다.
     * 테스트용 사용자를 먼저 저장한 뒤 동일한 인증 정보를 사용하여 조회한다.
     * 조회된 사용자가 null이 아니며 저장했던 사용자 정보와 일치하는지 검증한다.
     */
    @Test
    public void findByUsernameAndPassword_로그인_성공_테스트() {
        // given: 로그인할 사용자 정보를 DB에 저장
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userPersistenceRepository.save(user);

        String username = "testUser";
        String password = "3456";

        // when: 올바른 사용자명과 비밀번호로 로그인 조회
        User foundUser =
                userPersistenceRepository.findByUsernameAndPassword(username, password);

        // then: 로그인에 성공하여 사용자가 조회되어야 함
        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getId()).isNotNull();
        Assertions.assertThat(foundUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(foundUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(foundUser.getEmail()).isEqualTo("test@email.com");
    }

    /**
     * 존재하는 사용자명을 사용하지만 잘못된 비밀번호로 로그인 조회를 수행한다.
     * 사용자명은 일치하지만 비밀번호가 일치하지 않으므로 조회 결과가 없어야 한다.
     * 로그인 실패를 의미하는 null이 반환되는지 검증한다.
     */
    @Test
    public void findByUsernameAndPassword_비밀번호_불일치_테스트() {
        // given: 로그인할 사용자 정보를 DB에 저장
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userPersistenceRepository.save(user);

        String username = "testUser";
        String password = "9999";

        // when: 잘못된 비밀번호로 로그인 조회
        User foundUser =
                userPersistenceRepository.findByUsernameAndPassword(username, password);

        // then: 일치하는 사용자가 없으므로 null 반환
        Assertions.assertThat(foundUser).isNull();
    }

    /**
     * 존재하지 않는 사용자명으로 로그인용 조회를 수행한다.
     * 데이터베이스에 해당 username을 가진 사용자가 존재하지 않는 상황을 확인한다.
     * 로그인 실패를 의미하는 null이 반환되는지 검증한다.
     */
    @Test
    public void findByUsernameAndPassword_존재하지_않는_사용자_테스트() {
        // given: 존재하지 않는 로그인 정보
        String username = "xxxxx";
        String password = "3456";

        // when: 존재하지 않는 사용자명으로 로그인 조회
        User foundUser =
                userPersistenceRepository.findByUsernameAndPassword(username, password);

        // then: 일치하는 사용자가 없으므로 null 반환
        Assertions.assertThat(foundUser).isNull();
    }
}
