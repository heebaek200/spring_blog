package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
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

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PurchaseService {

    private final BoardJpaRepository boardJpaRepository;
    private final PurchaseJpaRepository purchaseJpaRepository;
    private final UserJpaRepository userJpaRepository;

    // 유료 게시글 구매
    // 포인트 차감 + 구매 내역 저장을 하나의 트랜잭션으로 처리 (하나라도 실패하면 모두 롤백)
    @Transactional
    public void purchase(Long boardId, Long userId) {
        // 1. 게시글 조회
        Board board = boardJpaRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 유료 게시글인지 확인
        if (! board.isPremium()) {
            throw new Exception400("유료 게시글이 아닙니다.");
        }

        // 3. 작성자는 자기글을 구매할 수 없음(이미 열람 가능)
        if (board.isOwner(userId)) {
            throw new Exception400("자신이 작성한 게시글을 구매할 수 없습니다.");
        }

        // 4. 이미 구매했는지 확인
        if (purchaseJpaRepository.existsByUserIdAndBoardId(
                userId, boardId
        )) {
            throw new Exception400("이미 구매한 게시글입니다.");
        }

        // 5. 구매자 조회 및 포인트 차감. 포인트 부족하면 롤백
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        user.deductPoint(Board.PREMIUM_PRICE);

        // 6. 구매 내역 저장
        Purchase purchase = Purchase.builder()
                .user(user)
                .board(board)
                .price(Board.PREMIUM_PRICE)
                .build();
        purchaseJpaRepository.save(purchase);
    }

    /**
     * 구매 내역 조회
     * @param sessionUserId 세션의 User.id
     * @param page 페이지
     * @param size 페이지 당 행 수
     * @return PageDto
     */
    public PurchaseResponse.PageDto findAllByUserId(Long sessionUserId, int page, int size) {
        log.info("구매 내역 조회 시작");

        // 1부터 시작하는 번호를 0부터로 변환
        int pageIndex = Math.max(0, page - 1);

        // 페이지 크기는 1 ~ 50으로 제한 (size=10000 같은 요청 방어)
        int validSize = Math.clamp(size, 1, 50);

        // 페이징 주문서 생성. 최신 구매가 먼저 오도록 id 내림차순 정렬.
        Pageable pageable = PageRequest.of(pageIndex, validSize, Sort.by(Sort.Direction.DESC, "id"));

        // 페이징 처리가 된  구매 내역 조회 요청 (주문 결과 Page)
        Page<Purchase> purchasePage = purchaseJpaRepository.findAllByUserIdJoinBoardAndAuthor(sessionUserId, pageable);

        // 조회 후 트랜잭션 안에서 응답 DTO로 변환처리
        return new PurchaseResponse.PageDto(purchasePage);
    }
}
