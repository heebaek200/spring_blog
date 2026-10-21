package com.tenco.spring_blog.user;

import com.tenco.spring_blog.oauth.OAuthProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * 사용자명과 역할 정보를 함께 조회할 수 있는지 확인한다.
     * 테스트 데이터는 data.sql에 의존하지 않고 테스트 내부에서 직접 저장한다.
     * 조회 결과가 존재하고 사용자명이 일치하는지만 검증한다.
     */
    @Test
    void findByUsernameWithRoles_사용자명일치_테스트() {
        // given: USER 역할을 가진 테스트 사용자를 직접 저장한다.
        User user = createUser("ssar", "1234", "ssar@test.com");
        user.addRole(Role.USER);

        userJpaRepository.saveAndFlush(user);
        entityManager.clear();

        // when: 저장한 사용자명으로 다시 조회한다.
        Optional<User> userOptional =
                userJpaRepository.findByUsernameWithRoles("ssar");

        // then: 해당 사용자를 찾았는지 확인한다.
        assertThat(userOptional)
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("ssar");
    }

    /**
     * findByUsernameWithRoles의 JOIN FETCH 동작을 확인한다.
     * 영속성 컨텍스트를 clear한 뒤 새로 조회하여 기존 캐시의 영향을 제거한다.
     * PersistenceUnitUtil을 이용해 roles 연관관계가 이미 로딩되었는지 검사한다.
     */
    @Test
    void findByUsernameWithRoles_역할FetchJoin_테스트() {
        // given: 역할을 가진 사용자를 저장하고 영속성 컨텍스트를 초기화한다.
        User user = createUser("cos", "1234", "cos@test.com");
        user.addRole(Role.USER);
        user.addRole(Role.ADMIN);

        userJpaRepository.saveAndFlush(user);
        entityManager.clear();

        // when: JOIN FETCH가 적용된 Repository 메서드로 조회한다.
        User foundUser = userJpaRepository.findByUsernameWithRoles("cos")
                .orElseThrow();

        // then: roles 컬렉션이 이미 로딩된 상태인지 확인한다.
        PersistenceUnitUtil persistenceUnitUtil =
                entityManagerFactory.getPersistenceUnitUtil();

        assertThat(
                persistenceUnitUtil.isLoaded(foundUser, "roles")
        ).isTrue();
    }

    /**
     * 존재하지 않는 사용자명으로 조회했을 때의 결과를 확인한다.
     * Repository 조회 결과가 null이 아니라 Optional.empty()인지 검사한다.
     * 정상 조회와 실패 조회를 별개의 테스트로 분리한다.
     */
    @Test
    void findByUsernameWithRoles_존재하지않는사용자_테스트() {
        // given: 조회할 사용자 데이터를 저장하지 않는다.

        // when: 존재하지 않는 사용자명으로 조회한다.
        Optional<User> userOptional =
                userJpaRepository.findByUsernameWithRoles("unknown");

        // then: 조회 결과가 비어 있어야 한다.
        assertThat(userOptional).isEmpty();
    }

    /**
     * 사용자명 중복 확인에 사용하는 조회 메서드를 검증한다.
     * 테스트 안에서 사용자를 저장한 뒤 같은 사용자명으로 검색한다.
     * 검색된 Entity의 이메일 값으로 올바른 사용자임을 확인한다.
     */
    @Test
    void findByUsername_사용자명일치_테스트() {
        // given: 사용자명 ssar인 사용자를 저장한다.
        User user = createUser(
                "ssar",
                "1234",
                "ssar@test.com"
        );

        userJpaRepository.saveAndFlush(user);
        entityManager.clear();

        // when: 사용자명으로 조회한다.
        User foundUser = userJpaRepository
                .findByUsername("ssar")
                .orElseThrow();

        // then: 저장한 사용자의 이메일과 일치하는지 확인한다.
        assertThat(foundUser.getEmail())
                .isEqualTo("ssar@test.com");
    }

    /**
     * 이메일 중복 조회에서 일치하는 데이터가 없을 때를 검증한다.
     * 별도의 초기 데이터를 사용하지 않아 테스트 간 독립성을 유지한다.
     * 조회 결과는 Optional.empty()이어야 한다.
     */
    @Test
    void findByEmail_존재하지않는이메일_테스트() {
        // given: 테스트 DB에 해당 이메일 사용자를 저장하지 않는다.

        // when: 존재하지 않는 이메일로 조회한다.
        Optional<User> userOptional =
                userJpaRepository.findByEmail(
                        "unknown@test.com"
                );

        // then: 조회 결과가 없어야 한다.
        assertThat(userOptional).isEmpty();
    }

    /**
     * Repository 테스트에서 반복해서 사용할 User 객체를 생성한다.
     * 각 테스트가 자신만의 데이터를 저장하도록 Entity만 생성하며,
     * 실제 DB 저장은 각 테스트 메서드에서 직접 수행한다.
     */
    private User createUser(
            String username,
            String password,
            String email
    ) {
        return User.builder() // 확인 필요: 현재 프로젝트 User 빌더 구조 기준
                .username(username)
                .password(password)
                .email(email)
                .provider(OAuthProvider.LOCAL)
                .build();
    }
}