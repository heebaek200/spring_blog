package com.tenco.spring_blog.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, Long> {
    // 구매 내역 조회 (해당 게시글 pk 기준)
    @Query("""
    SELECT p
    FROM Purchase AS p
    WHERE
        p.user.id = :userId
    AND p.board.id = :boardId
""")
    Optional<Purchase> findByUserIdAndBoardID(
            @Param("userId") Long userId,
            @Param("boardId") Long boardId
    );

    // 구매 여부 확인
    @Query("""
    SELECT
        CASE
            WHEN COUNT(p) > 0 THEN
                TRUE
            ELSE
                FALSE
        END
    FROM Purchase AS p
    WHERE
        p.user.id = :userId
    AND p.board.id = :boardId
""")
    boolean existsByUserIdAndBoardId(
        @Param("userId") Long userId,
        @Param("boardId") Long boardId
    );


    // 구매 1건 이상 확인
    @Query("""
    SELECT
        CASE
            WHEN COUNT(p) > 0 THEN
                TRUE
            ELSE
                FALSE
        END
    FROM Purchase AS p
    WHERE
        p.board.id = :boardId
""")
    boolean existsByBoardId(
            @Param("boardId") Long boardId
    );


    // 게시글에 달린 구매 내역 일괄 삭제
    @Modifying
    @Query("""
    DELETE FROM Purchase AS p
    WHERE
        p.board.id = :boardId
""")
    void deleteByBoardId(
        @Param("boardId") Long boardId
    );

}
