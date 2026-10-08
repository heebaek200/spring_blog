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
    public User join(UserRequest.JoinDto joinDto) {
        log.info("회원가입 서비스 처리 시작");

//        userJpaRepository.findByUsername(joinDto.getUsername()).orElseThrow(() -> {
//            log.warn("회원가입 실패 - 중복된 사용자명 : {}", joinDto.getUsername());
//            return new Exception400("이미 존재하는 사용자명입니다.");
//        });
        // 중복체크
        userJpaRepository.findByUsername(joinDto.getUsername()).ifPresent(user -> {
            log.warn("회원가입 실패 - 중복된 사용자명");
            throw new Exception400("이미 존재하는 사용자명입니다."); // 찾아보니까 원랜 409래
        });

        User savedUser = userJpaRepository.save(joinDto.toEntity());

        log.info("회원가입 서비스 처리 완료 - 사용자명: {}, ID: {}",
                joinDto.getUsername(), savedUser.getId());

        return savedUser;
    }

    /**
     * 로그인 처리
     * @param loginDto 로그인 정보
     * @return 로그인된 사용자 정보
     */
    public User login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 처리 시작");

        User user = userJpaRepository.findByUsernameAndPassword(loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명 또는 비밀번호 불일치");
                    return new Exception401("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });
        log.info("로그인 서비스 처리 시작");
        return user;
    }

    /**
     * 사용자 정보 조회
     * @param id 사용자 PK
     * @return 사용자 정보
     */
    public User findById(Long id) {
        log.info("사용자 정보 조회 처리 시작");

        return userJpaRepository.findById(id)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }

    /**
     * 회원 정보 수정 처리
     * @param id 사용자 PK
     * @param updateDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    @Transactional
    public User updateById(Long id, UserRequest.UpdateDto updateDto) {
        User user = findById(id);

        user.update(updateDto);
        return user;
    }

}
