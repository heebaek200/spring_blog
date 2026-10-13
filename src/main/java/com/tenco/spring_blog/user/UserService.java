package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception404;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;

    /**
     * 회원가입 처리
     * @param joinDto 회원가입 정보
     * @return 저장된 사용자 정보
     */
    @Transactional
    public UserResponse.JoinDto join(UserRequest.JoinDto joinDto) {
        log.info("회원가입 서비스 처리 시작");

        // 중복체크
        userJpaRepository.findByUsername(joinDto.getUsername()).ifPresent(user -> {
            log.warn("회원가입 실패 - 중복된 사용자명");
            throw new Exception400("이미 존재하는 사용자명입니다."); // 찾아보니까 원랜 409래
        });

        // 이메일 중복체크
        userJpaRepository.findByEmail(joinDto.getEmail()).ifPresent(user -> {
            log.warn("회원가입 실패 - 중복된 이메일");
            throw new Exception400("이미 사용 중인 이메일입니다.");
        });

        User savedUser = userJpaRepository.save(joinDto.toEntity());

        log.info("회원가입 서비스 처리 완료 - 사용자명: {}, ID: {}",
                joinDto.getUsername(), savedUser.getId());

        return new UserResponse.JoinDto(savedUser);
    }

    /**
     * 로그인 처리
     * @param loginDto 로그인 정보
     * @return 로그인된 사용자 정보
     */
    public SessionUserDto login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 처리 시작");

        User user = userJpaRepository.findByUsernameAndPassword(loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명 또는 비밀번호 불일치");
                    return new Exception401("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });
        log.info("로그인 서비스 처리 시작");
        return new SessionUserDto(user);
    }

    /**
     * 회원 정보 수정 화면 : 화면에 필요한 값만 담는 DTO 반환
     *
     * @param userId
     * @return
     */
    public UserResponse.UpdateFormDto findUpdateForm(Long userId) {
        User user = findById(userId);
        return new UserResponse.UpdateFormDto(user);
    }

    /**
     * 회원 정보 수정 처리
     * : 세션 동기화용 DTO 반환
     *
     * @param id 사용자 PK
     * @param updateDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    @Transactional
    public SessionUserDto updateById(Long id, UserRequest.UpdateDto updateDto) {
        User user = findById(id);

        user.update(updateDto);
        return new SessionUserDto(user);
    }

    /**
     * 사용자 정보 조회
     * : 서비스 안에서만 쓰는 조회용 메서드 (캡슐화)
     * @param id 사용자 PK
     * @return 사용자 정보
     */
    private User findById(Long id) {
        log.info("사용자 정보 조회 처리 시작");

        return userJpaRepository.findById(id)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }
}
