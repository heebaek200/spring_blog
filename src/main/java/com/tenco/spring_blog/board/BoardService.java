package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.reply.ReplyJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Board 관련 비즈니스 로직을 처리하는 Service 계층
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReplyJpaRepository replyJpaRepository;


    /**
     * 게시글 저장
     * @param saveDto 게시글 저장 정보
     * @param sessionUser 작성자 정보
     * @return 저장된 게시글
     */
    @Transactional
    public void save(BoardRequest.SaveDto saveDto, SessionUserDto sessionUser) {
        log.info("게시글 저장 시작 - 제목: {}, 작성자: {}", saveDto.getTitle(), sessionUser.getUsername());

        User user = userJpaRepository.getReferenceById(sessionUser.getId());
        Board savedBoard = boardJpaRepository.save(saveDto.toEntity(user));

        log.info("게시글 저장 완료 - ID: {}, 제목: {}", savedBoard.getTitle(), savedBoard.getTitle());
    }

    /**
     * 게시글 상세 조회
     * @param id 게시글 PK
     * @return 게시글 정보 (작성자 정보 포함)
     */
    public BoardResponse.DetailDto findById(Long id) {
        log.info("게시글 상세 조회 시작 - id: {}", id);

        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        return new BoardResponse.DetailDto(board);
    }

    /**
     * 게시글 목록 조회 (메인 페이지 용)
     * @return 게시글 목록 (작성자 정보 포함)
     */
    public List<BoardResponse.ListDto> findAll() {
        log.info("게시글 목록 조회 시작");
        List<Board> boardList = boardJpaRepository.findAllJoinUser();

        // List<Board>에서 List<BoardResponse.ListDto>로 옮겨담기
        // 반복문 사용 => 스트림API에 람다식 => 메서드 참조 문법
        return boardList.stream()
                .map(BoardResponse.ListDto::new)
                .toList();
    }

    /**
     * 게시글 수정 화면 요청 (인가 검사)
     *
     * @param id
     * @param userId
     * @return
     */
    public BoardResponse.UpdateFormDto findByIdAndUpdateForm(Long id, Long userId) {
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 인가 검사
        if (! board.isOwner(userId)) {
            throw  new Exception403("게시글 수정 권한이 없습니다.");
        }

        return new BoardResponse.UpdateFormDto(board);
    }

    /**
     * 게시글 수정 (권한 체크)
     * @param id
     * @param updateDto
     * @param sessionUser
     * @return
     */
    @Transactional
    public void updateById(Long id,  BoardRequest.UpdateDto updateDto, SessionUserDto sessionUser) {
        log.info("게시글 수정 시작");

        // 1. 게시글 조회
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 권한 체크
        if (! board.isOwner(sessionUser.getId())) {
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 3. 더티 체킹을 활용한 수정
        board.update(updateDto);

        log.info("게시글 수정 완료");
    }

    @Transactional
    public void deleteById(Long id, SessionUserDto sessionUser) {
        log.info("게시글 삭제 시작");

        // 1. 게시글 조회
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        if (! board.isOwner(sessionUser.getId())) {
            log.warn("게시글 삭제 권한 없음 - 게시글 ID: {}, 작성자: {}, 요청자: {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 게시글의 댓글 일괄 삭제
        replyJpaRepository.deleteByBoardId(id);

        // 게시글 삭제
        boardJpaRepository.deleteById(id);

        log.info("게시글 삭제 완료");
    }


}
