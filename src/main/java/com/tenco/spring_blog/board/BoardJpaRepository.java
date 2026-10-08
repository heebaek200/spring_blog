package com.tenco.spring_blog.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardJpaRepository extends JpaRepository<Board, Long> {
    // 기본적인 CRUD 기능 만들어져 있음.
    /**
     * 1. 등록 및 수정 save(Board entity)
     *      - 엔티티를 DB에 저장. ID가 없으면 INSERT, 있으면 UPDATE 실행.
     * 2. 단건 조회: findById(Long id)
     *      - ID로 엔티티를 조회하면 Optional<Board> 타입을 반환함.
     * 3. 전체 조회: findAll()
     *      - 테이블의 모든 데이터를 조회하여 List<Board>로 반환.
     * 4. 삭제: deleteById(Long id)
     *      - 특정 ID를 가진 엔티티를 삭제
     * 5. 데이터 개수: count()
     *      - 전체 레코드의 개수를 반환함
     * 6. 존재 여부 확인: existsById(Long id)
     *      - 해당 ID를 가진 데이터가 있는지 확인하여 boolean을 반환함
     */


    // 게시글 ID로 조회하면서 작성자 정보도 한번에 가져오기
    // JOIN FETCH를 사용하여 N+1 문제 해결
    // N+1 문제: 지연 로딩에서 Board 조회 후 User를 따로 조회하면 쿼리가 여러번 실행됨.
    // JOIN FETCH는 이를 한 번의 쿼리로 해결함.
    @Query("SELECT b FROM Board AS b JOIN FETCH b.user WHERE b.id = :id")
    Optional<Board> findbyIdJoinUser(@Param("id") Long id);

    @Query("SELECT b FROM Board AS b JOIN FETCH b.user ORDER BY b.id DESC")
    List<Board> findAllJoinUser();


}
