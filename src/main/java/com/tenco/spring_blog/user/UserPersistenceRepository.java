package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserPersistenceRepository {

    private final EntityManager em;

    // 회원 정보 조회 - 로그인 (사용자 이름, 비밀번호 확인)
    public User findByUsernameAndPassword(String username, String password) {
        try {
            String jpql = "SELECT u FROM User AS u WHERE u.username = :username AND u.password = :password";
            Query query = em.createQuery(jpql, User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);

            return (User) query.getSingleResult();
        } catch (Exception e) {
            // 일치하는 사용자가 없거나 에러 발생 시 null 반환
            // 로그인 실패를 의미함
            return null;
        }
    }

    // 회원가입
    @Transactional
    public User save(User user) {
        // 비영속 상태의 User 객체를 영속성 컨텍스트에 저장.
        em.persist(user);   // 영속성 컨텍스트가 user 객체를 관리하기 시작함

        // persist() 후 객체는 영속 상태가 되고 트랙잭션 커밋 시점에 INSERT 쿼리 실행됨
        // 자동생성된 id와 createdAt이 user객체에 설정됨
        return user;
    }

    // 사용자명 중복체크용 조회 메서드
    public User findByUsername(String username) {
        // em.find()는 PK 기반으로 조회하고, 우리가 필요한건 username 기반 조회이므로 JPQL로 구현
        String jpql = """
                SELECT u
                FROM User AS u
                WHERE u.username = :username
                """;

        try {
            return em.createQuery(jpql, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            // 사용자를 찾을 수 없는 경우 null 반환
            return null;
        }

    }
}
