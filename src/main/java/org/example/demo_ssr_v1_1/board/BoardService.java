package org.example.demo_ssr_v1_1.board;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception500;
import org.example.demo_ssr_v1_1.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 서비스 레이어 (Service Layer)
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
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 게시글 목록 조회
     * 
     * 트랜잭션:
     * - 읽기 전용 트랜잭션 (readOnly = true)
     * - 성능 최적화: 변경 작업이 없으므로 읽기 전용으로 설정
     * 
     * @return 게시글 목록 (생성일 기준 내림차순)
     */
    @Transactional(readOnly = true)
    public List<Board> 게시글목록조회() {
        // Repository의 쿼리 메서드 호출
        // findAllByOrderByCreatedAtDesc(): 생성일 기준 내림차순 정렬
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 게시글 상세 조회
     * 
     * Optional 처리:
     * - findById()는 Optional<Board>를 반환
     * - orElseThrow(): 값이 없으면 예외 발생, 있으면 Board 반환
     * 
     * @param id 게시글 ID
     * @return 게시글 엔티티
     * @throws Exception404 게시글이 없을 경우
     */
    @Transactional(readOnly = true)
    public Board 게시글상세조회(Long id) {
        // Optional을 사용한 null 안전 처리
        // 게시글이 없으면 Exception404 발생
        return boardRepository.findById(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없어요 : "));
    }

    /**
     * 게시글 작성
     * 
     * 트랜잭션:
     * - 기본 트랜잭션 (읽기/쓰기)
     * - save() 메서드 실행 시 INSERT 쿼리 실행
     * 
     * @param saveDTO 게시글 작성 DTO
     * @param user 작성자 (세션에서 가져온 사용자)
     * @return 저장된 게시글 엔티티
     */
    @Transactional
    public Board 게시글작성(BoardRequest.SaveDTO saveDTO, User user) {
        // DTO를 엔티티로 변환
        Board board = saveDTO.toEntity(user);
        
        // JpaRepository의 save() 메서드
        // - ID가 null이면 INSERT
        // - ID가 있으면 UPDATE
        return boardRepository.save(board);
    }

    /**
     * 게시글 수정 화면용 조회 (인가 검사 포함)
     * 
     * 인가 검사:
     * - 게시글 소유자만 수정 가능
     * - isOwner() 메서드로 소유자 확인
     * 
     * @param id 게시글 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 게시글 엔티티
     * @throws Exception404 게시글이 없을 경우
     * @throws Exception403 수정 권한이 없을 경우
     */
    @Transactional(readOnly = true)
    public Board 게시글수정화면(Long id, Long userId) {
        // 게시글 조회
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));

        // 인가 검사: 게시글 소유자인지 확인
        if (!board.isOwner(userId)) {
            throw new Exception403("게시글 수정 권한 없음");
        }

        return board;
    }

    /**
     * 게시글 수정 처리
     * 
     * 더티 체킹 (Dirty Checking):
     * - 엔티티를 조회한 후 필드 값을 변경
     * - 트랜잭션이 끝날 때 자동으로 UPDATE 쿼리 실행
     * - save()를 호출해도 되지만, @Transactional이 있으면 자동으로 UPDATE 됨
     * 
     * @param id 게시글 ID
     * @param updateDTO 게시글 수정 DTO
     * @param userId 현재 로그인한 사용자 ID
     * @throws Exception404 게시글이 없을 경우
     * @throws Exception403 수정 권한이 없을 경우
     * @throws Exception500 수정 실패 시
     */
    @Transactional
    public void 게시글수정(Long id, BoardRequest.UpdateDTO updateDTO, Long userId) {
        // 게시글 조회
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));

        // 인가 검사: 게시글 소유자인지 확인
        if (!board.isOwner(userId)) {
            throw new Exception403("게시글 수정 권한이 없습니다");
        }

        // 더티 체킹을 활용한 수정 처리
        try {
            // 엔티티의 상태 값 변경
            board.update(updateDTO);
            
            // save() 호출: 변경된 엔티티 저장
            // 참고: @Transactional이 있으면 save() 없이도 자동으로 UPDATE 됨
            // 하지만 명시적으로 save()를 호출하는 것이 더 명확함
            boardRepository.save(board);
        } catch (Exception e) {
            throw new Exception500("게시글 수정 실패: " + e.getMessage());
        }
    }

    /**
     * 게시글 삭제
     * 
     * 삭제 처리:
     * - deleteById() 메서드 사용
     * - 엔티티가 없으면 EmptyResultDataAccessException 발생
     * 
     * @param id 게시글 ID
     * @param userId 현재 로그인한 사용자 ID
     * @throws Exception404 게시글이 없을 경우
     * @throws Exception403 삭제 권한이 없을 경우
     */
    @Transactional
    public void 게시글삭제(Long id, Long userId) {
        // 게시글 조회
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));

        // 인가 검사: 게시글 소유자인지 확인
        if (!board.isOwner(userId)) {
            throw new Exception403("삭제 권한이 없습니다");
        }

        // JpaRepository의 deleteById() 메서드
        // 엔티티가 없으면 EmptyResultDataAccessException 발생
        boardRepository.deleteById(id);
    }
}

