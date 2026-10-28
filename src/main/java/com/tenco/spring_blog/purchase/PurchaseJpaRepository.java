package com.tenco.spring_blog.purchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 구매 존재 건수
    @Query("""
    SELECT
        COUNT(p)
    FROM Purchase AS p
    WHERE
        p.board.id = :boardId
""")
    long countByBoardId(
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

    // 구매 내역
    // u : 구매자, bu : 작성자
    @Query(value = """
        SELECT
            p
        FROM Purchase AS p
        JOIN FETCH
            p.user AS u
        JOIN FETCH
            p.board AS b
        JOIN FETCH
            b.user AS bu
        WHERE
            p.user.id = :userId
""", countQuery = """
        SELECT
            count(p)
        FROM Purchase AS p
        JOIN
            p.user AS u
        WHERE
            p.user.id = :userId
""")
    Page<Purchase> findAllByUserIdJoinBoardAndAuthor(@Param(value = "userId") Long userId, Pageable pageable);
}
