package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor // final 필드에 대한 DI 처리 생성자를 자동 생성
@Repository // IoC
public class BoardNativeRepository {

    private final EntityManager em;

    @Transactional
    public void save(String title, String content, String username) {
        Query query = em.createNativeQuery(
                "INSERT INTO board_tb(title, content, username, created_at)"
                + "VALUES(?, ?, ?, NOW())"
        );
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, username);

        query.executeUpdate();
    }

    public List<Board> findAll() {
        String sql = """
                SELECT * FROM board_tb ORDER BY id DESC                
                """;

        Query query = em.createNativeQuery(sql, Board.class);
        return query.getResultList();
    }

    public Board findById(Long id) {
        String sql = """
                SELECT * FROM board_tb WHERE id = ?
                """;

        Query query = em.createNativeQuery(sql, Board.class);
        query.setParameter(1, id);

        try {
            return (Board) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM board_tb WHERE id = ?
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    @Transactional
    public boolean updateById(String title, String content, Long id) {

        String sql = """
                UPDATE board_tb SET
                    title   = ?
                  , content = ?
                WHERE
                    id = ?
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, id);

        int rows = query.executeUpdate();
        return rows > 0;
    }
}
