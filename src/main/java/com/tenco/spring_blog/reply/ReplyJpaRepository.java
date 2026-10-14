package com.tenco.spring_blog.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyJpaRepository extends JpaRepository<Reply, Long> {
    // 기본적인 CRUD 기능 만들어져 있음.
    /**
     * 1. 등록 및 수정 save(Board entity)
     *      - 엔티티를 DB에 저장. ID가 없으면 INSERT, 있으면 UPDATE 실행.
     * 2. 단건 조회: findById(Long id)
     *      - ID로 엔티티를 조회하면 Optional<Reply> 타입을 반환함.
     * 3. 전체 조회: findAll()
     *      - 테이블의 모든 데이터를 조회하여 List<Reply>로 반환.
     * 4. 삭제: deleteById(Long id)
     *      - 특정 ID를 가진 엔티티를 삭제
     * 5. 데이터 개수: count()
     *      - 전체 레코드의 개수를 반환함
     * 6. 존재 여부 확인: existsById(Long id)
     *      - 해당 ID를 가진 데이터가 있는지 확인하여 boolean을 반환함
     */

    // 게시글의 댓글 목록 조회 (작성자와 함께 조회, 오래된 순)
    @Query("""
SELECT r
FROM Reply AS r
JOIN FETCH r.user
WHERE
    r.board.id = :boardId
ORDER BY
    r.id ASC
""")
    List<Reply> findAllByBoardIdJoinUser(@Param("boardId") Long boardId);

    // 게시글의 댓글 일괄 삭제
    // 주의: @Query는 기본적으로 SELECT로 실행되므로 @Modifying 필요
    @Modifying
    @Query("DELETE FROM Reply AS r WHERE r.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long BoardId);

}
