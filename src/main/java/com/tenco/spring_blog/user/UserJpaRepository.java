package com.tenco.spring_blog.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 불필요 (부모 인터페이스 어딘가에 있나봄)
public interface UserJpaRepository extends JpaRepository<User, Long> {
    // 기본적인 CRUD 기능 만들어져 있음.
    /**
     * 1. 등록 및 수정 save(User entity)
     *      - 엔티티를 DB에 저장. ID가 없으면 INSERT, 있으면 UPDATE 실행.
     * 2. 단건 조회: findById(Long id)
     *      - ID로 엔티티를 조회하면 Optional<User> 타입을 반환함.
     * 3. 전체 조회: findAll()
     *      - 테이블의 모든 데이터를 조회하여 List<User>로 반환.
     * 4. 삭제: deleteById(Long id)
     *      - 특정 ID를 가진 엔티티를 삭제
     * 5. 데이터 개수: count()
     *      - 전체 레코드의 개수를 반환함
     * 6. 존재 여부 확인: existsById(Long id)
     *      - 해당 ID를 가진 데이터가 있는지 확인하여 boolean을 반환함
     */

    // 사용자명과 비밀번호로 조회 (로그인용)
    @Query("SELECT u FROM User AS u WHERE u.username = :username AND u.password = :password")
    Optional<User> findByUsernameAndPassword(
            @Param("username") String username,
            @Param("password") String password
    );

    // 사용자명으로 사용자 조회 (중복 체크용)
    @Query("SELECT u FROM User AS u WHERE u.username = :username")
    Optional<User> findByUsername(
            @Param("username") String username
    );

    // 사용자 정보 업데이트는 JPA의 더티 체킹을 활용하므로 만들지 않음

}
