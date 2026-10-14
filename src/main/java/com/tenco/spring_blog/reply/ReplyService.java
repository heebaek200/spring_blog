package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyJpaRepository replyJpaRepository;
    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public List<ReplyResponse.ListDto> findAllByBoardId(Long boardId, Long sessionuserId) {
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(boardId);
        return replyList.stream()
                .map(reply -> new ReplyResponse.ListDto(reply, sessionuserId))
                .toList();
    }

    // 댓글 작성
    @Transactional
    public void save(ReplyRequest.SaveDto saveDto, SessionUserDto sessionUserDto) {
        // 1. 게시글 먼저 조회 (실제로 있는지 존재 확인을 겸함)
        Board board = boardJpaRepository.findById(saveDto.getBoardId())
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 작성자 정보 조회
        User user = userJpaRepository.findById(sessionUserDto.getId())
                .orElseThrow(() -> new Exception404("작성자 정보를 확인할 수 없습니다."));

        // R
        replyJpaRepository.save(saveDto.toEntity(board, user));
    }

    // 댓글 삭제
    @Transactional
    public Long deleteById(Long replyId, SessionUserDto sessionUserDto) {
        // 1. 댓글 조회
        Reply reply = replyJpaRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));

        // 2. 권한 확인: 작성자 본인만 삭제 가능
        if (reply.isOwner(sessionUserDto.getId()) == false) {
            throw new Exception403("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        Long boardId = reply.getBoard().getId();

        // 삭제
        replyJpaRepository.delete(reply);

        return boardId;   // 리다이렉트용으로 사용하기 위해 반환하는 응용도 있음
    }

}
