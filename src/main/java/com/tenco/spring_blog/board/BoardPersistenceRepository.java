package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 영속성 컨텍스트 활용한 레파지토리 클래스 만들기
 */
@RequiredArgsConstructor
@Repository
public class BoardPersistenceRepository {

    private final EntityManager em;


    @Transactional
    public void updateById(Long id, BoardRequest.UpdateDto reqDto) {
        // 1. 수정할 엔티티를 먼저 조회 후 영속 상태로 만듬
        Board boardEntity = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인
        if (boardEntity == null) {
            throw new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다");
        }

        // 엔티티 객체 상태 변경 중
//        boardEntity.setTitle(reqDto.getTitle());
//        boardEntity.setContent(reqDto.getContent());
        boardEntity.update(reqDto);

        // 1차 캐시에 저장된 엔티티 객체의 내부 상태값이 변경되고 트랜잭션이 종료되면
        // Dirty Checking이 일어남

    }


    // 게시글 삭제하기 (영속성 컨텍스트를 활용한 안전한 삭제)
    @Transactional
    public void deleteById(Long id) {
        // 1. 먼저 삭제할 엔티티를 영속 상태로 조회부터 함
        Board boardEntity = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인 (안전한 삭제)
        if (boardEntity == null) {
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다.");
        }

        // 3. 영속 상태의 엔티티 삭제 상태 변경
        em.remove(boardEntity);

        // 트랙잭션 커밋 시점에 DELETE SQL 실행

        // JPQL로 삭제해보기
        // DELETE FROM Board AS b WHERE b.id = :id
//        Query query = em.createQuery("DELETE FROM Board AS b WHERE b.id = :id");
//        query.setParameter("id", id);
//        query.executeUpdate();
    }




    // 기본키로 게시글 단건 조회 (1차 캐시 활용)
    public Board findById(Long id) {
        Board board = em.find(Board.class, id);
        // find() 메서드 특징:
        // 1. 기본키로만 조회 가능
        // 2. 1차 캐시에 먼저 찾기 시도
        // 3. 없으면 DB에서 조회 후 캐시에 저장
        // 4. 영속 상태로 만든 후 반환
        return board;
    }

    public Board findByWithJPQL(Long id) {
        String jpql = """
                SELECT
                    b
                FROM Board AS b
                WHERE
                    b.id = :id
                """;

        return em.createQuery(jpql, Board.class)
                .setParameter("id", id)
                .getSingleResult();
    }


    // JPQL을 사용한 게시글 목록 조회
    public List<Board> findAll() {
        // JPQL: 엔티티 객체를 대상으로 하는 객체지향 쿼리
        // Board는 엔티티 클래스 명, b 별칭
        // 테이블명(board_tb)가 아닌 인티티명(board)을 사용
        String jpql = """
                SELECT b FROM Board AS b ORDER BY b.createdAt DESC
                """;

        try {
            // createQuery : JPQL 쿼리 생성
            // 두번째 매개변수로 반환타입을 지정(타입 안정성)
            // getResultList() : List<Board>로 반환
            return em.createQuery(jpql, Board.class).getResultList();
        } catch (Exception e) {
            return null;
        }
    }



    // 게시글 저장
    @Transactional
    public Board save(Board board) {
        // 1. 매개변수로 받은 board는 이 시점에서 비영속상태라고 할 수 있다.
        //  - 데이터베이스와 연관없는 순수 Java 객체의 상태

        em.persist(board);
        // 2. 영속성 컨텍스트에 저장, board 객체가 영속 상태로 변경됨
        //  - 영속성 컨텍스트가 엔티티를 관리하기 시작함
        //  - 아직 INSERT 쿼리는 실행되지 않음(쓰기 지연)

        // 3. 트랙잭션 커밋 시점에 실제 INSERT 쿼리가 실행됨
        //  - 이 때 영속성 컨텍스트의 변경사항이 DB에 반영됨
        //  - board 객체의 id필드에 자동 생성된 값이 할당됨.
        return board;

        // 4. 영속 상태의  객체를 반환
        //  - 자동으로 생성된 id값 등을 포함된 객체가 반환됨.
    }

    // 엔티티의 영속 상태 4가지 확인
    private void entityLifecycleEx() {
        // 1. 비영속 상태
        Board board = new Board("제목", "내용", "작성자");

        // 2. 영속 상태
        em.persist(board);

        // 3. 준영속 상태
        em.detach(board);

        // 4. 삭제 예정 상태
        em.remove(board);
    }
}
