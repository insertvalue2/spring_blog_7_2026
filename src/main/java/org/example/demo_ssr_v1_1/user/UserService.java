package org.example.demo_ssr_v1_1.user;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스 레이어 (Service Layer)
 * 
 * 핵심 개념:
 * 1. 서비스 레이어의 역할:
 *    - 비즈니스 로직을 처리하는 계층
 *    - Controller와 Repository 사이의 중간 계층
 *    - 트랜잭션 관리 (@Transactional)
 *    - 여러 Repository를 조합하여 복잡한 비즈니스 로직 처리
 * 
 * 2. 계층 구조 (3-Tier Architecture):
 *    Controller (표현 계층) 
 *      ↓ 요청
 *    Service (비즈니스 계층) ← 현재 위치
 *      ↓ 요청
 *    Repository (데이터 접근 계층)
 *      ↓
 *    Database
 * 
 * 3. @Service:
 *    - Spring이 이 클래스를 서비스 빈으로 등록
 *    - @Component의 특수한 형태
 *    - 비즈니스 로직을 담당하는 클래스임을 명시
 * 
 * 4. @RequiredArgsConstructor:
 *    - final 필드에 대한 생성자를 자동 생성
 *    - 의존성 주입(DI)을 위한 생성자 주입 방식
 *    - @Autowired 대신 생성자 주입을 사용 (권장 방식)
 * 
 * 5. @Transactional:
 *    - 메서드 실행 시 트랜잭션을 시작하고 종료 시 커밋
 *    - 예외 발생 시 자동 롤백
 *    - 더티 체킹(Dirty Checking) 활성화
 *    - 여러 DB 작업을 하나의 트랜잭션으로 묶어 일관성 보장
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입 처리
     * 
     * 비즈니스 로직:
     * 1. 유효성 검사 (DTO에서 처리)
     * 2. 사용자명 중복 체크
     * 3. 엔티티 저장
     * 
     * 트랜잭션:
     * - 기본 트랜잭션 (읽기/쓰기)
     * - save() 메서드 실행 시 INSERT 쿼리 실행
     * 
     * @param joinDTO 회원가입 DTO
     * @return 저장된 사용자 엔티티
     * @throws Exception400 사용자명이 이미 존재할 경우
     */
    @Transactional
    public User 회원가입(UserRequest.JoinDTO joinDTO) {
        // 1. 유효성 검사
        joinDTO.validate();

        // 2. 사용자명 중복 체크
        // Optional의 isPresent(): 값이 있으면 true, 없으면 false
        if (userRepository.findByUsername(joinDTO.getUsername()).isPresent()) {
            throw new Exception400("이미 존재하는 사용자 이름입니다");
        }

        // 3. DTO를 엔티티로 변환
        User user = joinDTO.toEntity();

        // 4. JpaRepository의 save() 메서드: 엔티티 저장 (INSERT)
        return userRepository.save(user);
    }

    /**
     * 로그인 처리
     * 
     * 비즈니스 로직:
     * 1. 유효성 검사 (DTO에서 처리)
     * 2. 사용자명과 비밀번호로 사용자 조회
     * 3. 로그인 성공/실패 처리
     * 
     * 트랜잭션:
     * - 읽기 전용 트랜잭션 (readOnly = true)
     * - 조회만 하므로 읽기 전용으로 설정
     * 
     * @param loginDTO 로그인 DTO
     * @return 로그인한 사용자 엔티티
     * @throws Exception400 로그인 실패 시 (사용자명 또는 비밀번호 불일치)
     */
    @Transactional(readOnly = true)
    public User 로그인(UserRequest.LoginDTO loginDTO) {
        // 1. 유효성 검사
        loginDTO.validate();

        // 2. 사용자명과 비밀번호로 사용자 조회
        // 쿼리 메서드 findByUsernameAndPassword()는 Optional<User>를 반환
        // orElse(null): Optional이 비어있으면 null 반환, 있으면 User 객체 반환
        User sessionUser = userRepository.findByUsernameAndPassword(
                loginDTO.getUsername(),
                loginDTO.getPassword())
                .orElse(null); // 로그인 실패 시 null 반환

        // 3. 로그인 실패 처리
        if (sessionUser == null) {
            throw new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다");
        }

        return sessionUser;
    }

    /**
     * 회원정보 수정 화면용 조회 (인가 검사 포함)
     * 
     * 인가 검사:
     * - 자기 자신의 정보만 조회 가능
     * - isOwner() 메서드로 소유자 확인
     * 
     * @param userId 현재 로그인한 사용자 ID
     * @return 사용자 엔티티
     * @throws Exception404 사용자가 없을 경우
     * @throws Exception403 수정 권한이 없을 경우
     */
    @Transactional(readOnly = true)
    public User 회원정보수정화면(Long userId) {
        // 세션의 사용자 ID로 회원정보 조회
        // JpaRepository의 findById()는 Optional<User>를 반환
        // orElseThrow(): Optional이 비어있으면 예외 발생, 있으면 User 객체 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 자기 자신의 정보만 수정 가능한지 확인
        if (!user.isOwner(userId)) {
            throw new Exception403("회원정보 수정 권한이 없습니다");
        }

        return user;
    }

    /**
     * 회원정보 수정 처리
     * 
     * 더티 체킹 (Dirty Checking):
     * - 엔티티를 조회한 후 필드 값을 변경
     * - 트랜잭션이 끝날 때 자동으로 UPDATE 쿼리 실행
     * - save()를 호출해도 되지만, @Transactional이 있으면 자동으로 UPDATE 됨
     * 
     * 세션 갱신:
     * - 수정된 사용자 정보를 세션에 다시 저장
     * - Controller에서 처리하도록 엔티티 반환
     * 
     * @param updateDTO 회원정보 수정 DTO
     * @param userId 현재 로그인한 사용자 ID
     * @return 수정된 사용자 엔티티
     * @throws Exception404 사용자가 없을 경우
     * @throws Exception403 수정 권한이 없을 경우
     */
    @Transactional
    public User 회원정보수정(UserRequest.UpdateDTO updateDTO, Long userId) {
        // 1. 수정하려는 회원정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 2. 인가 검사: 자기 자신의 정보만 수정 가능한지 확인
        if (!user.isOwner(userId)) {
            throw new Exception403("회원정보 수정 권한이 없습니다");
        }

        // 3. 유효성 검사
        updateDTO.validate();

        // 4. 더티 체킹을 활용한 수정 처리
        // 엔티티의 상태 값 변경
        user.update(updateDTO);

        // 5. 변경된 엔티티 저장 (더티 체킹)
        // 참고: @Transactional이 있으면 save() 없이도 자동으로 UPDATE 됨
        // 하지만 명시적으로 save()를 호출하는 것이 더 명확함
        User updateUser = userRepository.save(user);

        // 6. 수정된 사용자 정보 반환 (Controller에서 세션 갱신용)
        return updateUser;
    }
}

