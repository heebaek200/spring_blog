package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

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

        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(joinDto.getPassword());

        // 프로필 이미지 저장 후 새로운 파일명 획득
        String profileImage = FileUtil.saveImage(joinDto.getProfileImage());

        // Role  기본값 할당
        User user = joinDto.toEntity(profileImage, encodedPassword);
        user.addRole(Role.USER);
        User savedUser = userJpaRepository.save(user);

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

//        User user = userJpaRepository.findByUsernameAndPassword(loginDto.getUsername(), loginDto.getPassword())
//                .orElseThrow(() -> {
//                    log.warn("로그인 실패 - 사용자명 또는 비밀번호 불일치");
//                    return new Exception401("사용자명 또는 비밀번호가 올바르지 않습니다.");
//                });

        User user = userJpaRepository.findByUsernameWithRoles(loginDto.getUsername())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명 또는 비밀번호 불일치");
                    return new Exception401("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new Exception401("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

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
        // 정보 조회
        User user = findById(id);

        // 비밀번호 변경
        if (updateDto.hasPassword()) {
            // 비밀번호 해싱
            updateDto.setPassword( passwordEncoder.encode(updateDto.getPassword()) );
            user.update(updateDto);
        }

        // 새 이미지를 골랐을 때만 교체 (null 또는 파일데이터)
        if (updateDto.hasProfileImage()) {
            String newImage = FileUtil.saveImage(updateDto.getProfileImage());

            // 기존 파일
            String oldImage = user.getProfileImage();

            // DB에는 새 파일 이름 (변경 감지)
            user.updateProfileImage(newImage);

            // 기존 파일 서버 폴더에서 삭제처리
            FileUtil.deleteFile(oldImage);
        }

        // 세션 동기화용 DTO 변환
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

    // 마이페이지 조회 기능
    public UserResponse.DetailDto findDetail(Long userId) {
        User user = findById(userId);

        return new UserResponse.DetailDto(user);
    }

    /**
     * 파일 삭제
     * @param userId
     */
    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = findById(userId);
        String oldImage = user.getProfileImage();
        user.updateProfileImage(null);  // DB의 파일 정보 비우기 (null 세팅)

        FileUtil.deleteFile(oldImage);  // 실제 파일 삭제
    }
}
