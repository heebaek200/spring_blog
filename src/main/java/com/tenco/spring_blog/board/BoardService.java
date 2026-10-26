package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.BoardPurchaseExistsException;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.purchase.PurchaseJpaRepository;
import com.tenco.spring_blog.reply.ReplyJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PurchaseJpaRepository purchaseJpaRepository;


    /**
     * 게시글 저장
     * @param saveDto 게시글 저장 정보
     * @param sessionUser 작성자 정보
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
    public BoardResponse.DetailDto findById(Long id, Long sessionUserId) {
        log.info("게시글 상세 조회 시작 - id: {}", id);

        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 로그인하지 않았으면 작성자도 구매자도 아님
        boolean owner = sessionUserId != null && board.isOwner(sessionUserId);

        // 구매여부
        boolean purchased = sessionUserId != null && purchaseJpaRepository.existsByUserIdAndBoardId(sessionUserId, id);

        return new BoardResponse.DetailDto(board, owner, purchased);
    }

    /**
     * 게시글 목록 조회 (메인 페이지 용) : 페이징 처리 추가 : 검색 기능 추가
     * @return 게시글 목록 (작성자 정보 포함)
     */
    public BoardResponse.PageDto findAll(int page, int size, String keyword) {
        log.info("게시글 목록 조회 시작");

        // 1. 1부터 시작하는 번호를 0부터로 변환
        int pageIndex = Math.max(0, page - 1);

        // 2. 페이지 크기는 1 ~ 50으로 제한 (size=10000 같은 요청 방어)
        int validSize = Math.clamp(size, 1, 50);

        // 페이징 주문서 생성. 최신 글이 먼저 오도록 id 내림차순 정렬.
        Pageable pageable = PageRequest.of(pageIndex, validSize, Sort.by(Sort.Direction.DESC, "id"));

        // 페이징 처리가 된 게시글 목록 조회 요청 (주문 결과 Page)
        Page<Board> boardPage;

        // 검색어 유무에 따라 분기
        if (keyword == null || keyword.isBlank()) {
            boardPage = boardJpaRepository.findAllJoinUser(pageable);
        } else {
            boardPage = boardJpaRepository.findAllKeywordJoinUser(pageable, keyword);
        }

        // 조회 후 트랜잭션 안에서 응답 DTO로 변환처리
        return new BoardResponse.PageDto(boardPage);
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

        // 구매했으면 삭제불가
        if (purchaseJpaRepository.existsByBoardId(id)) {
            throw new BoardPurchaseExistsException("구입 이력이 존재하는 게시글은 삭제할 수 없습니다.");
        }

        // 게시글의 댓글 일괄 삭제
        replyJpaRepository.deleteByBoardId(id);

        // 게시글 삭제
        boardJpaRepository.deleteById(id);

        log.info("게시글 삭제 완료");
    }


}
