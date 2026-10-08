package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Board 관련 비즈니스 로직을 처리하는 Service 계층
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardJpaRepository boardJpaRepository;


    /**
     * 게시글 저장
     * @param saveDto 게시글 저장 정보
     * @param sessionUser 작성자 정보
     * @return 저장된 게시글
     */
    @Transactional
    public Board save(BoardRequest.SaveDto saveDto, User sessionUser) {
        log.info("게시글 저장 시작 - 제목: {}, 작성자: {}", saveDto.getTitle(), sessionUser.getUsername());

        Board board = saveDto.toEntity(sessionUser);
        Board savedBoard = boardJpaRepository.save(board);

        log.info("게시글 저장 완료 - ID: {}, 제목: {}", savedBoard.getTitle(), savedBoard.getTitle());

        return savedBoard;
    }

    /**
     * 게시글 상세 조회
     * @param id 게시글 PK
     * @return 게시글 정보 (작성자 정보 포함)
     */
    public Board findByid(Long id) {
        log.info("게시글 상세 조회 시작 - id: {}", id);

        // Board board = boardJpaRepository.findbyId(id) <- N+1 문제
        Board board = boardJpaRepository.findbyIdJoinUser(id)
                .orElseThrow(() -> {
                    log.warn("게시글 조회 실패 - ID : {}", id);
                    return new Exception404("게시글을 찾을 수 없습니다.");
                });
        log.info("게시글 상세 조회 완료 - 제목: {}, 작성자 : {}", board.getTitle(), board.getUser().getUsername());

        return board;
    }

    /**
     * 게시글 목록 조회 (메인 페이지 용)
     * @return 게시글 목록 (작성자 정보 포함)
     */
    public List<Board> findAll() {
        log.info("게시글 목록 조회 시작");

        // List<Board> boardList = boardJpaRepository.findAll(); <- N+1 문제
        List<Board> boardList = boardJpaRepository.findAllJoinUser();

        log.info("게시글 목록 조회 완료");

        return boardList;
    }

    /**
     * 게시글 수정 (권한 체크)
     * @param id
     * @param updateDto
     * @param sessionUser
     * @return
     */
    @Transactional
    public Board updateById(Long id,  BoardRequest.UpdateDto updateDto, User sessionUser) {
        log.info("게시글 수정 시작");

        // 1. 게시글 조회
        Board board = findByid(id);

        // 2. 권한 체크
        if (! board.isOwner(sessionUser.getId())) {
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 3. 더티 체킹을 활용한 수정
        board.update(updateDto);

        log.info("게시글 수정 완료");

        return board;
    }

    @Transactional
    public void deleteById(Long id, User sessionUser) {
        log.info("게시글 삭제 시작");

        // 1. 게시글 조회
        Board board = findByid(id);
        if (! board.isOwner(sessionUser.getId())) {
            log.warn("게시글 삭제 권한 없음 - 게시글 ID: {}, 작성자: {}, 요청자: {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        boardJpaRepository.deleteById(id);

        log.info("게시글 삭제 완료");
    }


}
